package com.personal.flashonclap.light.status.flash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class Splash extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        Thread logoTimer = new Thread() {
            public void run() {
                try {
                    int logoTimer = 0;
                    while (logoTimer < 3000) {
                        sleep(100);
                        logoTimer = logoTimer + 100;
                    }
//                    startActivity(new Intent(
//                            "com.itapp.flashlightonclap.led.turnon.flash.CLEARSCREEN"));

                    startActivity(new Intent(Splash.this, MainActivity.class));

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    finish();
                }
            }
        };

        logoTimer.start();

    }

}


