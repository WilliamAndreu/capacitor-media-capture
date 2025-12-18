'use strict';

var core = require('@capacitor/core');

/**
 * Error codes for media capture
 */
exports.CaptureError = void 0;
(function (CaptureError) {
    /**
     * Camera or microphone failed to capture image or sound
     */
    CaptureError[CaptureError["CAPTURE_INTERNAL_ERR"] = 0] = "CAPTURE_INTERNAL_ERR";
    /**
     * Camera application or audio capture application is currently serving other capture request
     */
    CaptureError[CaptureError["CAPTURE_APPLICATION_BUSY"] = 1] = "CAPTURE_APPLICATION_BUSY";
    /**
     * Invalid use of the API (e.g. limit parameter has value less than one)
     */
    CaptureError[CaptureError["CAPTURE_INVALID_ARGUMENT"] = 2] = "CAPTURE_INVALID_ARGUMENT";
    /**
     * User exited camera application or audio capture application before capturing anything
     */
    CaptureError[CaptureError["CAPTURE_NO_MEDIA_FILES"] = 3] = "CAPTURE_NO_MEDIA_FILES";
    /**
     * The requested capture operation is not supported
     */
    CaptureError[CaptureError["CAPTURE_NOT_SUPPORTED"] = 20] = "CAPTURE_NOT_SUPPORTED";
    /**
     * Permission denied
     */
    CaptureError[CaptureError["CAPTURE_PERMISSION_DENIED"] = 21] = "CAPTURE_PERMISSION_DENIED";
})(exports.CaptureError || (exports.CaptureError = {}));

const MediaCapture = core.registerPlugin('MediaCapture', {
    web: () => Promise.resolve().then(function () { return web; }).then(m => new m.MediaCaptureWeb()),
});

class MediaCaptureWeb extends core.WebPlugin {
    async captureAudio(options) {
        console.log('captureAudio', options);
        return new Promise((resolve, reject) => {
            navigator.mediaDevices
                .getUserMedia({ audio: true })
                .then((stream) => {
                const mediaRecorder = new MediaRecorder(stream);
                const audioChunks = [];
                mediaRecorder.addEventListener('dataavailable', (event) => {
                    audioChunks.push(event.data);
                });
                mediaRecorder.addEventListener('stop', () => {
                    const audioBlob = new Blob(audioChunks, { type: 'audio/webm' });
                    const audioUrl = URL.createObjectURL(audioBlob);
                    const timestamp = new Date().getTime();
                    const mediaFile = {
                        name: `audio_${timestamp}.webm`,
                        fullPath: audioUrl,
                        type: 'audio/webm',
                        lastModifiedDate: timestamp,
                        size: audioBlob.size,
                    };
                    stream.getTracks().forEach((track) => track.stop());
                    resolve({ files: [mediaFile] });
                });
                mediaRecorder.start();
                // Stop recording after duration or when limit is reached
                const duration = (options === null || options === void 0 ? void 0 : options.duration) || 0;
                if (duration > 0) {
                    setTimeout(() => {
                        if (mediaRecorder.state === 'recording') {
                            mediaRecorder.stop();
                        }
                    }, duration * 1000);
                }
                else {
                    // Auto-stop after 60 seconds if no duration specified
                    setTimeout(() => {
                        if (mediaRecorder.state === 'recording') {
                            mediaRecorder.stop();
                        }
                    }, 60000);
                }
            })
                .catch((error) => {
                console.error('Audio capture error:', error);
                reject({
                    code: exports.CaptureError.CAPTURE_INTERNAL_ERR,
                    message: error.message || 'Failed to capture audio',
                });
            });
        });
    }
    async captureImage(options) {
        console.log('captureImage', options);
        return new Promise((resolve, reject) => {
            const input = document.createElement('input');
            input.type = 'file';
            input.accept = 'image/*';
            input.capture = 'environment';
            if ((options === null || options === void 0 ? void 0 : options.limit) && options.limit > 1) {
                input.multiple = true;
            }
            input.onchange = async (event) => {
                const target = event.target;
                const files = target.files;
                if (!files || files.length === 0) {
                    reject({
                        code: exports.CaptureError.CAPTURE_NO_MEDIA_FILES,
                        message: 'No media files captured',
                    });
                    return;
                }
                const limit = (options === null || options === void 0 ? void 0 : options.limit) || 1;
                const mediaFiles = [];
                for (let i = 0; i < Math.min(files.length, limit); i++) {
                    const file = files[i];
                    const fileUrl = URL.createObjectURL(file);
                    mediaFiles.push({
                        name: file.name,
                        fullPath: fileUrl,
                        type: file.type,
                        lastModifiedDate: file.lastModified,
                        size: file.size,
                    });
                }
                resolve({ files: mediaFiles });
            };
            input.oncancel = () => {
                reject({
                    code: exports.CaptureError.CAPTURE_NO_MEDIA_FILES,
                    message: 'User cancelled image capture',
                });
            };
            input.click();
        });
    }
    async captureVideo(options) {
        console.log('captureVideo', options);
        return new Promise((resolve, reject) => {
            const input = document.createElement('input');
            input.type = 'file';
            input.accept = 'video/*';
            input.capture = 'environment';
            if ((options === null || options === void 0 ? void 0 : options.limit) && options.limit > 1) {
                input.multiple = true;
            }
            input.onchange = async (event) => {
                const target = event.target;
                const files = target.files;
                if (!files || files.length === 0) {
                    reject({
                        code: exports.CaptureError.CAPTURE_NO_MEDIA_FILES,
                        message: 'No media files captured',
                    });
                    return;
                }
                const limit = (options === null || options === void 0 ? void 0 : options.limit) || 1;
                const mediaFiles = [];
                for (let i = 0; i < Math.min(files.length, limit); i++) {
                    const file = files[i];
                    const fileUrl = URL.createObjectURL(file);
                    mediaFiles.push({
                        name: file.name,
                        fullPath: fileUrl,
                        type: file.type,
                        lastModifiedDate: file.lastModified,
                        size: file.size,
                    });
                }
                resolve({ files: mediaFiles });
            };
            input.oncancel = () => {
                reject({
                    code: exports.CaptureError.CAPTURE_NO_MEDIA_FILES,
                    message: 'User cancelled video capture',
                });
            };
            input.click();
        });
    }
    async getFormatData(mediaFile) {
        console.log('getFormatData', mediaFile);
        // For web implementation, we can try to extract format data
        // This is a basic implementation
        return new Promise((resolve, reject) => {
            if (mediaFile.type.startsWith('image/')) {
                const img = new Image();
                img.onload = () => {
                    resolve({
                        height: img.height,
                        width: img.width,
                        bitrate: 0,
                        duration: 0,
                    });
                };
                img.onerror = () => {
                    reject({
                        code: exports.CaptureError.CAPTURE_INTERNAL_ERR,
                        message: 'Failed to load image',
                    });
                };
                img.src = mediaFile.fullPath;
            }
            else if (mediaFile.type.startsWith('video/') ||
                mediaFile.type.startsWith('audio/')) {
                const media = document.createElement(mediaFile.type.startsWith('video/') ? 'video' : 'audio');
                media.onloadedmetadata = () => {
                    const data = {
                        duration: media.duration,
                        bitrate: 0,
                    };
                    if ('videoWidth' in media && 'videoHeight' in media) {
                        data.width = media.videoWidth;
                        data.height = media.videoHeight;
                    }
                    else {
                        data.width = 0;
                        data.height = 0;
                    }
                    resolve(data);
                };
                media.onerror = () => {
                    reject({
                        code: exports.CaptureError.CAPTURE_INTERNAL_ERR,
                        message: 'Failed to load media',
                    });
                };
                media.src = mediaFile.fullPath;
            }
            else {
                reject({
                    code: exports.CaptureError.CAPTURE_NOT_SUPPORTED,
                    message: 'Unsupported media type',
                });
            }
        });
    }
}

var web = /*#__PURE__*/Object.freeze({
    __proto__: null,
    MediaCaptureWeb: MediaCaptureWeb
});

exports.MediaCapture = MediaCapture;
//# sourceMappingURL=plugin.cjs.js.map
