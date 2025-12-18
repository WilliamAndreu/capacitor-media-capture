/*
   Licensed to the Apache Software Foundation (ASF) under one
   or more contributor license agreements.  See the NOTICE file
   distributed with this work for additional information
   regarding copyright ownership.  The ASF licenses this file
   to you under the Apache License, Version 2.0 (the
   "License"); you may not use this file except in compliance
   with the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing,
   software distributed under the License is distributed on an
   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
   KIND, either express or implied.  See the License for the
   specific language governing permissions and limitations
   under the License.
*/
package com.capacitor.mediacapture;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.activity.result.ActivityResult;
import androidx.core.content.FileProvider;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@CapacitorPlugin(
    name = "MediaCapture",
    permissions = {
        @Permission(strings = { Manifest.permission.CAMERA }, alias = MediaCapturePlugin.CAMERA),
        @Permission(strings = { Manifest.permission.RECORD_AUDIO }, alias = MediaCapturePlugin.AUDIO),
        @Permission(
            strings = {
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE
            },
            alias = MediaCapturePlugin.MEDIA
        )
    }
)
public class MediaCapturePlugin extends Plugin {

    static final String CAMERA = "camera";
    static final String AUDIO = "audio";
    static final String MEDIA = "media";
    static final String CAMERA_AND_AUDIO = "cameraAndAudio";

    private static final String VIDEO_3GPP = "video/3gpp";
    private static final String VIDEO_MP4 = "video/mp4";
    private static final String AUDIO_3GPP = "audio/3gpp";
    private static final String[] AUDIO_TYPES = new String[] { "audio/3gpp", "audio/aac", "audio/amr", "audio/wav" };
    private static final String IMAGE_JPEG = "image/jpeg";
    private static final String TAG = "MediaCapture";

    private static final String CAPTURE_AUDIO = "captureAudioResult";
    private static final String CAPTURE_IMAGE = "captureImageResult";
    private static final String CAPTURE_VIDEO = "captureVideoResult";

    private String audioAbsolutePath;
    private String imageAbsolutePath;
    private String videoAbsolutePath;
    private int currentLimit = 1;
    private int currentCount = 0;
    private JSArray currentResults = new JSArray();

    @PluginMethod
    public void captureAudio(PluginCall call) {
        currentLimit = call.getInt("limit", 1);
        currentCount = 0;
        currentResults = new JSArray();

        if (getPermissionState(AUDIO) != PermissionState.GRANTED) {
            requestPermissionForAlias(AUDIO, call, "audioPermissionsCallback");
        } else {
            startAudioCapture(call);
        }
    }

    @PermissionCallback
    private void audioPermissionsCallback(PluginCall call) {
        if (getPermissionState(AUDIO) == PermissionState.GRANTED) {
            startAudioCapture(call);
        } else {
            call.reject("Audio recording permission denied. Please enable microphone access in your device settings.");
        }
    }

    private void startAudioCapture(PluginCall call) {
        try {
            Intent intent = new Intent(android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION);
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
            String fileName = "cdv_media_capture_audio_" + timeStamp + ".m4a";
            File audio = new File(getTempDirectoryPath(), fileName);

            Uri audioUri = FileProvider.getUriForFile(
                getContext(),
                getContext().getPackageName() + ".fileprovider",
                audio
            );
            this.audioAbsolutePath = audio.getAbsolutePath();
            Log.d(TAG, "Recording audio and saving to: " + this.audioAbsolutePath);

            startActivityForResult(call, intent, CAPTURE_AUDIO);
        } catch (ActivityNotFoundException ex) {
            call.reject("No Activity found to handle Audio Capture", ex);
        }
    }

    @PluginMethod
    public void captureImage(PluginCall call) {
        currentLimit = call.getInt("limit", 1);
        currentCount = 0;
        currentResults = new JSArray();

        if (getPermissionState(CAMERA) != PermissionState.GRANTED) {
            requestPermissionForAlias(CAMERA, call, "imagePermissionsCallback");
        } else {
            startImageCapture(call);
        }
    }

    @PermissionCallback
    private void imagePermissionsCallback(PluginCall call) {
        if (getPermissionState(CAMERA) == PermissionState.GRANTED) {
            startImageCapture(call);
        } else {
            call.reject("Camera permission denied. Please enable camera access in your device settings.");
        }
    }

    private void startImageCapture(PluginCall call) {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String fileName = "cdv_media_capture_image_" + timeStamp + ".jpg";
        File image = new File(getTempDirectoryPath(), fileName);

        Uri imageUri = FileProvider.getUriForFile(
            getContext(),
            getContext().getPackageName() + ".fileprovider",
            image
        );
        this.imageAbsolutePath = image.getAbsolutePath();
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        Log.d(TAG, "Taking a picture and saving to: " + this.imageAbsolutePath);

        startActivityForResult(call, intent, CAPTURE_IMAGE);
    }

    @PluginMethod
    public void captureVideo(PluginCall call) {
        currentLimit = call.getInt("limit", 1);
        currentCount = 0;
        currentResults = new JSArray();

        // Check if both camera and audio permissions are granted
        boolean cameraGranted = getPermissionState(CAMERA) == PermissionState.GRANTED;
        boolean audioGranted = getPermissionState(AUDIO) == PermissionState.GRANTED;

        if (!cameraGranted || !audioGranted) {
            // Request both permissions for video recording with audio
            String[] permissions = new String[2];
            permissions[0] = CAMERA;
            permissions[1] = AUDIO;
            requestPermissionForAliases(permissions, call, "videoPermissionsCallback");
        } else {
            startVideoCapture(call);
        }
    }

    @PermissionCallback
    private void videoPermissionsCallback(PluginCall call) {
        boolean cameraGranted = getPermissionState(CAMERA) == PermissionState.GRANTED;
        boolean audioGranted = getPermissionState(AUDIO) == PermissionState.GRANTED;

        if (cameraGranted && audioGranted) {
            startVideoCapture(call);
        } else if (!cameraGranted) {
            call.reject("Camera permission denied. Please enable camera access in your device settings.");
        } else {
            call.reject("Microphone permission denied. Video will be recorded without audio. Please enable microphone access for video with sound.");
        }
    }

    private void startVideoCapture(PluginCall call) {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String fileName = "cdv_media_capture_video_" + timeStamp + ".mp4";
        File movie = new File(getTempDirectoryPath(), fileName);

        Uri videoUri = FileProvider.getUriForFile(
            getContext(),
            getContext().getPackageName() + ".fileprovider",
            movie
        );
        this.videoAbsolutePath = movie.getAbsolutePath();
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, videoUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        Log.d(TAG, "Recording a video and saving to: " + this.videoAbsolutePath);

        int duration = call.getInt("duration", 0);
        int quality = call.getInt("quality", 1);
        if (duration > 0) {
            intent.putExtra("android.intent.extra.durationLimit", duration);
        }
        intent.putExtra("android.intent.extra.videoQuality", quality);

        startActivityForResult(call, intent, CAPTURE_VIDEO);
    }

    @PluginMethod
    public void getFormatData(PluginCall call) {
        String filePath = call.getString("fullPath");
        String mimeType = call.getString("type");

        if (filePath == null) {
            call.reject("File path is required");
            return;
        }

        try {
            JSObject result = getFormatDataForFile(filePath, mimeType);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("Error getting format data", e);
        }
    }

    @ActivityCallback
    private void captureAudioResult(PluginCall call, ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_CANCELED) {
            if (currentResults.length() > 0) {
                JSObject ret = new JSObject();
                ret.put("files", currentResults);
                call.resolve(ret);
            } else {
                call.reject("User cancelled");
            }
            return;
        }

        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent intent = result.getData();
            if (intent != null) {
                Uri uri = intent.getData();
                if (uri != null) {
                    copyAudioFile(uri, this.audioAbsolutePath);
                }
            }

            JSObject mediaFile = createMediaFile(this.audioAbsolutePath);
            if (mediaFile != null) {
                currentResults.put(mediaFile);
                currentCount++;

                if (currentCount >= currentLimit) {
                    JSObject ret = new JSObject();
                    ret.put("files", currentResults);
                    call.resolve(ret);
                } else {
                    startAudioCapture(call);
                }
            } else {
                call.reject("Error creating media file");
            }
        } else {
            call.reject("Capture failed");
        }
    }

    @ActivityCallback
    private void captureImageResult(PluginCall call, ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_CANCELED) {
            if (currentResults.length() > 0) {
                JSObject ret = new JSObject();
                ret.put("files", currentResults);
                call.resolve(ret);
            } else {
                call.reject("User cancelled");
            }
            return;
        }

        if (result.getResultCode() == Activity.RESULT_OK) {
            JSObject mediaFile = createMediaFile(this.imageAbsolutePath);
            if (mediaFile != null) {
                currentResults.put(mediaFile);
                currentCount++;

                if (currentCount >= currentLimit) {
                    JSObject ret = new JSObject();
                    ret.put("files", currentResults);
                    call.resolve(ret);
                } else {
                    startImageCapture(call);
                }
            } else {
                call.reject("Error creating media file");
            }
        } else {
            call.reject("Capture failed");
        }
    }

    @ActivityCallback
    private void captureVideoResult(PluginCall call, ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_CANCELED) {
            if (currentResults.length() > 0) {
                JSObject ret = new JSObject();
                ret.put("files", currentResults);
                call.resolve(ret);
            } else {
                call.reject("User cancelled");
            }
            return;
        }

        if (result.getResultCode() == Activity.RESULT_OK) {
            JSObject mediaFile = createMediaFile(this.videoAbsolutePath);
            if (mediaFile != null) {
                currentResults.put(mediaFile);
                currentCount++;

                if (currentCount >= currentLimit) {
                    JSObject ret = new JSObject();
                    ret.put("files", currentResults);
                    call.resolve(ret);
                } else {
                    startVideoCapture(call);
                }
            } else {
                call.reject("Error creating media file");
            }
        } else {
            call.reject("Capture failed");
        }
    }

    private String getTempDirectoryPath() {
        File cache = new File(getContext().getCacheDir(), "com.capacitor.mediacapture");
        cache.mkdirs();
        return cache.getAbsolutePath();
    }

    private JSObject createMediaFile(String path) {
        File fp = new File(path);
        JSObject obj = new JSObject();

        try {
            obj.put("name", fp.getName());
            obj.put("fullPath", "file://" + fp.getAbsolutePath());
            obj.put("type", FileHelper.getMimeType(Uri.fromFile(fp), getContext()));
            obj.put("lastModifiedDate", fp.lastModified());
            obj.put("size", fp.length());
        } catch (Exception e) {
            Log.e(TAG, "Error creating media file object", e);
            return null;
        }

        return obj;
    }

    private JSObject getFormatDataForFile(String filePath, String mimeType) throws JSONException {
        Uri fileUrl = filePath.startsWith("file:") ? Uri.parse(filePath) : Uri.fromFile(new File(filePath));
        JSObject obj = new JSObject();

        // Setup defaults
        obj.put("height", 0);
        obj.put("width", 0);
        obj.put("bitrate", 0);
        obj.put("duration", 0);
        obj.put("codecs", "");

        // If the mimeType isn't set, try to determine it
        if (mimeType == null || mimeType.isEmpty() || "null".equals(mimeType)) {
            mimeType = FileHelper.getMimeType(fileUrl, getContext());
        }
        Log.d(TAG, "Mime type = " + mimeType);

        if (mimeType != null) {
            if (mimeType.equals(IMAGE_JPEG) || filePath.endsWith(".jpg")) {
                obj = getImageData(fileUrl, obj);
            } else if (Arrays.asList(AUDIO_TYPES).contains(mimeType)) {
                obj = getAudioVideoData(filePath, obj, false);
            } else if (mimeType.equals(VIDEO_3GPP) || mimeType.equals(VIDEO_MP4)) {
                obj = getAudioVideoData(filePath, obj, true);
            }
        }

        return obj;
    }

    private JSObject getImageData(Uri fileUrl, JSObject obj) throws JSONException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileUrl.getPath(), options);
        obj.put("height", options.outHeight);
        obj.put("width", options.outWidth);
        return obj;
    }

    private JSObject getAudioVideoData(String filePath, JSObject obj, boolean video) throws JSONException {
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(filePath);
            player.prepare();
            obj.put("duration", player.getDuration() / 1000);
            if (video) {
                obj.put("height", player.getVideoHeight());
                obj.put("width", player.getVideoWidth());
            }
        } catch (IOException e) {
            Log.d(TAG, "Error loading video file", e);
        } finally {
            player.release();
        }
        return obj;
    }

    private void copyAudioFile(Uri sourceUri, String destinationPath) {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = getContext().getContentResolver().openInputStream(sourceUri);
            if (input == null) {
                Log.e(TAG, "Unable to open input audio stream");
                return;
            }

            output = new FileOutputStream(destinationPath);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error copying audio file", e);
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (IOException ex) {
                Log.e(TAG, "Error closing streams", ex);
            }
        }
    }
}
