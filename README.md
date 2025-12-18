# 📸 Capacitor Media Capture

> A powerful, modern Capacitor plugin for capturing **audio**, **video**, and **images** on iOS and Android.

[![npm version](https://img.shields.io/npm/v/capacitor-media-capture.svg)](https://www.npmjs.com/package/capacitor-media-capture)
[![License](https://img.shields.io/npm/l/capacitor-media-capture.svg)](https://github.com/WilliamAndreu/capacitor-media-capture/blob/master/LICENSE)

Built for **Capacitor 6 & 7** with full TypeScript support and a modern Promise-based API. Compatible with the Cordova Media Capture plugin API for easy migration.

---

## ✨ Features

- 🎥 **Video capture** with quality and duration controls
- 📷 **Image capture** from device camera
- 🎙️ **Audio recording** with duration limits
- 📊 **Media format data** (dimensions, duration, file size)
- 🔒 **Smart permission handling** for iOS and Android
- 📱 **Modern permissions** (Android 13+ granular permissions)
- 🎯 **TypeScript** definitions included
- ⚡ **Promise-based API** (no callbacks!)

---

## 📦 Installation

### Option 1: From npm (Recommended)

```bash
npm install capacitor-media-capture
npx cap sync
```

### Option 2: From GitHub

```bash
# Latest version
npm install WilliamAndreu/capacitor-media-capture
npx cap sync

# Specific version
npm install WilliamAndreu/capacitor-media-capture#v1.0.0
npx cap sync
```

---

## 🚀 Quick Start

```typescript
import { MediaCapture } from 'capacitor-media-capture';

// Capture a video
const video = await MediaCapture.captureVideo({
  duration: 30,  // 30 seconds max
  quality: 1     // High quality
});

console.log('Video captured:', video.files[0].fullPath);
```

---

## 📱 Platform Support

| Platform | Version | Status |
|----------|---------|--------|
| **Android** | API 22 (5.1) to API 35 (14+) | ✅ Full support |
| **iOS** | iOS 13.0 to iOS 18+ | ✅ Full support |
| **Web** | Modern browsers | ⚠️ Basic support |

### Android Features
- ✅ Android 13+ granular media permissions
- ✅ Robust permission handling
- ✅ FileProvider support for sharing

### iOS Features
- ✅ Smart permission checking
- ✅ Graceful denial handling
- ✅ Fixed share sheet conflicts
- ✅ Proper threading for UI operations

---

## ⚙️ Configuration

### 🤖 Android Setup

#### Step 1: Add Permissions to AndroidManifest.xml

Add these permissions to `android/app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- ========== REQUIRED PERMISSIONS ========== -->

    <!-- Camera access -->
    <uses-permission android:name="android.permission.CAMERA" />

    <!-- Microphone access (for audio and video with sound) -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <!-- ========== STORAGE PERMISSIONS ========== -->

    <!-- For Android 12 and below -->
    <uses-permission
        android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />

    <uses-permission
        android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />

    <!-- For Android 13+ (API 33+) - Granular Media Permissions -->
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
    <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />

    <!-- Optional: Audio settings (for recording optimization) -->
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

    <application>
        <!-- Your existing application config -->

        <!-- ========== FileProvider (REQUIRED) ========== -->
        <!-- If you already have a FileProvider, skip to Step 2 -->

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>

    </application>
</manifest>
```

> ⚠️ **Important**: The `android:authorities` must match your FileProvider setup. Most apps use `${applicationId}.fileprovider`.

#### Step 2: Configure FileProvider Paths

**If you already have a FileProvider**, add these paths to your existing `android/app/src/main/res/xml/file_paths.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Your existing paths -->

    <!-- ========== ADD THESE FOR MEDIA CAPTURE ========== -->
    <cache-path name="media_capture_cache" path="com.capacitor.mediacapture/" />
    <external-cache-path name="media_capture_external_cache" path="com.capacitor.mediacapture/" />
    <external-files-path name="media_capture_external_files" path="." />
    <files-path name="media_capture_files" path="." />

</paths>
```

**If you DON'T have a FileProvider yet**, create `android/app/src/main/res/xml/file_paths.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <cache-path name="media_capture_cache" path="com.capacitor.mediacapture/" />
    <external-cache-path name="media_capture_external_cache" path="com.capacitor.mediacapture/" />
    <external-files-path name="media_capture_external_files" path="." />
    <files-path name="media_capture_files" path="." />
</paths>
```

---

### 🍎 iOS Setup

#### Add Required Keys to Info.plist

Add these keys to `ios/App/App/Info.plist`:

```xml
<dict>
    <!-- Your existing keys -->

    <!-- ========== REQUIRED PERMISSIONS ========== -->

    <!-- Camera Permission (REQUIRED) -->
    <key>NSCameraUsageDescription</key>
    <string>This app needs camera access to capture photos and videos</string>

    <!-- Microphone Permission (REQUIRED for audio and video with sound) -->
    <key>NSMicrophoneUsageDescription</key>
    <string>This app needs microphone access to record audio and videos with sound</string>

    <!-- ========== OPTIONAL PERMISSIONS ========== -->

    <!-- Only needed if you save to Photo Library -->
    <key>NSPhotoLibraryUsageDescription</key>
    <string>This app needs access to your photo library to save captured media</string>

    <!-- For iOS 14+ (if saving to Photo Library) -->
    <key>NSPhotoLibraryAddUsageDescription</key>
    <string>This app needs permission to save photos and videos to your gallery</string>

</dict>
```

> 🚨 **Critical**: Missing these keys will cause your app to **crash immediately** on iOS without explanation!

> 💡 **Tip**: Customize the permission messages to explain why your specific app needs these permissions.

---

## 📖 API Reference

### `captureAudio(options?)`

Capture audio recording.

```typescript
import { MediaCapture } from 'capacitor-media-capture';

const result = await MediaCapture.captureAudio({
  limit: 1,        // Max number of audio clips (default: 1)
  duration: 60     // Max duration in seconds (optional)
});

console.log(result.files); // Array of MediaFile objects
```

**Options:**
| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `limit` | `number` | `1` | Maximum number of audio clips to capture |
| `duration` | `number` | - | Maximum duration in seconds (optional) |

---

### `captureImage(options?)`

Capture photos using the device camera.

```typescript
import { MediaCapture } from 'capacitor-media-capture';

const result = await MediaCapture.captureImage({
  limit: 1,        // Max number of images (default: 1)
  quality: 100     // Image quality 0-100 (Android only)
});

console.log(result.files); // Array of MediaFile objects
```

**Options:**
| Property | Type | Default | Platform | Description |
|----------|------|---------|----------|-------------|
| `limit` | `number` | `1` | All | Maximum number of images to capture |
| `quality` | `number` | `100` | Android only | Image quality (0-100) |

---

### `captureVideo(options?)`

Capture video using the device camera.

```typescript
import { MediaCapture } from 'capacitor-media-capture';

const result = await MediaCapture.captureVideo({
  limit: 1,        // Max number of video clips (default: 1)
  duration: 30,    // Max duration in seconds (optional)
  quality: 1       // Video quality: 0 = low, 1 = high (iOS only)
});

console.log(result.files); // Array of MediaFile objects
```

**Options:**
| Property | Type | Default | Platform | Description |
|----------|------|---------|----------|-------------|
| `limit` | `number` | `1` | All | Maximum number of video clips |
| `duration` | `number` | - | All | Maximum duration in seconds |
| `quality` | `number` | `1` | iOS only | Video quality (0 = low, 1 = high) |

---

### `getFormatData(file)`

Get format information about a captured media file.

```typescript
import { MediaCapture } from 'capacitor-media-capture';

const result = await MediaCapture.captureVideo();
const file = result.files[0];

const formatData = await MediaCapture.getFormatData({
  fullPath: file.fullPath,
  type: file.type
});

console.log(formatData);
// {
//   width: 1920,
//   height: 1080,
//   duration: 15.5,
//   bitrate: 0,
//   codecs: ""
// }
```

**Parameters:**
| Property | Type | Description |
|----------|------|-------------|
| `fullPath` | `string` | Full path to the media file |
| `type` | `string` | MIME type of the file |

**Returns:**
| Property | Type | Description |
|----------|------|-------------|
| `width` | `number` | Width in pixels (0 for audio) |
| `height` | `number` | Height in pixels (0 for audio) |
| `duration` | `number` | Duration in seconds (0 for images) |
| `bitrate` | `number` | Bitrate in bits per second |
| `codecs` | `string` | Codec information |

---

## 📝 TypeScript Interfaces

### `MediaFile`

```typescript
interface MediaFile {
  name: string;              // File name (e.g., "video_1234567890.mp4")
  fullPath: string;          // Full URI path (e.g., "file:///path/to/file.mp4")
  type: string;              // MIME type (e.g., "video/mp4")
  lastModifiedDate: number;  // Timestamp in milliseconds
  size: number;              // File size in bytes
}
```

### `MediaFileData`

```typescript
interface MediaFileData {
  codecs?: string;           // Audio/video codecs
  bitrate?: number;          // Bitrate in bits per second
  height?: number;           // Height in pixels (0 for audio)
  width?: number;            // Width in pixels (0 for audio)
  duration?: number;         // Duration in seconds (0 for images)
}
```

### `CaptureError`

```typescript
interface CaptureError {
  code: number;              // Error code
  message: string;           // Error message
}
```

**Error Codes:**
- `CAPTURE_INTERNAL_ERR` - Internal error
- `CAPTURE_APPLICATION_BUSY` - Application busy
- `CAPTURE_INVALID_ARGUMENT` - Invalid argument
- `CAPTURE_NO_MEDIA_FILES` - No media files captured
- `CAPTURE_NOT_SUPPORTED` - Operation not supported
- `CAPTURE_PERMISSION_DENIED` - Permission denied

---

## 🔄 Migration from Cordova

This plugin provides a similar API to the Cordova Media Capture plugin for easy migration.

### Key Differences

| Feature | Cordova | Capacitor |
|---------|---------|-----------|
| API Style | Callbacks | Promises/async-await |
| TypeScript | ❌ No | ✅ Full support |
| Error Handling | Error callbacks | try-catch with typed errors |
| Import | Global `navigator` | ES6 import |

### Migration Example

**Before (Cordova):**
```javascript
navigator.device.capture.captureVideo(
  function success(mediaFiles) {
    console.log('Captured:', mediaFiles[0].fullPath);
  },
  function error(err) {
    console.error('Error:', err.message);
  },
  { limit: 1, duration: 30 }
);
```

**After (Capacitor):**
```typescript
import { MediaCapture } from 'capacitor-media-capture';

try {
  const result = await MediaCapture.captureVideo({
    limit: 1,
    duration: 30
  });
  console.log('Captured:', result.files[0].fullPath);
} catch (error) {
  console.error('Error:', error.message);
}
```

---

## 🐛 Troubleshooting

### Android Issues

#### ❌ SecurityException: authority mismatch

**Error:**
```
SecurityException: The authority does not match the one of the contentProvider
```

**Solution:** Make sure the `android:authorities` in your FileProvider matches what's used in the code. Most apps use `${applicationId}.fileprovider`.

#### ❌ Permission Denied

**Error:**
```
Permission denied: CAMERA or RECORD_AUDIO
```

**Solution:**
1. Verify all permissions are in `AndroidManifest.xml`
2. Check that your app's `targetSdkVersion` is set correctly
3. For Android 13+, ensure granular media permissions are added

#### ❌ File Not Found / Can't Share

**Error:**
```
FileNotFoundException or sharing fails
```

**Solution:** Ensure FileProvider paths are configured correctly in `file_paths.xml`.

---

### iOS Issues

#### ❌ App Crashes Immediately

**Error:** App crashes without error message

**Solution:** You're missing permission keys in `Info.plist`. Add `NSCameraUsageDescription` and `NSMicrophoneUsageDescription`.

#### ❌ "Can't share while sharing is in progress"

**Error:**
```
Can't share while sharing is in progress
```

**Solution:** This is already fixed in v1.0.0+. Update to the latest version.

#### ❌ Permission Denied

**Error:** User denied camera/microphone permission

**Solution:**
1. Provide clear permission descriptions in `Info.plist`
2. Handle permission denials gracefully in your code
3. Direct users to Settings if they denied permission

---

## 💡 Tips & Best Practices

### 1. Handle Permissions Gracefully

```typescript
try {
  const result = await MediaCapture.captureVideo();
  // Process result
} catch (error) {
  if (error.message.includes('permission')) {
    // Show user-friendly message explaining why permission is needed
    alert('Camera access is required to record videos. Please enable it in Settings.');
  }
}
```

### 2. Set Reasonable Duration Limits

```typescript
// Good: Reasonable limit
await MediaCapture.captureVideo({ duration: 60 }); // 1 minute

// Bad: Too long, large file size
await MediaCapture.captureVideo({ duration: 3600 }); // 1 hour!
```

### 3. Check File Size Before Upload

```typescript
const result = await MediaCapture.captureVideo();
const file = result.files[0];

if (file.size > 10 * 1024 * 1024) { // 10 MB
  alert('Video is too large. Please record a shorter video.');
}
```

### 4. Clean Up Files After Use

Files are stored in cache directories which are automatically cleaned by the OS, but you can manually delete them if needed:

```typescript
// iOS: Files are in .cachesDirectory (auto-cleaned)
// Android: Files are in caches directory (auto-cleaned)

// Manual cleanup if needed:
// Use Capacitor Filesystem plugin to delete files
```

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Based on the [Apache Cordova Media Capture Plugin](https://github.com/apache/cordova-plugin-media-capture)
- Built with ❤️ for the Capacitor community

---

## 📞 Support

- 🐛 **Bug Reports**: [GitHub Issues](https://github.com/WilliamAndreu/capacitor-media-capture/issues)
- 💬 **Questions**: [GitHub Discussions](https://github.com/WilliamAndreu/capacitor-media-capture/discussions)
- 📧 **Email**: [Create an issue](https://github.com/WilliamAndreu/capacitor-media-capture/issues/new)

---

<div align="center">

Made with ❤️ by [William Aveiga](https://github.com/WilliamAndreu)

⭐ Star this repo if you find it helpful!

</div>
