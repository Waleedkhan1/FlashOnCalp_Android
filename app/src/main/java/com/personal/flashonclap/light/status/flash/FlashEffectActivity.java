package com.personal.flashonclap.light.status.flash;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FlashEffectActivity extends AppCompatActivity {

    private ImageView ivFlashEffect,ivLightOff,ivBack;
    private Context context;
    private ConstraintLayout constMain;
    private TextView tvTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_effect);

        ivFlashEffect = findViewById(R.id.ivLightEffect);
        constMain = findViewById(R.id.constMain);
        ivLightOff = findViewById(R.id.ivLightOff);
        ivBack = findViewById(R.id.ivBack);
        tvTxt = findViewById(R.id.tvTxt);
        context = getApplicationContext();

        init();
    }

    private void init(){
        ivFlashEffect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        boolean retVal = true;
                        retVal = Settings.System.canWrite(FlashEffectActivity.this);
                        if (retVal == false) {
                            if (!Settings.System.canWrite(getApplicationContext())) {

                                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                                Toast.makeText(getApplicationContext(), "Please, allow system settings for automatic logout ", Toast.LENGTH_LONG).show();
                                startActivityForResult(intent, 200);
                            }
                        }else {
                            Settings.System.putInt(context.getContentResolver(),
                                    Settings.System.SCREEN_BRIGHTNESS, 250);
                            constMain.setBackgroundColor(ContextCompat.getColor(FlashEffectActivity.this, R.color.white));
                            tvTxt.setTextColor(ContextCompat.getColor(FlashEffectActivity.this,R.color.black));
                            ivLightOff.setVisibility(View.VISIBLE);
                            ivFlashEffect.setVisibility(View.GONE);
                            ivBack.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);

                        }
                    }

                }catch (Exception e){

                }


            }
        });

        ivLightOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings.System.putInt(context.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, 10);
                constMain.setBackgroundColor(ContextCompat.getColor(FlashEffectActivity.this, R.color.black));
                tvTxt.setTextColor(ContextCompat.getColor(FlashEffectActivity.this,R.color.white));
                ivFlashEffect.setVisibility(View.VISIBLE);
                ivLightOff.setVisibility(View.GONE);
                ivBack.setColorFilter(ContextCompat.getColor(context, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }
}