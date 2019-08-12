package com.sd.lib.recorder;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.CountDownTimer;

import java.io.File;

public class FMediaRecorder
{
    private static final String DIR_NAME = "record";

    private static FMediaRecorder sInstance;
    private MediaRecorder mRecorder;
    private FMediaRecorderParams mRecorderParams;
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

    private FMediaRecorder()
    {
    }

    public static FMediaRecorder getInstance()
    {
        if (sInstance == null)
        {
            synchronized (FMediaRecorder.class)
            {
                if (sInstance == null)
                    sInstance = new FMediaRecorder();
            }
        }
        return sInstance;
    }


    /**
     * 初始化录音器
     *
     * @param context
     */
    public synchronized void init(Context context)
    {
        if (mIsInit)
            return;

        try
        {

            if (mRecorder != null)
                release();

            File dir = context.getExternalCacheDir();
            if (dir == null)
                dir = context.getCacheDir();

            mDirFile = new File(dir, DIR_NAME);

            mRecorder = new MediaRecorder();
            mRecorder.setOnErrorListener(mInternalOnErrorListener);
            mState = State.Idle;

            mIsInit = true;
        } catch (Exception e)
        {
            notifyException(e);
        }
    }

    /**
     * 设置录音参数
     *
     * @param recorderParams
     */
    public void setRecorderParams(FMediaRecorderParams recorderParams)
    {
        mRecorderParams = recorderParams;
    }

    private FMediaRecorderParams getRecorderParams()
    {
        if (mRecorderParams == null)
        {
            mRecorderParams = FMediaRecorderParams.DEFAULT;
        }
        return mRecorderParams;
    }

    /**
     * 设置录音回调
     *
     * @param onRecorderCallback
     */
    public void setOnRecorderCallback(OnRecorderCallback onRecorderCallback)
    {
        mOnRecorderCallback = onRecorderCallback;
    }

    /**
     * 设置状态变化回调
     *
     * @param onStateChangeCallback
     */
    public void setOnStateChangeCallback(OnStateChangeCallback onStateChangeCallback)
    {
        mOnStateChangeCallback = onStateChangeCallback;
    }

    /**
     * 设置异常回调
     *
     * @param onExceptionCallback
     */
    public void setOnExceptionCallback(OnExceptionCallback onExceptionCallback)
    {
        mOnExceptionCallback = onExceptionCallback;
    }

    /**
     * 设置倒计时回调
     *
     * @param onCountDownCallback
     */
    public void setOnCountDownCallback(OnCountDownCallback onCountDownCallback)
    {
        mOnCountDownCallback = onCountDownCallback;
    }

    /**
     * 设置最大录音时长
     *
     * @param maxRecordTime (毫秒)
     */
    public void setMaxRecordTime(long maxRecordTime)
    {
        this.mMaxRecordTime = maxRecordTime;
    }

    /**
     * 删除默认目录下的所有录音文件
     */
    public void deleteAllFile()
    {
        Utils.deleteFileOrDir(mDirFile);
    }

    /**
     * 返回默认的录音文件保存目录
     *
     * @return
     */
    public File getDirFile()
    {
        ensureDirectoryExists();
        return mDirFile;
    }

    /**
     * 返回默认目录下根据指定文件名对应的File对象
     *
     * @param fileName
     * @return
     */
    public File getFile(String fileName)
    {
        File file = new File(getDirFile(), fileName);
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
     */
    public void start(File file)
    {
        switch (mState)
        {
            case Idle:
                startRecorder(file);
                break;
            case Recording:

                break;
            case Stopped:
                startRecorder(file);
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

    private void ensureDirectoryExists()
    {
        if (mDirFile != null && !mDirFile.exists())
        {
            try
            {
                mDirFile.mkdirs();
            } catch (Exception e)
            {
                notifyException(e);
            }
        }
    }

    private void startRecorder(File file)
    {
        try
        {
            mRecorder.setAudioSource(getRecorderParams().getAudioSource());
            mRecorder.setOutputFormat(getRecorderParams().getOutputFormat());
            mRecorder.setAudioEncoder(getRecorderParams().getAudioEncoder());

            mRecordFile = file;
            if (mRecordFile == null || !mRecordFile.exists())
            {
                mRecordFile = Utils.createDefaultFileUnderDir(getDirFile(), null);
            }

            mRecorder.setOutputFile(mRecordFile.getAbsolutePath());
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
        mOnRecorderCallback.onRecordSuccess(mRecordFile, duration);
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
        void onStateChanged(State oldState, State newState, FMediaRecorder recorder);
    }

    public interface OnRecorderCallback
    {
        /**
         * 录音成功回调
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
