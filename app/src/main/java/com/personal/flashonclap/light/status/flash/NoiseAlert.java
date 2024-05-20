package com.personal.flashonclap.light.status.flash;


import static android.content.pm.PackageManager.FEATURE_CAMERA_FLASH;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.personal.flashonclap.light.status.flash.Utils.MainStorageUtils;
import com.personal.flashonclap.light.status.flash.service.ShakeDetectService;


public class NoiseAlert extends Activity {
    /* constants */
    private static final int POLL_INTERVAL = 300;

    /**
     * running state
     **/
    private boolean mRunning = false;

    /**
     * config state
     **/
    private int mThreshold;

    private PowerManager.WakeLock mWakeLock;

    private Handler mHandler = new Handler();

    /* References to view elements */
    //private TextView mStatusView;
    private SoundLevelView mDisplay;

    /* data source */
    private SoundMeter mSensor;
    private String mCameraId;
    private CameraManager mCameraManager;
    private Camera camera;
    boolean isFlashOn = false;
    private boolean hasFlash = false;
    private boolean status = false;
    int value = 0;
    Parameters params;
    private ImageView ivSettings, imgOff, imgOn;
    Global global;

    Typeface font;

    private FrameLayout ad_view_container;
    private AdView adView;

    private MyService mSensorService;
    private MainStorageUtils mainStorageUtils = new MainStorageUtils();

    /******************
     * Define runnable thread again and again detect noise
     *********/

    private Runnable mSleepTask = new Runnable() {
        public void run() {
            // Log.i("Noise", "runnable mSleepTask");

            start();
//            turnOnflash();

        }
    };

    // Create runnable thread to Monitor Voice
    public Runnable mPollTask = new Runnable() {
        public void run() {

            double amp = mSensor.getAmplitude(NoiseAlert.this);
            // Log.i("Noise", "runnable mPollTask");
            updateDisplay("Monitoring Voice...", amp);

            if ((amp > mThreshold)) {
                if (isFlashOn) {
                    // turn off flash
                    isFlashOn = false;
                    turnOffflash();
                    status = true;
                } else {
                    // turn on flash
                    isFlashOn = true;
                    turnOnflash();
                    status = false;
                }
                //callForHelp();
                // Log.i("Noise", "==== onCreate ===");

            }

            // Runnable(mPollTask) will again execute after POLL_INTERVAL
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);

        }
    };
    MainActivity mainActivity = new MainActivity();
    /*********************************************************/

    /**
     * Called when the activity is first created.
     */
    @SuppressLint({"InvalidWakeLockTag", "NewApi", "UnsupportedChromeOsCameraSystemFeature"})
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Defined SoundLevelView in main.xml file
        setContentView(R.layout.main);

        //comments by asad

//        MobileAds.initialize(this, new OnInitializationCompleteListener() {
//            @Override
//            public void onInitializationComplete(InitializationStatus initializationStatus) {}
//        });

        global = new Global();

        startService(new Intent(this, ShakeDetectService.class));

        startService(new Intent(this, MyService.class));

        ad_view_container = findViewById(R.id.ad_view_container);
        // Step 1 - Create an AdView and set the ad unit ID on it.
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
        ad_view_container.addView(adView);
        loadBanner();

        //mStatusView = (TextView) findViewById(R.id.status);

        // Used to record voice
        mSensor = new SoundMeter();
        mDisplay = findViewById(R.id.volume);
        ivSettings = findViewById(R.id.ivSettings);
        imgOff = findViewById(R.id.imgOff);
        imgOn = findViewById(R.id.imgOn);

        font = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "NoiseAlert");

        /*
         * First check if device is supporting flashlight or not
         */
        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(FEATURE_CAMERA_FLASH);

        imgOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFlashOn = false;
                imgOff.setVisibility(View.GONE);
                imgOn.setVisibility(View.VISIBLE);
                MainStorageUtils mainStorageUtils = new MainStorageUtils();
                mainStorageUtils.putValue(NoiseAlert.this, "Value", "1");
                Toast.makeText(NoiseAlert.this, "Flash On", Toast.LENGTH_SHORT).show();
                start();
            }
        });

        imgOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgOff.setVisibility(View.VISIBLE);
                imgOn.setVisibility(View.GONE);
                MainStorageUtils mainStorageUtils = new MainStorageUtils();
                mainStorageUtils.putValue(NoiseAlert.this, "Value", "0");
                Toast.makeText(NoiseAlert.this, "Flash Off", Toast.LENGTH_SHORT).show();
                stop();
                turnOffflash();
            }
        });

//        if (!hasFlash) {
//            // device doesn't support flash
//            // Show alert message and close the application
//            AlertDialog alert = new AlertDialog.Builder(NoiseAlert.this)
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

        // get the camera
        // getCamera();
        // turnOnFlash();

        initializeApplicationConstants();
        mDisplay.setLevel(0, mThreshold);

        if (!mRunning) {
            mRunning = true;
            start();
//             turnOnflash();
        }

        ivSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(NoiseAlert.this, SettingsActiviy.class);
                startActivity(i);
            }
        });

    }

    /*
     * Get the camera
     */
    public void getCamera() {
        if (mCameraManager == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                //Log.e("Camera Error. Failed to Open. Error: ", e.getMessage());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Log.i("Noise", "==== onResume ===");
        // getCamera();
        initializeApplicationConstants();
        mDisplay.setLevel(0, mThreshold);

//        isFlashOn = false;

        if (!mRunning) {
            mRunning = true;
//            start();
//             turnOnflash();
        }

//        if (status == true) {
//            // turn off flash
//            turnOffflash();
//        } else {
//            // turn on flash
//            if (value == 1) {
//                turnOnflash();
//            }
//        }

    }

    @Override
    public void onStop() {
        super.onStop();
        // Log.i("Noise", "==== onStop ===");

//        if (camera != null) {
//            camera.release();
//            camera = null;
//        }

        // Stop noise monitoring
        stop();


    }

    @Override
    protected void onStart() {
        super.onStart();

        // on starting the app get the camera params
        //  getCamera();
    }

    public void start() {
        // Log.i("Noise", "==== start ===");

        mSensor.start();
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }

        // Noise monitoring start
        // Runnable(mPollTask) will execute after POLL_INTERVAL
        mHandler.postDelayed(mPollTask, POLL_INTERVAL);

    }

    private void stop() {
        Log.i("Noise", "==== Stop Noise Monitoring===");
        String switchValue = mainStorageUtils.getValue(NoiseAlert.this, "Value", "");
        if (switchValue.equalsIgnoreCase("0")) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            mHandler.removeCallbacks(mSleepTask);
            mHandler.removeCallbacks(mPollTask);
            mSensor.stop();
            mDisplay.setLevel(0, 0);
            updateDisplay("stopped...", 0.0);
            mRunning = false;
        } else {
            start();
        }


    }

    private void initializeApplicationConstants() {
        // Set Noise Threshold
        mThreshold = 10;

    }

    private void updateDisplay(String status, double signalEMA) {
        //	mStatusView.setText(status);
        //
        mDisplay.setLevel((int) signalEMA, mThreshold);

    }

    private void callForHelp() {

        // stop();

        // Show alert when noise thersold crossed
        Toast.makeText(getApplicationContext(),
                "Noise Thersold Crossed, do here your stuff.",
                Toast.LENGTH_LONG).show();
    }

    //    by asad
    public void turnOnflash() {

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            assert mCameraManager != null;
            mCameraId = mCameraManager.getCameraIdList()[0];
            mCameraManager.setTorchMode(mCameraId, true);
            Toast.makeText(NoiseAlert.this, "FlashLight is ON", Toast.LENGTH_SHORT).show();
        } catch (CameraAccessException e) {
            Log.e("Camera Problem", "Cannot turn on camera flashlight");
        }

    }

    public void turnOffflash() {

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            assert mCameraManager != null;
            mCameraId = mCameraManager.getCameraIdList()[0];
            mCameraManager.setTorchMode(mCameraId, false);
            Toast.makeText(NoiseAlert.this, "FlashLight is OFF", Toast.LENGTH_SHORT).show();
        } catch (CameraAccessException e) {
            Log.e("Camera Problem", "Cannot turn off camera flashlight");
        }
    }


    @Override
    public void onBackPressed() {

        super.onBackPressed();
//turnOnflash();
    }

    //by asad
    @Override
    protected void onPause() {
        super.onPause();

        // on pause turn off the flash
//        isFlashOn = true;
//        turnOffflash();
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