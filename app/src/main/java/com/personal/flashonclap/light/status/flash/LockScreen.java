package com.personal.flashonclap.light.status.flash;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.ActivityCompat;

public class LockScreen extends BroadcastReceiver {

    MainActivity mainActivity = new MainActivity();
    @Override
    public void onReceive(Context context, Intent intent) {
//        if screen off our service should start
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){

            ActivityCompat.startForegroundService(context,new Intent(context,MyService.class));
            mainActivity.startService();
        }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){

            mainActivity.stopService();
//want to turn off flash if user available
        }
    }
}
