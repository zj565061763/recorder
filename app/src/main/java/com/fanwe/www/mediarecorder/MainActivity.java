package com.fanwe.www.mediarecorder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.fanwe.lib.player.SDMediaPlayer;
import com.fanwe.lib.recorder.FMediaRecorder;

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

        FMediaRecorder.getInstance().start(new File(getExternalCacheDir(), "record.aac"));
    }

    /**
     * 停止录音
     */
    public void onClickStopRecord(View view)
    {
        FMediaRecorder.getInstance().stop();
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
        FMediaRecorder.getInstance().init(this);
        FMediaRecorder.getInstance().setMaxRecordTime(60 * 1000);
        FMediaRecorder.getInstance().setOnCountDownCallback(new FMediaRecorder.OnCountDownCallback()
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
                FMediaRecorder.getInstance().stop();
            }
        });
        FMediaRecorder.getInstance().setOnStateChangeCallback(new FMediaRecorder.OnStateChangeCallback()
        {
            @Override
            public void onStateChanged(FMediaRecorder.State oldState, FMediaRecorder.State newState, FMediaRecorder recorder)
            {
                Log.i(TAG, "Recorder onStateChanged:" + newState);
            }
        });
        FMediaRecorder.getInstance().setOnRecorderCallback(new FMediaRecorder.OnRecorderCallback()
        {
            @Override
            public void onRecordSuccess(File file, long duration)
            {
                Log.i(TAG, "Recorder onRecordSuccess:" + file.getAbsolutePath() + "," + duration);
                SDMediaPlayer.getInstance().setDataPath(file.getAbsolutePath());
            }
        });
        FMediaRecorder.getInstance().setOnExceptionCallback(new FMediaRecorder.OnExceptionCallback()
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
        FMediaRecorder.getInstance().release();
        SDMediaPlayer.getInstance().release();
    }
}
