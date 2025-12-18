import { WebPlugin } from '@capacitor/core';
import type { MediaCapturePlugin, CaptureAudioOptions, CaptureImageOptions, CaptureVideoOptions, MediaFile, MediaFileResult, MediaFileData } from './definitions';
export declare class MediaCaptureWeb extends WebPlugin implements MediaCapturePlugin {
    captureAudio(options?: CaptureAudioOptions): Promise<MediaFileResult>;
    captureImage(options?: CaptureImageOptions): Promise<MediaFileResult>;
    captureVideo(options?: CaptureVideoOptions): Promise<MediaFileResult>;
    getFormatData(mediaFile: MediaFile): Promise<MediaFileData>;
}
