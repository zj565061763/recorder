package com.sd.www.mediarecorder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.sd.lib.player.FMediaPlayer;
import com.sd.lib.recorder.FMediaRecorder;

import java.io.File;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";

    private final FMediaRecorder mRecorder = new FMediaRecorder();
    private final FMediaPlayer mPlayer = new FMediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initRecorder();
        initPlayer();
    }

    private void initRecorder()
    {
        mRecorder.init(this);
        mRecorder.setOnStateChangeCallback(new FMediaRecorder.OnStateChangeCallback()
        {
            @Override
            public void onStateChanged(FMediaRecorder recorder, FMediaRecorder.State oldState, FMediaRecorder.State newState)
            {
                Log.i(TAG, "Recorder onStateChanged:" + newState);
            }
        });
        mRecorder.setOnRecorderCallback(new FMediaRecorder.OnRecorderCallback()
        {
            @Override
            public void onRecordSuccess(File file, long duration)
            {
                Log.i(TAG, "Recorder onRecordSuccess:" + file.getAbsolutePath() + "," + duration);
                mPlayer.setDataPath(file.getAbsolutePath());
            }
        });
        mRecorder.setOnExceptionCallback(new FMediaRecorder.OnExceptionCallback()
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
        mPlayer.init();
        mPlayer.addOnStateChangeCallback(new FMediaPlayer.OnStateChangeCallback()
        {
            @Override
            public void onStateChanged(FMediaPlayer player, FMediaPlayer.State oldState, FMediaPlayer.State newState)
            {
                Log.i(TAG, "Player onStateChanged:" + newState);
            }
        });
        mPlayer.setOnExceptionCallback(new FMediaPlayer.OnExceptionCallback()
        {
            @Override
            public void onException(Exception e)
            {
                Log.i(TAG, "Player onException:" + e);
            }
        });
    }

    /**
     * 开始录音
     */
    public void onClickStartRecord(View view)
    {
        mPlayer.reset();

        mRecorder.start(new File(getExternalCacheDir(), "record.aac"));
    }

    /**
     * 停止录音
     */
    public void onClickStopRecord(View view)
    {
        mRecorder.stop();
    }

    /**
     * 开始播放
     */
    public void onClickStartPlay(View view)
    {
        mPlayer.start();
    }

    /**
     * 停止播放
     */
    public void onClickStopPlay(View view)
    {
        mPlayer.stop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mRecorder.release();

        mPlayer.release();
    }
}
