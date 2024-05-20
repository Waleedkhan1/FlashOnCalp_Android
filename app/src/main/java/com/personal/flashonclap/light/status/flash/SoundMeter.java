package com.personal.flashonclap.light.status.flash;


import java.io.IOException;

import android.content.Context;
import android.media.MediaRecorder;

import com.personal.flashonclap.light.status.flash.Utils.MainStorageUtils;

public class SoundMeter {
    // This file is used to record voice
    static final private double EMA_FILTER = 0.6;

    private MediaRecorder mRecorder = null;
    private double mEMA = 0.0;
    private Context mContext;

    public void start() {

        if (mRecorder == null) {

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");

            try {
                mRecorder.prepare();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                mRecorder.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mEMA = 0.0;
        }
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public double getAmplitude(Context Context) {
        this.mContext = Context;
        MainStorageUtils mainStorageUtils = new MainStorageUtils();
        String progressValue = mainStorageUtils.getValue(mContext, "Progress", "");
        StringBuilder msg = new StringBuilder(progressValue);
        msg.append("00");

        if (mRecorder != null)
//                        return  (mRecorder.getMaxAmplitude()/500.0);
            if (progressValue != "") {
                if (progressValue.equals("0")) {
//                    Toast.makeText(Context, "By default sensitivity applied", Toast.LENGTH_SHORT).show();
                    return (mRecorder.getMaxAmplitude() / 500.0);
                } else {
                    return (mRecorder.getMaxAmplitude() / Integer.parseInt(String.valueOf(msg)));
                }
            } else {
                return (mRecorder.getMaxAmplitude() / 500.0);
            }
        else
            return 0;

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude(mContext);
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }
}
