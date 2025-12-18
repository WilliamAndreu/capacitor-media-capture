/**
 * Error codes for media capture
 */
export var CaptureError;
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
})(CaptureError || (CaptureError = {}));
