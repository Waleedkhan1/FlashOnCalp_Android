package com.personal.flashonclap.light.status.flash;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    boolean light;
    SurfaceTexture mPreviewTexture = new SurfaceTexture(0);

    private Button clap_flash, rate_app, share_app, flash, ivFlash;

    private boolean mRunning = false;

    private static final int POLL_INTERVAL = 300;

    private CameraManager mCameraManager;
    private SoundMeter mSensor;
    private String mCameraId;
    private SoundLevelView mDisplay;
    private int mThreshold;
    private PowerManager.WakeLock mWakeLock;
    private Handler mHandler = new Handler();

//    String moreAppStore = "https://play.google.com/store/apps/developer?id=Ori+Tech";
    //String appAdd = "https://play.google.com/store/apps/details?id=com.hungrybird.lockscreenapp.unlock";

    boolean On;
    private Camera camera;
    private boolean isFlashOn = false;
    private boolean hasFlash;
    Parameters params;
    private int timerValue = 30000;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1;

    InterstitialAd interstitialAd;
    Intent i1, i2;

    final private int PERMISSION_ALL = 1;
    Typeface font;

    private FrameLayout ad_view_container;
    private AdView adView;

    Intent mServiceIntent;
    private MyService mSensorService;
    private Boolean timer = false;

    //============== Native Code Start Here ============== //
    private CheckBox mRequestAppInstallAds;
    private CheckBox mRequestContentAds;
    private CheckBox mStartVideoAdsMuted;
    private TextView mVideoStatus;
    private Button mRefresh;
    //============== Native Code End Here =============== //


    // Create runnable thread to Monitor Voice

//    private Runnable mSleepTask = new Runnable() {
//        public void run() {
//            // Log.i("Noise", "runnable mSleepTask");
//
//            start();
////            turnOnflash();
//
//        }
//    };
//
//    public Runnable mPollTask = new Runnable() {
//        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//        public void run() {
//
//            double amp = mSensor.getAmplitude();
//            // Log.i("Noise", "runnable mPollTask");
//            updateDisplay("Monitoring Voice...", amp);
//
//            if ((amp > mThreshold)) {
//                if (isFlashOn) {
//                    // turn off flash
//                    isFlashOn = false;
//                    turnOffflash();
//                } else {
//                    // turn on flash
//                    isFlashOn = true;
//                    turnOnflash();
//                }
//                //callForHelp();
//                // Log.i("Noise", "==== onCreate ===");
//
//            }
//
//            // Runnable(mPollTask) will again execute after POLL_INTERVAL
//            mHandler.postDelayed(mPollTask, POLL_INTERVAL);
//
//        }
//    };

    @SuppressLint({"MissingPermission", "InvalidWakeLockTag"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        ad_view_container = findViewById(R.id.ad_view_container);
        // Step 1 - Create an AdView and set the ad unit ID on it.
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
        ad_view_container.addView(adView);
        loadBanner();

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.admob_interstitial));
        interstitialAd.loadAd(new AdRequest.Builder().build());

        flash = findViewById(R.id.ivflash);
        clap_flash = findViewById(R.id.flash_clap);
        ivFlash = findViewById(R.id.ivFlash);
        rate_app = findViewById(R.id.rate_app);
        share_app = findViewById(R.id.share_app);

        flash.setOnClickListener(this);
        clap_flash.setOnClickListener(this);
        rate_app.setOnClickListener(this);
        share_app.setOnClickListener(this);
        ivFlash.setOnClickListener(this);

        font = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

//        mSensor = new SoundMeter();
//
//        mThreshold = 10;
//
//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "NoiseAlert");

//        if (!hasFlash) {
//            // device doesn't support flash
//            // Show alert message and close the application
//            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
//                    .create();
//            alert.setTitle("Error");
//            alert.setMessage("Sorry, your device doesn't support flash light!");
//            alert.setButton("OK", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {
//                    // closing the application
//                    finish();
//                }
//            });
//            alert.show();
//            return;
//        }


//        String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.WAKE_LOCK,
//                Manifest.permission.RECORD_AUDIO
//                , Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET};
//
//        if (!hasPermissions(this, PERMISSIONS)) {
//            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
//
//        }

//        if (!mRunning) {
//            mRunning = true;
//            start();
////             turnOnflash();
//        }


        String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WAKE_LOCK,
                Manifest.permission.CAMERA
                , Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET,
                Manifest.permission.WRITE_SETTINGS


        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        getCamera();

    }

    //for
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.ivflash:

                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                    interstitialAd.setAdListener(new AdListener() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            interstitialAd.loadAd(new AdRequest.Builder().build());
                            startActivity(new Intent(MainActivity.this, FlashLightActivity.class));
                        }
                    });
                } else {
                    startActivity(new Intent(MainActivity.this, FlashLightActivity.class));
                }

                break;

            case R.id.flash_clap:
//                mSensorService = new MyService();
//
//                mServiceIntent = new Intent(this, mSensorService.getClass());
//                startService(mServiceIntent);

               /* if (mInterstitialAd_Admob.isLoaded()){
                    mInterstitialAd_Admob.show();
                    requestNewInterstitial();
                }*/
                if (camera != null) {
                    camera.release();
                    camera = null;
                }

                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                    interstitialAd.setAdListener(new AdListener() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            interstitialAd.loadAd(new AdRequest.Builder().build());
                            i1 = new Intent(MainActivity.this, NoiseAlert.class);
//                            i1 = new Intent(MainActivity.this, DetectorActivity.class);
                            startActivity(i1);
                        }
                    });
                } else {
                    i1 = new Intent(MainActivity.this, NoiseAlert.class);
//                    i1 = new Intent(MainActivity.this, DetectorActivity.class);
                    startActivity(i1);
                }

                break;
            case R.id.rate_app:

                try {
                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id="
                                    + getPackageName())));
                }

                break;

            case R.id.share_app:

                Intent j = new Intent(Intent.ACTION_SEND);
                j.setType("text/plain");
                j.putExtra(Intent.EXTRA_SUBJECT, "Flash Light On Clap");
                String sAux = "\nLet me recommend you this application\n\n";
                sAux = sAux + "https://play.google.com/store/apps/details?id=com.itapp.flashlightonclap.led.turnon.flash\n\n";
                j.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(j, "choose one"));

                break;

            case R.id.ivFlash:
                Intent i = new Intent(MainActivity.this, FlashEffectActivity.class);
                startActivity(i);
                break;


        }
    }
//to start foreground service

    public void startService() {
        Intent serviceIntent = new Intent(this, MyService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
        ContextCompat.startForegroundService(this, serviceIntent);
    }



//    public void start() {
//        // Log.i("Noise", "==== start ===");
//
//        mSensor.start();
//        if (!mWakeLock.isHeld()) {
//            mWakeLock.acquire();
////
//        }
//
//        // Noise monitoring start
//        // Runnable(mPollTask) will execute after POLL_INTERVAL
//        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
//
//    }

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public void turnOffflash(){
//
//        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try{
//            assert mCameraManager != null;
//            mCameraId = mCameraManager.getCameraIdList()[0];
//            mCameraManager.setTorchMode(mCameraId, false);
//            Toast.makeText(MainActivity.this, "FlashLight is OFF", Toast.LENGTH_SHORT).show();
//        }
//        catch(CameraAccessException e){
//            Log.e("Camera Problem", "Cannot turn off camera flashlight");
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public void turnOnflash(){
//
//        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try{
//            assert mCameraManager != null;
//            mCameraId = mCameraManager.getCameraIdList()[0];
//            mCameraManager.setTorchMode(mCameraId, true);
//            Toast.makeText(MainActivity.this, "FlashLight is ON", Toast.LENGTH_SHORT).show();
//        }
//        catch(CameraAccessException e){
//            Log.e("Camera Problem", "Cannot turn on camera flashlight");
//        }
//
//    }


    private void updateDisplay(String status, double signalEMA) {
        //	mStatusView.setText(status);
        //
//        mDisplay.setLevel((int) signalEMA, mThreshold);

    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, MyService.class);
        stopService(serviceIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted -code

                } else {
                    Toast.makeText(getApplicationContext(), "Please Allow Permission", Toast.LENGTH_LONG).show();

                    finish();


                }
            }
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    /*
     * Get the camera
     */
    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                // Log.e("Camera Error. Failed to Open. Error: ", e.getMessage());
            }
        }
    }

    private void loadAdd() {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
            interstitialAd.setAdListener(new AdListener() {
                @SuppressLint("MissingPermission")
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    interstitialAd.loadAd(new AdRequest.Builder().build());
                }
            });
        }
    }

    private void turnOnFlash1() {

        if (!On && light) {
            if (camera == null || params == null) {
                return;
            }
            try {


                params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(params);
                try {
                    camera.setPreviewTexture(mPreviewTexture);
                } catch (IOException ex) {
                    // Ignore
                }

                camera.startPreview();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //                   swch.setBackgroundResource(R.drawable.button_onn);
                        Log.i("inlighton", "Light On");
                    }
                });


                On = true;

            } catch (Exception e) {
                Log.i("yourerro", "Error = " + e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.e("tttttttttttt", "onDestroy");
        if (mServiceIntent != null) {
            stopService(mServiceIntent);
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // on stop release the camera
        if (camera != null) {
            camera.release();
            camera = null;
        }

        // on pause turn off the flash
//        turnOffFlash();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (timer == false){
            loadAdd();
//        }

        new CountDownTimer(timerValue, 1000) {

            public void onTick(long millisUntilFinished) {
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
//                loadAdd();
                timer = true;
            }

        }.start();

        // on resume turn on the flash
//turnOnFlash1();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // on starting the app get the camera params
        getCamera();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBackPressed() {
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Exit")
                .setMessage("Are you sure want to exit ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                        startMain.addCategory(Intent.CATEGORY_HOME);
                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(startMain);


                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).create();

        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.item_alert_dialog, null);

        //============== Native Code Start Here ============== //
        MobileAds.initialize(MainActivity.this, getResources().getString(R.string.native_ad_id));
        mRefresh = dialogView.findViewById(R.id.btn_refresh);
        mRequestAppInstallAds = dialogView.findViewById(R.id.cb_appinstall);
        mRequestContentAds = dialogView.findViewById(R.id.cb_content);
        mStartVideoAdsMuted = dialogView.findViewById(R.id.cb_start_muted);
        mVideoStatus = dialogView.findViewById(R.id.tv_video_status);

        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshAd(mRequestAppInstallAds.isChecked(),
                        mRequestContentAds.isChecked(), dialogView);
            }
        });

        refreshAd(mRequestAppInstallAds.isChecked(),
                mRequestContentAds.isChecked(), dialogView);

        refreshAd(true, true, dialogView);
        // ============== Native Code End Here =============== //
        dialog.setView(dialogView);

        dialog.show();

    }

    @SuppressLint("MissingPermission")
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

    //============== Native Code Start Here ============== //
    private void populateAppInstallAdView(NativeAppInstallAd nativeAppInstallAd,
                                          NativeAppInstallAdView adView) {
        VideoController vc = nativeAppInstallAd.getVideoController();

        vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
            public void onVideoEnd() {
                mRefresh.setEnabled(true);
                mVideoStatus.setText(getResources().getString(R.string.native_ad_id));
                super.onVideoEnd();
            }
        });

        adView.setHeadlineView(adView.findViewById(R.id.appinstall_headline));
        adView.setBodyView(adView.findViewById(R.id.appinstall_body));
        adView.setCallToActionView(adView.findViewById(R.id.appinstall_call_to_action));
        adView.setIconView(adView.findViewById(R.id.appinstall_app_icon));
        adView.setPriceView(adView.findViewById(R.id.appinstall_price));
        adView.setStarRatingView(adView.findViewById(R.id.appinstall_stars));
        adView.setStoreView(adView.findViewById(R.id.appinstall_store));

        // Some assets are guaranteed to be in every NativeAppInstallAd.
        if (nativeAppInstallAd.getHeadline() != null)
            ((TextView) adView.getHeadlineView()).setText(nativeAppInstallAd.getHeadline());

        if (nativeAppInstallAd.getBody() != null)
            ((TextView) adView.getBodyView()).setText(nativeAppInstallAd.getBody());

        if (nativeAppInstallAd.getCallToAction() != null)
            ((Button) adView.getCallToActionView()).setText(nativeAppInstallAd.getCallToAction());

        if (nativeAppInstallAd.getIcon().getDrawable() != null)
            ((ImageView) adView.getIconView()).setImageDrawable(nativeAppInstallAd.getIcon().getDrawable());

        MediaView mediaView = adView.findViewById(R.id.appinstall_media);
        ImageView mainImageView = adView.findViewById(R.id.appinstall_image);

        if (vc.hasVideoContent()) {
            adView.setMediaView(mediaView);
            mainImageView.setVisibility(View.GONE);
            mVideoStatus.setText(String.format(Locale.getDefault(),
                    "Video status: Ad contains a %.2f:1 video asset.",
                    vc.getAspectRatio()));
        } else {
            adView.setImageView(mainImageView);
            mediaView.setVisibility(View.GONE);

            List<NativeAd.Image> images = nativeAppInstallAd.getImages();
            if (images.size() > 0) {
                mainImageView.setImageDrawable(images.get(0).getDrawable());
            }

            mRefresh.setEnabled(true);
            mVideoStatus.setText(getResources().getString(R.string.native_ad_id));
        }

        if (nativeAppInstallAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAppInstallAd.getPrice());
        }

        if (nativeAppInstallAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAppInstallAd.getStore());
        }

        if (nativeAppInstallAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAppInstallAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAppInstallAd);
    }

    private void populateContentAdView(NativeContentAd nativeContentAd,
                                       NativeContentAdView adView) {
        mVideoStatus.setText(getResources().getString(R.string.native_ad_id));
        mRefresh.setEnabled(true);

        adView.setHeadlineView(adView.findViewById(R.id.contentad_headline));
        adView.setImageView(adView.findViewById(R.id.contentad_image));
        adView.setBodyView(adView.findViewById(R.id.contentad_body));
        adView.setCallToActionView(adView.findViewById(R.id.contentad_call_to_action));
        adView.setLogoView(adView.findViewById(R.id.contentad_logo));
        adView.setAdvertiserView(adView.findViewById(R.id.contentad_advertiser));

        // Some assets are guaranteed to be in every NativeContentAd.
        ((TextView) adView.getHeadlineView()).setText(nativeContentAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeContentAd.getBody());
        ((TextView) adView.getCallToActionView()).setText(nativeContentAd.getCallToAction());
        ((TextView) adView.getAdvertiserView()).setText(nativeContentAd.getAdvertiser());

        List<NativeAd.Image> images = nativeContentAd.getImages();

        if (images.size() > 0) {
            ((ImageView) adView.getImageView()).setImageDrawable(images.get(0).getDrawable());
        }
        NativeAd.Image logoImage = nativeContentAd.getLogo();

        if (logoImage == null) {
            adView.getLogoView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getLogoView()).setImageDrawable(logoImage.getDrawable());
            adView.getLogoView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeContentAd);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Log.i("Noise", "==== onStop ===");

        if (camera != null) {
            camera.release();
            camera = null;
        }

        // Stop noise monitoring
//        stop();


    }

//    private void stop() {
//        Log.i("Noise", "==== Stop Noise Monitoring===");
//        if (mWakeLock.isHeld()) {
//            mWakeLock.release();
//        }
//        mHandler.removeCallbacks(mSleepTask);
//        mHandler.removeCallbacks(mPollTask);
//        mSensor.stop();
//        mDisplay.setLevel(0, 0);
//        updateDisplay("stopped...", 0.0);
//        mRunning = false;
//
//    }

    @SuppressLint("MissingPermission")
    private void refreshAd(boolean requestAppInstallAds, boolean requestContentAds, final View view) {
        if (!requestAppInstallAds && !requestContentAds) {
            Toast.makeText(MainActivity.this, "At least one ad format must be checked to request an ad.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mRefresh.setEnabled(false);

        AdLoader.Builder builder = new AdLoader.Builder(MainActivity.this, getResources().getString(R.string.native_ad_id));

        if (requestAppInstallAds) {
            builder.forAppInstallAd(new NativeAppInstallAd.OnAppInstallAdLoadedListener() {
                @Override
                public void onAppInstallAdLoaded(NativeAppInstallAd ad) {
                    FrameLayout frameLayout = view.findViewById(R.id.fl_adplaceholder);
                    @SuppressLint("InflateParams") NativeAppInstallAdView adView = (NativeAppInstallAdView) getLayoutInflater()
                            .inflate(R.layout.ad_app_installscreen_saver_new_, null);
                    populateAppInstallAdView(ad, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                }
            });
        }

        if (requestContentAds) {
            builder.forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
                @Override
                public void onContentAdLoaded(NativeContentAd ad) {
                    FrameLayout frameLayout = view.findViewById(R.id.fl_adplaceholder);
                    @SuppressLint("InflateParams") NativeContentAdView adView = (NativeContentAdView) getLayoutInflater()
                            .inflate(R.layout.ad_content, null);
                    populateContentAdView(ad, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                }
            });
        }

        VideoOptions videoOptions;
        videoOptions = new VideoOptions.Builder()
                .setStartMuted(mStartVideoAdsMuted.isChecked())
                .build();
//		VideoOptions videoOptions = new VideoOptions.Builder()
//				.setStartMuted(true)
//				.build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
//				mRefresh.setEnabled(true);
//                Toast.makeText(NavigationDrawer.this, "Failed to load native ad: "
//                        + errorCode,  Toast.LENGTH_SHORT).show();
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());

        mVideoStatus.setText("");
    }
    //============== Native Code End Here ============== //

}
