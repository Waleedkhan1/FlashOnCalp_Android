package com.personal.flashonclap.light.status.flash;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.IBinder;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class MyService extends Service implements SensorEventListener {
    private BroadcastReceiver mReceiver;
    private boolean isShowing = false;
    CameraManager cameraManager;
    private SensorManager sensorManager;
    private WindowManager windowManager;
    private TextView textview;
    WindowManager.LayoutParams params;
    private Camera camera;
    private SoundMeter mSensor;
    private int mThreshold;
    NoiseAlert noiseAlert = new NoiseAlert();
    private long lastShakeTime = 0;
    private boolean isFlashlightOn = false;
    private Float shakeThreshold;
    public static final int MIN_TIME_BETWEEN_SHAKES = 1000;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sensorManager != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            sensorManager.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
//
//        try {
//            shakeThreshold = Float.parseFloat(Global.loadFile(getApplicationContext(), "settings.txt"));
//        } catch (Exception ex) {
//            Global.saveFile(getApplicationContext(), "settings.txt", String.valueOf(10.2f));
//            shakeThreshold = 10.2f;
//            ex.getMessage();
//        }

        //Register receiver for determining screen off and if user is present
        mReceiver = new LockScreen();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);

        registerReceiver(mReceiver, filter);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

//        Global global = new Global();
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
//            long curTime = System.currentTimeMillis();
//            if ((curTime - lastShakeTime) > MIN_TIME_BETWEEN_SHAKES) {
//                if (noiseAlert.isFlashOn) {
//                    isFlashlightOn = global.torchToggle("off", this);
//                } else {
//                    isFlashlightOn = global.torchToggle("on", this);
//                }
//            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        onTaskRemoved(intent);
//        Toast.makeText(this, "Service running in background", Toast.LENGTH_SHORT).show();
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            String input = intent.getStringExtra("inputExtra");

            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Foreground Service")
                    .setContentText(input)
                    .setSmallIcon(R.drawable.ic_baseline_highlight_24)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
        }

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(rootIntent);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //    public class LockScreenStateReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
//                //if screen is turn off show the textview
//                ActivityCompat.startForegroundService(context,new Intent(context,MyService.class));
////                Toast.makeText(getApplication(),"services ",Toast.LENGTH_SHORT).show();
////
//                if (!isShowing) {
//                    windowManager.addView(textview, params);
//                    isShowing = true;
//                }
//            }
//
//            else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
//                //Handle resuming events if user is present/screen is unlocked remove the textview immediately
//                if (isShowing) {
//                    Toast.makeText(getApplication(),"services ",Toast.LENGTH_SHORT).show();
//                    windowManager.removeViewImmediate(textview);
//                    isShowing = false;
//                }
//            }
//        }
//    }
    @Override
    public void onDestroy() {

        Intent broadcastIntent = new Intent(this, LockScreen.class);
        sendBroadcast(broadcastIntent);

//        unregister receiver when the service is destroy
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }

        //remove view if it is showing and the service is destroy
        if (isShowing) {
            windowManager.removeViewImmediate(textview);
            isShowing = false;
        }

        super.onDestroy();
    }

}
