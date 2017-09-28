package com.fanwe.www.mediarecorder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.fanwe.lib.recorder.SDMediaRecorder;
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

        initRecorder();
        initPlayer();
    }

    /**
     * 开始录音
     */
    public void onClickStartRecord(View view)
    {
        SDMediaPlayer.getInstance().reset();

        SDMediaRecorder.getInstance().start(new File(getExternalCacheDir(), "record.aac"));
    }

    /**
     * 停止录音
     */
    public void onClickStopRecord(View view)
    {
        SDMediaRecorder.getInstance().stop();
    }

    /**
     * 开始播放
     */
    public void onClickStartPlay(View view)
    {
        SDMediaPlayer.getInstance().start();
    }

    /**
     * 停止播放
     */
    public void onClickStopPlay(View view)
    {
        SDMediaPlayer.getInstance().stop();
    }

    private void initRecorder()
    {
        SDMediaRecorder.getInstance().init(this);
        SDMediaRecorder.getInstance().setMaxRecordTime(60 * 1000);
        SDMediaRecorder.getInstance().setOnCountDownCallback(new SDMediaRecorder.OnCountDownCallback()
        {
            @Override
            public void onTick(long leftTime)
            {
                Log.i(TAG, "Recorder Timer onTick:" + leftTime);
            }

            @Override
            public void onFinish()
            {
                Log.i(TAG, "Recorder Timer finish");
                SDMediaRecorder.getInstance().stop();
            }
        });
        SDMediaRecorder.getInstance().setOnStateChangeCallback(new SDMediaRecorder.OnStateChangeCallback()
        {
            @Override
            public void onStateChanged(SDMediaRecorder.State oldState, SDMediaRecorder.State newState, SDMediaRecorder recorder)
            {
                Log.i(TAG, "Recorder onStateChanged:" + newState);
            }
        });
        SDMediaRecorder.getInstance().setOnRecorderCallback(new SDMediaRecorder.OnRecorderCallback()
        {
            @Override
            public void onRecordSuccess(File file, long duration)
            {
                Log.i(TAG, "Recorder onRecordSuccess:" + file.getAbsolutePath() + "," + duration);
                SDMediaPlayer.getInstance().setDataPath(file.getAbsolutePath());
            }
        });
        SDMediaRecorder.getInstance().setOnExceptionCallback(new SDMediaRecorder.OnExceptionCallback()
        {
            @Override
            public void onException(Exception e)
            {
                Log.i(TAG, "Recorder onException:" + e);
            }
        });
    }

    private void initPlayer()
    {
        SDMediaPlayer.getInstance().init();
        SDMediaPlayer.getInstance().setOnStateChangeCallback(new SDMediaPlayer.OnStateChangeCallback()
        {
            @Override
            public void onStateChanged(SDMediaPlayer.State oldState, SDMediaPlayer.State newState, SDMediaPlayer player)
            {
                Log.i(TAG, "Player onStateChanged:" + newState);
            }
        });
        SDMediaPlayer.getInstance().setOnExceptionCallback(new SDMediaPlayer.OnExceptionCallback()
        {
            @Override
            public void onException(Exception e)
            {
                Log.i(TAG, "Player onException:" + e);
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        SDMediaRecorder.getInstance().release();
        SDMediaPlayer.getInstance().release();
    }
}
