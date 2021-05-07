package edu.papan01.livestreamer;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, ImageReader.OnImageAvailableListener {
    private TextureView sf_view;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private List<Surface> sf;
    private ImageReader imageReader;
    private Handler handler;
    private Handler imageHandler;
    private CameraStream cameraStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkGrant();
        sf_view = findViewById(R.id.sf_view);
        sf_view.setSurfaceTextureListener(this);
        handler = createHandler("camera");
        imageHandler = createHandler("imageHandler");
        sf = new ArrayList<>();
        cameraStream = new CameraStream();
    }

    private Handler createHandler(String aaa) {
        HandlerThread thread = new HandlerThread(aaa);
        thread.start();
        return new Handler(thread.getLooper());
    }

    private void checkGrant() {
        String[] PERMISSION_LIST1 = {Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        for (String permission : PERMISSION_LIST1) {
            int grantResult = ActivityCompat.checkSelfPermission(getApplicationContext(), permission);
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, PERMISSION_LIST1, 0);
            }
        }
    }

    private void openCamera() throws CameraAccessException {
        cameraManager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.YUV_420_888, 4);
        imageReader.setOnImageAvailableListener(this, imageHandler);
        cameraManager.openCamera(cameraManager.getCameraIdList()[0], new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                sf_view.getSurfaceTexture().setDefaultBufferSize(1920, 1080);
                sf.add(new Surface(sf_view.getSurfaceTexture()));
                sf.add(imageReader.getSurface());
                cameraDevice = camera;
                try {
                    captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                    for (Surface s : sf) {
                        captureRequestBuilder.addTarget(s);
                    }
                    cameraDevice.createCaptureSession(sf, new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            cameraCaptureSessions = session;
                            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,
                                    CameraMetadata.CONTROL_MODE_AUTO);
                            try {

                                cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, handler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    }, handler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {

            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {

            }
        }, handler);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
            openCamera();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = reader.acquireLatestImage();
        if (image != null) {
            Log.d("LiveStreamer",image.getHeight() + "");
        }
        image.close();
    }
}
