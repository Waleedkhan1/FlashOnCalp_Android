package com.personal.flashonclap.light.status.flash;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class FlashLightActivity extends AppCompatActivity {

    private ImageView flashSwitchImg;
    private Button flashButton;
    private String mCameraId;
    private CameraManager mCameraManager;


    private boolean isFlashOn = false;
    private boolean hasFlash = false;

    private FrameLayout ad_view_container;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_light);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        ad_view_container = findViewById(R.id.ad_view_container);
        // Step 1 - Create an AdView and set the ad unit ID on it.
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
        ad_view_container.addView(adView);
        loadBanner();

//        mAdView.setAdListener(new AdListener() {
//            @Override
//            public void onAdLoaded() {
//                mAdView.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAdFailedToLoad(int errorCode) {
//                mAdView.setVisibility(View.GONE);
//            }
//        });

         hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

//        if (!isFlashAvailable) {
//            showNoFlashError();
//        }

//        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            mCameraId = mCameraManager.getCameraIdList()[0];
//
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }

        flashSwitchImg = findViewById(R.id.flashSwitchImg);

        flashButton = findViewById(R.id.flashSwitch);

        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasFlash){
                    if (isFlashOn){
                        isFlashOn = false;
                        flashButton.setBackgroundResource(R.drawable.btn_flash_off);
                        flashSwitchImg.setBackgroundResource(R.drawable.img_flash_off);
                        turnOffflash();
                    }
                    else {
                        isFlashOn = true;
                        flashButton.setBackgroundResource(R.drawable.btn_flash_on);
                        flashSwitchImg.setBackgroundResource(R.drawable.img_flash_on);
                        turnOnflash();
                    }
                }
                else {
                    Toast.makeText(FlashLightActivity.this, "Device does'nt support Flash", Toast.LENGTH_SHORT).show();
                }
            }
        });

//
//    public void showNoFlashError() {
//        AlertDialog alert = new AlertDialog.Builder(this)
//                .create();
//        alert.setTitle("Oops!");
//        alert.setMessage("Flash not available in this device...");
//        alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                finish();
//            }
//        });
//        alert.show();
//    }
//
//    public void switchFlashLight(boolean status) {
//        try {
//            mCameraManager.setTorchMode(mCameraId, status);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//   }
//
//    private void getCamera() {
//        if (camera == null) {
//            try {
//
//                camera = Camera.open();
//                params = camera.getParameters();
//                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//            } catch (RuntimeException e) {
//                // Log.e("Camera Error. Failed to Open. Error: ", e.getMessage());
//            }
//        }
//    }
//
//    private void turnOnFlash() {
//
//        Log.d("flashLog", "Turn On Flash Called");
//
//        if (!isFlashOn) {
//            if (camera == null || params == null) {
//                getCamera();
//                return;
//            }
//            // play sound
//            params = camera.getParameters();
//
//            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//            camera.setParameters(params);
//            camera.startPreview();
//            isFlashOn = true;
//
//            flashSwitchImg.setBackgroundResource(R.drawable.img_flash_on);
//            flashSwitch.setBackgroundResource(R.drawable.btn_flash_on);
//
//        }
//    }
//
//    private void turnOffFlash() {
//
//        Log.d("flashLog", "Turn Off Flash Called");
//
//        if (isFlashOn) {
//            if (camera == null || params == null) {
//                return;
//            }
//            // play sound
//
//
//            params = camera.getParameters();
//            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//            camera.setParameters(params);
//            camera.stopPreview();
//            isFlashOn = false;
//
//            flashSwitchImg.setBackgroundResource(R.drawable.img_flash_off);
//            flashSwitch.setBackgroundResource(R.drawable.btn_flash_off);
//
//        }
//    }
        }
    private void turnOnflash(){

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            assert mCameraManager != null;
            mCameraId = mCameraManager.getCameraIdList()[0];
            mCameraManager.setTorchMode(mCameraId, true);
            Toast.makeText(FlashLightActivity.this, "FlashLight is ON", Toast.LENGTH_SHORT).show();
        }
        catch(CameraAccessException e){
            Log.e("Camera Problem", "Cannot turn on camera flashlight");
        }

    }
    private void turnOffflash(){

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            assert mCameraManager != null;
            mCameraId = mCameraManager.getCameraIdList()[0];
            mCameraManager.setTorchMode(mCameraId, false);
            Toast.makeText(FlashLightActivity.this, "FlashLight is OFF", Toast.LENGTH_SHORT).show();
        }
        catch(CameraAccessException e){
            Log.e("Camera Problem", "Cannot turn off camera flashlight");
        }
    }
    private void loadBanner() {
        // Create an ad request. Check your logcat output for the hashed device ID
        // to get test ads on a physical device, e.g.,
        // "Use AdRequest.Builder.addTestDevice("ABCDE0123") to get test ads on this
        // device."
        AdRequest adRequest =
                new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                        .build();

        AdSize adSize = getAdSize();
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);

        // Step 5 - Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

}
