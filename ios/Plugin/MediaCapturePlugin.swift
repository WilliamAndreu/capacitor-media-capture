import Foundation
import Capacitor
import AVFoundation
import UIKit
import MobileCoreServices

@objc(MediaCapturePlugin)
public class MediaCapturePlugin: CAPPlugin {
    private var imagePicker: UIImagePickerController?
    private var currentCall: CAPPluginCall?
    private var audioRecorder: AVAudioRecorder?

    @objc func captureAudio(_ call: CAPPluginCall) {
        currentCall = call

        let limit = call.getInt("limit") ?? 1
        let duration = call.getDouble("duration")

        // Check current permission status first
        let audioStatus = AVAudioSession.sharedInstance().recordPermission

        switch audioStatus {
        case .granted:
            startAudioRecording(duration: duration)
        case .denied:
            DispatchQueue.main.async {
                call.reject("Microphone permission denied. Please enable microphone access in Settings > Privacy > Microphone")
            }
        case .undetermined:
            // Request permission
            AVAudioSession.sharedInstance().requestRecordPermission { [weak self] granted in
                DispatchQueue.main.async {
                    if granted {
                        self?.startAudioRecording(duration: duration)
                    } else {
                        call.reject("Microphone permission denied. Please enable microphone access in Settings > Privacy > Microphone")
                    }
                }
            }
        @unknown default:
            DispatchQueue.main.async {
                call.reject("Unknown permission status")
            }
        }
    }

    @objc func captureImage(_ call: CAPPluginCall) {
        currentCall = call

        let limit = call.getInt("limit") ?? 1

        // Check camera availability
        guard UIImagePickerController.isSourceTypeAvailable(.camera) else {
            DispatchQueue.main.async {
                call.reject("Camera not available on this device")
            }
            return
        }

        // Check current permission status
        let cameraStatus = AVCaptureDevice.authorizationStatus(for: .video)

        switch cameraStatus {
        case .authorized:
            showImagePicker(limit: limit)
        case .denied, .restricted:
            DispatchQueue.main.async {
                call.reject("Camera permission denied. Please enable camera access in Settings > Privacy > Camera")
            }
        case .notDetermined:
            // Request permission
            AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
                DispatchQueue.main.async {
                    if granted {
                        self?.showImagePicker(limit: limit)
                    } else {
                        call.reject("Camera permission denied. Please enable camera access in Settings > Privacy > Camera")
                    }
                }
            }
        @unknown default:
            DispatchQueue.main.async {
                call.reject("Unknown permission status")
            }
        }
    }

    @objc func captureVideo(_ call: CAPPluginCall) {
        currentCall = call

        let limit = call.getInt("limit") ?? 1
        let duration = call.getDouble("duration")
        let quality = call.getInt("quality") ?? 1

        // Check camera availability
        guard UIImagePickerController.isSourceTypeAvailable(.camera) else {
            DispatchQueue.main.async {
                call.reject("Camera not available on this device")
            }
            return
        }

        // Check both camera and microphone permissions (needed for video with audio)
        let cameraStatus = AVCaptureDevice.authorizationStatus(for: .video)
        let audioStatus = AVAudioSession.sharedInstance().recordPermission

        // Handle camera permission
        switch cameraStatus {
        case .authorized:
            // Camera is authorized, now check audio
            handleVideoAudioPermission(call: call, duration: duration, quality: quality)
        case .denied, .restricted:
            DispatchQueue.main.async {
                call.reject("Camera permission denied. Please enable camera access in Settings > Privacy > Camera")
            }
        case .notDetermined:
            // Request camera permission first
            AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
                DispatchQueue.main.async {
                    if granted {
                        self?.handleVideoAudioPermission(call: call, duration: duration, quality: quality)
                    } else {
                        call.reject("Camera permission denied. Please enable camera access in Settings > Privacy > Camera")
                    }
                }
            }
        @unknown default:
            DispatchQueue.main.async {
                call.reject("Unknown camera permission status")
            }
        }
    }

    private func handleVideoAudioPermission(call: CAPPluginCall, duration: Double?, quality: Int) {
        let audioStatus = AVAudioSession.sharedInstance().recordPermission

        switch audioStatus {
        case .granted:
            showVideoPicker(duration: duration, quality: quality)
        case .denied:
            // Show warning but continue (video will be recorded without audio)
            print("Warning: Microphone permission denied. Video will be recorded without audio.")
            showVideoPicker(duration: duration, quality: quality)
        case .undetermined:
            // Request microphone permission
            AVAudioSession.sharedInstance().requestRecordPermission { [weak self] granted in
                DispatchQueue.main.async {
                    if !granted {
                        print("Warning: Microphone permission denied. Video will be recorded without audio.")
                    }
                    self?.showVideoPicker(duration: duration, quality: quality)
                }
            }
        @unknown default:
            showVideoPicker(duration: duration, quality: quality)
        }
    }

    @objc func getFormatData(_ call: CAPPluginCall) {
        guard let fullPath = call.getString("fullPath"),
              let type = call.getString("type") else {
            DispatchQueue.main.async {
                call.reject("Missing parameters")
            }
            return
        }

        let formatData = getMediaFormatData(fullPath: fullPath, type: type)
        DispatchQueue.main.async {
            call.resolve(formatData)
        }
    }

    // MARK: - Private Methods

    private func startAudioRecording(duration: Double?) {
        let audioSession = AVAudioSession.sharedInstance()
        do {
            try audioSession.setCategory(.record, mode: .default)
            try audioSession.setActive(true)

            let fileManager = FileManager.default
            let cachesPath = fileManager.urls(for: .cachesDirectory, in: .userDomainMask)[0]
            let timestamp = Int(Date().timeIntervalSince1970 * 1000)
            let audioFilename = "audio_\(timestamp).m4a"
            let audioURL = cachesPath.appendingPathComponent(audioFilename)

            let settings: [String: Any] = [
                AVFormatIDKey: Int(kAudioFormatMPEG4AAC),
                AVSampleRateKey: 44100,
                AVNumberOfChannelsKey: 2,
                AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue
            ]

            audioRecorder = try AVAudioRecorder(url: audioURL, settings: settings)
            audioRecorder?.record()

            // If duration is specified, stop after that time
            if let duration = duration, duration > 0 {
                DispatchQueue.main.asyncAfter(deadline: .now() + duration) { [weak self] in
                    self?.stopAudioRecording()
                }
            } else {
                // Show a simple alert to stop recording
                showAudioRecordingAlert()
            }
        } catch {
            DispatchQueue.main.async { [weak self] in
                self?.currentCall?.reject("Failed to start recording: \(error.localizedDescription)")
            }
        }
    }

    private func showAudioRecordingAlert() {
        DispatchQueue.main.async { [weak self] in
            guard let self = self,
                  let viewController = self.bridge?.viewController else { return }

            let alert = UIAlertController(title: "Recording Audio", message: "Tap Stop when finished", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Stop", style: .default) { [weak self] _ in
                self?.stopAudioRecording()
            })

            viewController.present(alert, animated: true)
        }
    }

    private func stopAudioRecording() {
        guard let recorder = audioRecorder else {
            DispatchQueue.main.async { [weak self] in
                self?.currentCall?.reject("No active recording")
            }
            return
        }

        recorder.stop()

        let audioURL = recorder.url
        let fileManager = FileManager.default

        if fileManager.fileExists(atPath: audioURL.path) {
            let mediaFile = createMediaFile(path: audioURL.path, type: "audio/m4a")
            let result: [String: Any] = [
                "files": [mediaFile]
            ]
            DispatchQueue.main.async { [weak self] in
                self?.currentCall?.resolve(result)
                self?.currentCall = nil
            }
        } else {
            DispatchQueue.main.async { [weak self] in
                self?.currentCall?.reject("Recording file not found")
                self?.currentCall = nil
            }
        }

        audioRecorder = nil
    }

    private func showImagePicker(limit: Int) {
        DispatchQueue.main.async { [weak self] in
            guard let self = self,
                  let viewController = self.bridge?.viewController else { return }

            self.imagePicker = UIImagePickerController()
            self.imagePicker?.delegate = self
            self.imagePicker?.sourceType = .camera
            self.imagePicker?.mediaTypes = [kUTTypeImage as String]
            self.imagePicker?.allowsEditing = false

            viewController.present(self.imagePicker!, animated: true)
        }
    }

    private func showVideoPicker(duration: Double?, quality: Int) {
        DispatchQueue.main.async { [weak self] in
            guard let self = self,
                  let viewController = self.bridge?.viewController else { return }

            self.imagePicker = UIImagePickerController()
            self.imagePicker?.delegate = self
            self.imagePicker?.sourceType = .camera
            self.imagePicker?.mediaTypes = [kUTTypeMovie as String]
            self.imagePicker?.allowsEditing = false

            if let duration = duration, duration > 0 {
                self.imagePicker?.videoMaximumDuration = duration
            }

            // Set video quality
            switch quality {
            case 0:
                self.imagePicker?.videoQuality = .typeLow
            case 5:
                self.imagePicker?.videoQuality = .typeMedium
            default:
                self.imagePicker?.videoQuality = .typeHigh
            }

            viewController.present(self.imagePicker!, animated: true)
        }
    }

    private func createMediaFile(path: String, type: String) -> [String: Any] {
        let fileManager = FileManager.default
        let fileURL = URL(fileURLWithPath: path)

        var fileSize: UInt64 = 0
        var lastModified: TimeInterval = 0

        do {
            let attributes = try fileManager.attributesOfItem(atPath: path)
            fileSize = attributes[.size] as? UInt64 ?? 0
            lastModified = (attributes[.modificationDate] as? Date)?.timeIntervalSince1970 ?? 0
        } catch {
            print("Error getting file attributes: \(error)")
        }

        return [
            "name": fileURL.lastPathComponent,
            "fullPath": "file://\(path)",
            "type": type,
            "lastModifiedDate": Int(lastModified * 1000),
            "size": fileSize
        ]
    }

    private func getMediaFormatData(fullPath: String, type: String) -> [String: Any] {
        var result: [String: Any] = [
            "height": 0,
            "width": 0,
            "bitrate": 0,
            "duration": 0,
            "codecs": ""
        ]

        let path = fullPath.replacingOccurrences(of: "file://", with: "")
        let url = URL(fileURLWithPath: path)

        if type.hasPrefix("image/") {
            if let image = UIImage(contentsOfFile: path) {
                result["height"] = Int(image.size.height)
                result["width"] = Int(image.size.width)
            }
        } else if type.hasPrefix("video/") || type.hasPrefix("audio/") {
            let asset = AVURLAsset(url: url)
            result["duration"] = Int(CMTimeGetSeconds(asset.duration))

            if type.hasPrefix("video/") {
                if let track = asset.tracks(withMediaType: .video).first {
                    let size = track.naturalSize
                    result["height"] = Int(size.height)
                    result["width"] = Int(size.width)
                }
            }
        }

        return result
    }
}

// MARK: - UIImagePickerControllerDelegate

extension MediaCapturePlugin: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    public func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
        // Dismiss picker and wait for completion before processing
        picker.dismiss(animated: true) { [weak self] in
            guard let self = self else { return }

            // Process file operations in background thread to avoid blocking UI
            DispatchQueue.global(qos: .userInitiated).async {
                if let image = info[.originalImage] as? UIImage {
                    // Save image to caches directory for sharing
                    let fileManager = FileManager.default
                    let cachesPath = fileManager.urls(for: .cachesDirectory, in: .userDomainMask)[0]
                    let timestamp = Int(Date().timeIntervalSince1970 * 1000)
                    let filename = "photo_\(timestamp).jpg"
                    let fileURL = cachesPath.appendingPathComponent(filename)

                    if let data = image.jpegData(compressionQuality: 1.0) {
                        do {
                            try data.write(to: fileURL)

                            let mediaFile = self.createMediaFile(path: fileURL.path, type: "image/jpeg")
                            let result: [String: Any] = [
                                "files": [mediaFile]
                            ]

                            DispatchQueue.main.async {
                                self.currentCall?.resolve(result)
                                self.currentCall = nil
                            }
                        } catch {
                            DispatchQueue.main.async {
                                self.currentCall?.reject("Failed to save image: \(error.localizedDescription)")
                                self.currentCall = nil
                            }
                        }
                    }
                } else if let videoURL = info[.mediaURL] as? URL {
                    // Copy video to app's caches directory for sharing
                    let fileManager = FileManager.default
                    let cachesPath = fileManager.urls(for: .cachesDirectory, in: .userDomainMask)[0]
                    let timestamp = Int(Date().timeIntervalSince1970 * 1000)
                    let filename = "video_\(timestamp).mp4"
                    let destinationURL = cachesPath.appendingPathComponent(filename)

                    do {
                        // Remove destination file if it exists
                        if fileManager.fileExists(atPath: destinationURL.path) {
                            try fileManager.removeItem(at: destinationURL)
                        }
                        // Copy video to caches directory
                        try fileManager.copyItem(at: videoURL, to: destinationURL)

                        let mediaFile = self.createMediaFile(path: destinationURL.path, type: "video/mp4")
                        let result: [String: Any] = [
                            "files": [mediaFile]
                        ]

                        DispatchQueue.main.async {
                            self.currentCall?.resolve(result)
                            self.currentCall = nil
                        }
                    } catch {
                        DispatchQueue.main.async {
                            self.currentCall?.reject("Failed to copy video: \(error.localizedDescription)")
                            self.currentCall = nil
                        }
                    }
                }
            }
        }
    }

    public func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true) { [weak self] in
            DispatchQueue.main.async {
                self?.currentCall?.reject("User cancelled")
                self?.currentCall = nil
            }
        }
    }
}
