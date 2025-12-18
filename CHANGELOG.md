# Changelog

All notable changes to this project will be documented in this file.

## [1.0.0] - 2025-12-15

### Added
- Initial release of capacitor-media-capture plugin
- Complete TypeScript API with types
- Android implementation with Java
- iOS implementation with Swift
- Web implementation with modern browser APIs
- Support for capturing audio, video, and images
- Format data extraction for media files

### Features
- **Android Support**: API 22 (Android 5.1) to API 35 (Android 14+)
  - Full support for Android 13+ granular media permissions (READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_AUDIO)
  - Backward compatibility with Android 12 and below using READ_EXTERNAL_STORAGE
  - Robust permission handling with clear error messages
  - FileProvider configuration for secure file sharing
  - Multi-permission request for video (camera + microphone)

- **iOS Support**: iOS 13.0 to iOS 18+
  - Smart permission checking before requesting
  - Handles all authorization states (.authorized, .denied, .restricted, .notDetermined)
  - Graceful fallback when microphone permission is denied for video (records without audio)
  - Clear error messages directing users to Settings
  - Memory leak prevention with weak self references

- **Permission Improvements**
  - ✅ Checks current permission state before requesting
  - ✅ No crashes when permissions are denied
  - ✅ Specific error messages for each permission type
  - ✅ Handles previously denied permissions correctly
  - ✅ Requests both camera and microphone for video recording
  - ✅ Continues video recording without audio if microphone is denied (iOS)

### Security
- Proper FileProvider configuration for Android
- Secure temporary file storage
- No external storage permissions needed for captures
- Privacy-respecting permission requests with clear descriptions

### Documentation
- Complete API documentation in README.md
- Detailed permissions guide in PERMISSIONS.md
- Usage examples in EXAMPLE.md
- Setup and development guide in SETUP.md
- Migration summary from Cordova in MIGRATION_SUMMARY.md

### Differences from Cordova Plugin
- Modern Promise-based API instead of callbacks
- Full TypeScript support with type definitions
- Better error handling with descriptive messages
- No dependency on cordova-plugin-file
- Android 13+ granular permissions support
- iOS permission state verification before requesting
- No crashes on permission denial
- Cleaner, more maintainable code structure

## Known Issues
None reported. All major issues from the Cordova version have been resolved.

## Migration from Cordova
See [MIGRATION_SUMMARY.md](./MIGRATION_SUMMARY.md) for detailed migration instructions.

## Compatibility

| Platform | Minimum Version | Maximum Tested Version | Status |
|----------|----------------|------------------------|--------|
| Android  | API 22 (5.1)   | API 35 (Android 14)    | ✅ Fully Supported |
| iOS      | 13.0           | 18.0                   | ✅ Fully Supported |
| Web      | Modern browsers| Latest                 | ✅ Basic Support |

## Breaking Changes from Cordova
- API uses Promises instead of callbacks
- Must use `import { MediaCapture }` instead of `navigator.device.capture`
- Error objects have different structure (more informative)
- Permission denied returns clear error message instead of error code
