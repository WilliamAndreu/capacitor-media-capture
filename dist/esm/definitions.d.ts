export interface MediaCapturePlugin {
    /**
     * Launch audio recorder application for recording audio clip(s).
     *
     * @param options - Options for audio capture
     * @returns Promise with array of captured media files
     */
    captureAudio(options?: CaptureAudioOptions): Promise<MediaFileResult>;
    /**
     * Launch camera application for taking image(s).
     *
     * @param options - Options for image capture
     * @returns Promise with array of captured media files
     */
    captureImage(options?: CaptureImageOptions): Promise<MediaFileResult>;
    /**
     * Launch device camera application for recording video(s).
     *
     * @param options - Options for video capture
     * @returns Promise with array of captured media files
     */
    captureVideo(options?: CaptureVideoOptions): Promise<MediaFileResult>;
    /**
     * Get format data for a media file.
     *
     * @param mediaFile - The media file to get format data for
     * @returns Promise with media file format data
     */
    getFormatData(mediaFile: MediaFile): Promise<MediaFileData>;
}
/**
 * Options for capturing audio
 */
export interface CaptureAudioOptions {
    /**
     * Maximum number of audio clips to capture. Default is 1.
     * On iOS, only 1 is supported.
     */
    limit?: number;
    /**
     * Maximum duration of an audio clip in seconds.
     */
    duration?: number;
}
/**
 * Options for capturing images
 */
export interface CaptureImageOptions {
    /**
     * Maximum number of images to capture. Default is 1.
     */
    limit?: number;
    /**
     * The quality of saved image in range [0-100]. Default is 100.
     * Android only.
     */
    quality?: number;
}
/**
 * Options for capturing video
 */
export interface CaptureVideoOptions {
    /**
     * Maximum number of video clips to capture. Default is 1.
     * On iOS, only 1 is supported.
     */
    limit?: number;
    /**
     * Maximum duration of a video clip in seconds.
     */
    duration?: number;
    /**
     * The quality of saved video. 0 = low, 1 = high. Default is 1.
     * iOS only.
     */
    quality?: number;
}
/**
 * Encapsulates properties of a media capture file
 */
export interface MediaFile {
    /**
     * The name of the file, without path information
     */
    name: string;
    /**
     * The full path of the file, including the name
     */
    fullPath: string;
    /**
     * The MIME type of the file
     */
    type: string;
    /**
     * The date and time that the file was last modified
     */
    lastModifiedDate: number;
    /**
     * The size of the file, in bytes
     */
    size: number;
}
/**
 * Encapsulates format information about a media file
 */
export interface MediaFileData {
    /**
     * The actual format of the audio and video content
     */
    codecs?: string;
    /**
     * The average bitrate of the content. The value is zero for images.
     */
    bitrate?: number;
    /**
     * The height of the image or video in pixels. The value is zero for audio clips.
     */
    height?: number;
    /**
     * The width of the image or video in pixels. The value is zero for audio clips.
     */
    width?: number;
    /**
     * The length of the video or sound clip in seconds. The value is zero for images.
     */
    duration?: number;
}
/**
 * Result returned from capture methods
 */
export interface MediaFileResult {
    /**
     * Array of captured media files
     */
    files: MediaFile[];
}
/**
 * Error codes for media capture
 */
export declare enum CaptureError {
    /**
     * Camera or microphone failed to capture image or sound
     */
    CAPTURE_INTERNAL_ERR = 0,
    /**
     * Camera application or audio capture application is currently serving other capture request
     */
    CAPTURE_APPLICATION_BUSY = 1,
    /**
     * Invalid use of the API (e.g. limit parameter has value less than one)
     */
    CAPTURE_INVALID_ARGUMENT = 2,
    /**
     * User exited camera application or audio capture application before capturing anything
     */
    CAPTURE_NO_MEDIA_FILES = 3,
    /**
     * The requested capture operation is not supported
     */
    CAPTURE_NOT_SUPPORTED = 20,
    /**
     * Permission denied
     */
    CAPTURE_PERMISSION_DENIED = 21
}
