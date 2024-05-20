package com.personal.flashonclap.light.status.flash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.personal.flashonclap.light.status.flash.Utils.MainStorageUtils;

public class SettingsActiviy extends AppCompatActivity {

    private SeekBar seekBar;
    private TextView tvStartValue,tvEndValue;
    private ImageView ivBack;
    private ImageView flashSwitchImg;
    private Button flashButton;
    float discrete=0;
    float start=0;
    float end=100;
    float start_pos=0;
    int start_position=0;
    private boolean isFlashOn = false;
    private boolean hasFlash = false;
    private AdView adView;
    private FrameLayout ad_view_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_activiy);

        seekBar = findViewById(R.id.seekBar);
        tvStartValue = findViewById(R.id.tvStartValue);
        ivBack = findViewById(R.id.ivBack);
        flashSwitchImg = findViewById(R.id.flashSwitchImg);
        ad_view_container = findViewById(R.id.ad_view_container);

        flashButton = findViewById(R.id.flashSwitch);

        start=-10;
        end=10;
        start_pos=5;

        start_position=(int) (((start_pos-start)/(end-start))*100);
        discrete=start_pos;

        seekBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        seekBar.getThumb().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);

        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        adView = new AdView(SettingsActiviy.this);
        adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
        ad_view_container.addView(adView);
        loadBanner();
//        seekBar.setProgress(start_position);

        init();

        MainStorageUtils mainStorageUtils = new MainStorageUtils();
        String progressValue = mainStorageUtils.getValue(SettingsActiviy.this,"Progress","");
        if (progressValue != ""){
            tvStartValue.setText(progressValue);
            seekBar.setProgress(Integer.parseInt(progressValue));
        }

        String switchValue = mainStorageUtils.getValue(SettingsActiviy.this,"Value","");
        if (switchValue != null){
            if (switchValue.equalsIgnoreCase("0")){
                flashButton.setBackgroundResource(R.drawable.btn_flash_off);
                flashSwitchImg.setBackgroundResource(R.drawable.img_flash_off);
            }else {
                flashButton.setBackgroundResource(R.drawable.btn_flash_on);
                flashSwitchImg.setBackgroundResource(R.drawable.img_flash_on);
            }
        }
    }

    private void init(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                float temp=progress;
                float dis=end-start;
                discrete=(start+((temp/100)*dis));

                tvStartValue.setText("Sensitivity Level: "+ progress);

                MainStorageUtils mainStorageUtils = new MainStorageUtils();
                mainStorageUtils.putValue(SettingsActiviy.this,"Progress",String.valueOf(progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasFlash){
                    if (isFlashOn){
                        isFlashOn = false;
                        flashButton.setBackgroundResource(R.drawable.btn_flash_off);
                        flashSwitchImg.setBackgroundResource(R.drawable.img_flash_off);
                        MainStorageUtils mainStorageUtils = new MainStorageUtils();
                        mainStorageUtils.putValue(SettingsActiviy.this,"Value","0");
                        Toast.makeText(SettingsActiviy.this, "Flash Off", Toast.LENGTH_SHORT).show();
//                        turnOffflash();
                    }
                    else {
                        isFlashOn = true;
                        flashButton.setBackgroundResource(R.drawable.btn_flash_on);
                        flashSwitchImg.setBackgroundResource(R.drawable.img_flash_on);
                        MainStorageUtils mainStorageUtils = new MainStorageUtils();
                        mainStorageUtils.putValue(SettingsActiviy.this,"Value","1");
                        Toast.makeText(SettingsActiviy.this, "Flash On", Toast.LENGTH_SHORT).show();
//                        turnOnflash();
                    }
                }
                else {
                    Toast.makeText(SettingsActiviy.this, "Device does'nt support Flash", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadBanner() {
        // Create an ad request. Check your logcat output for the hashed device ID
        // to get test ads on a physical device, e.g.,
        // "Use AdRequest.Builder.addTestDevice("ABCDE0123") to get test ads on this
        // device."
        AdRequest adRequest =
                new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                        .build();

//        AdSize adSize = getAdSize();
        AdSize customAdSize = new AdSize(300, 300);
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(customAdSize);

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