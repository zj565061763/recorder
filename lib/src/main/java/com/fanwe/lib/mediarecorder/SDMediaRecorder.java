package com.fanwe.lib.mediarecorder;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by Administrator on 2016/7/15.
 */
public class SDMediaRecorder
{
    private static final String DIR_NAME = "record";

    private static SDMediaRecorder sInstance;
    private MediaRecorder mRecorder;
    private State mState = State.Idle;
    private File mDirFile;
    private boolean mIsInit;

    private File mRecordFile;
    private long mStartTime;

    private CountDownTimer mCountDownTimer;
    private long mMaxRecordTime;

    private OnRecorderCallback mOnRecorderCallback;
    private OnExceptionCallback mOnExceptionCallback;
    private OnStateChangeCallback mOnStateChangeCallback;
    private OnCountDownCallback mOnCountDownCallback;

    private SDMediaRecorder()
    {
    }

    public static SDMediaRecorder getInstance()
    {
        if (sInstance == null)
        {
            synchronized (SDMediaRecorder.class)
            {
                if (sInstance == null)
                {
                    sInstance = new SDMediaRecorder();
                }
            }
        }
        return sInstance;
    }


    /**
     * 初始化录音器
     *
     * @param context
     */
    public void init(Context context)
    {
        try
        {
            if (mIsInit)
            {
                return;
            }

            if (mRecorder != null)
            {
                release();
            }

            mDirFile = new File(context.getCacheDir(), DIR_NAME);

            mRecorder = new MediaRecorder();
            mRecorder.setOnErrorListener(mInternalOnErrorListener);
            mState = State.Idle;

            mIsInit = true;
        } catch (Exception e)
        {
            notifyException(e);
        }
    }

    public void setOnRecorderCallback(OnRecorderCallback onRecorderCallback)
    {
        mOnRecorderCallback = onRecorderCallback;
    }

    public void setOnStateChangeCallback(OnStateChangeCallback onStateChangeCallback)
    {
        mOnStateChangeCallback = onStateChangeCallback;
    }

    public void setOnExceptionCallback(OnExceptionCallback onExceptionCallback)
    {
        mOnExceptionCallback = onExceptionCallback;
    }

    public void setOnCountDownCallback(OnCountDownCallback onCountDownCallback)
    {
        mOnCountDownCallback = onCountDownCallback;
    }

    public void setMaxRecordTime(long maxRecordTime)
    {
        this.mMaxRecordTime = maxRecordTime;
    }

    public void deleteAllFile()
    {
        Utils.deleteFileOrDir(mDirFile);
    }

    public File getRecordFile()
    {
        return mRecordFile;
    }

    public File getDirFile()
    {
        return mDirFile;
    }

    public File getFile(String fileName)
    {
        File file = new File(mDirFile, fileName);
        return file;
    }

    private MediaRecorder.OnErrorListener mInternalOnErrorListener = new MediaRecorder.OnErrorListener()
    {
        @Override
        public void onError(MediaRecorder mr, int what, int extra)
        {
            stopRecorder(false);
            notifyException(new RuntimeException(mr + ":" + String.valueOf(what) + "," + extra));
        }
    };

    public State getState()
    {
        return mState;
    }

    private void setState(State state)
    {
        if (mState == state)
        {
            return;
        }
        final State oldState = mState;

        mState = state;

        switch (mState)
        {
            case Recording:
                startTimer();
                break;
            case Stopped:
                stopTimer();
                break;
            case Released:
                mIsInit = false;
                break;
            case Idle:
                stopTimer();
                break;

            default:
                break;
        }

        if (mOnStateChangeCallback != null)
        {
            mOnStateChangeCallback.onStateChanged(oldState, mState, this);
        }
    }

    /**
     * 开始录音
     *
     * @param path 录音文件保存路径，如果为空的话，会用录音器内部的路径规则生成录音文件
     */
    public void start(String path)
    {
        switch (mState)
        {
            case Idle:
                startRecorder(path);
                break;
            case Recording:

                break;
            case Stopped:
                startRecorder(path);
                break;
            case Released:

                break;
            default:
                break;
        }
    }

    /**
     * 停止录音
     */
    public void stop()
    {
        switch (mState)
        {
            case Recording:
                stopRecorder(true);
                break;
            default:
                break;
        }
    }

    /**
     * 释放资源，一般在录音界面关闭的时候调用，调用后如果想系继续使用的话需要手动调用init(context)方法初始化
     */
    public void release()
    {
        switch (mState)
        {
            case Recording:
                stopRecorder(false);
                break;
            case Stopped:
                releaseRecorder();
                break;
            default:
                break;
        }
    }

    private void startRecorder(String path)
    {
        try
        {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            if (TextUtils.isEmpty(path))
            {
                mRecordFile = Utils.createDefaultFileUnderDir(mDirFile, "aac");
                path = mRecordFile.getAbsolutePath();
            } else
            {
                mRecordFile = new File(path);
            }

            mRecorder.setOutputFile(path);
            mRecorder.prepare();
            mRecorder.start();
            mStartTime = System.currentTimeMillis();

            setState(State.Recording);
        } catch (Exception e)
        {
            notifyException(e);
        }
    }

    private void stopRecorder(boolean notifySuccess)
    {
        try
        {
            mRecorder.stop();
            mRecorder.reset();
            setState(State.Stopped);

            if (notifySuccess)
            {
                notifyRecordSuccess();
            }

            resetData();
        } catch (Exception e)
        {
            notifyException(e);
        }
    }

    private void releaseRecorder()
    {
        mRecorder.release();
        setState(State.Released);
    }

    private void resetData()
    {
        mRecordFile = null;
        mStartTime = 0;
    }

    private void startTimer()
    {
        if (mMaxRecordTime < 1000)
        {
            return;
        }

        if (mCountDownTimer == null)
        {
            mCountDownTimer = new CountDownTimer(mMaxRecordTime, 1000)
            {
                @Override
                public void onTick(long millisUntilFinished)
                {
                    if (mOnCountDownCallback != null)
                    {
                        mOnCountDownCallback.onTick(millisUntilFinished);
                    }
                }

                @Override
                public void onFinish()
                {
                    if (mOnCountDownCallback != null)
                    {
                        mOnCountDownCallback.onFinish();
                    }
                }
            };
            mCountDownTimer.start();
        }
    }

    private void stopTimer()
    {
        if (mCountDownTimer != null)
        {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    private void notifyRecordSuccess()
    {
        if (mOnRecorderCallback == null)
        {
            return;
        }

        long duration = 0;
        if (mStartTime > 0)
        {
            duration = System.currentTimeMillis() - mStartTime;
        }
        mOnRecorderCallback.onRecordSuccess(getRecordFile(), duration);
    }

    private void notifyException(Exception e)
    {
        mRecorder.reset();
        setState(State.Idle);
        resetData();

        if (mOnExceptionCallback != null)
        {
            mOnExceptionCallback.onException(e);
        }
    }

    public enum State
    {
        /**
         * 已经释放资源
         */
        Released,
        /**
         * 空闲
         */
        Idle,
        /**
         * 录音中
         */
        Recording,
        /**
         * 重置状态
         */
        Stopped;
    }

    public interface OnStateChangeCallback
    {
        /**
         * 播放器状态发生变化回调
         *
         * @param oldState
         * @param newState
         * @param recorder
         */
        void onStateChanged(State oldState, State newState, SDMediaRecorder recorder);
    }

    public interface OnRecorderCallback
    {
        /**
         * 录制成功回调
         *
         * @param file
         * @param duration
         */
        void onRecordSuccess(File file, long duration);
    }

    public interface OnExceptionCallback
    {
        /**
         * 异常回调
         *
         * @param e
         */
        void onException(Exception e);
    }

    public interface OnCountDownCallback
    {
        void onTick(long leftTime);

        void onFinish();
    }

}
