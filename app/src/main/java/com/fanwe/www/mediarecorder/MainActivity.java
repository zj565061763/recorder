package com.fanwe.www.mediarecorder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.fanwe.lib.mediarecorder.SDMediaRecorder;
import com.fanwe.library.media.SDMediaPlayer;

import java.io.File;

public class MainActivity extends AppCompatActivity
{

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SDMediaRecorder.getInstance().init(this);
        SDMediaRecorder.getInstance().setMaxRecordTime(60 * 1000);
        SDMediaRecorder.getInstance().setOnCountDownCallback(new SDMediaRecorder.OnCountDownCallback()
        {
            @Override
            public void onTick(long leftTime)
            {
                Log.i(TAG, "Timer onTick:" + leftTime);
            }

            @Override
            public void onFinish()
            {
                Log.i(TAG, "Timer finish");
            }
        });
        SDMediaRecorder.getInstance().setOnStateChangeCallback(new SDMediaRecorder.OnStateChangeCallback()
        {
            @Override
            public void onStateChanged(SDMediaRecorder.State oldState, SDMediaRecorder.State newState, SDMediaRecorder recorder)
            {
                Log.i(TAG, "onStateChanged:" + oldState + " " + newState);
            }
        });
        SDMediaRecorder.getInstance().setOnRecorderCallback(new SDMediaRecorder.OnRecorderCallback()
        {
            @Override
            public void onRecordSuccess(File file, long duration)
            {
                Log.i(TAG, "onRecordSuccess:" + file.getAbsolutePath() + " " + duration);
                SDMediaPlayer.getInstance().setDataPath(file.getAbsolutePath());
            }
        });
    }

    public void onClickStartRecord(View view)
    {
        SDMediaPlayer.getInstance().stop();
    }

    public void onClickStopRecord(View view)
    {
        SDMediaRecorder.getInstance().stop();
    }

    public void onClickPlay(View view)
    {
//        SDMediaPlayer.getInstance().setDataPath(SDMediaRecorder.getInstance().getRecordFile().getAbsolutePath());
        SDMediaPlayer.getInstance().start();
    }
}
