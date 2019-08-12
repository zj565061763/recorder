package com.sd.lib.recorder;

import android.content.Context;
import android.media.MediaRecorder;

import java.io.File;

public class FMediaRecorder
{
    private static final String DIR_NAME = "record";

    private static FMediaRecorder sInstance;

    private Context mContext;
    private MediaRecorder mRecorder;
    private FMediaRecorderParams mRecorderParams;
    private State mState = State.Idle;
    private File mDirFile;
    private boolean mIsInit;

    private long mStartTime;
    private File mRecordFile;

    private OnRecorderCallback mOnRecorderCallback;
    private OnExceptionCallback mOnExceptionCallback;
    private OnStateChangeCallback mOnStateChangeCallback;

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
    public void init(Context context)
    {
        if (mIsInit)
            return;

        if (context == null)
            throw new IllegalArgumentException("context is null");

        mContext = context.getApplicationContext();

        try
        {
            mRecorder = new MediaRecorder();
            mRecorder.setOnErrorListener(mInternalOnErrorListener);
            mState = State.Idle;

            mIsInit = true;
        } catch (Exception e)
        {
            notifyException(e);
        }
    }

    private void checkInit()
    {
        if (!mIsInit)
            throw new RuntimeException(this + " has not been init");
    }

    /**
     * 设置录音参数
     *
     * @param params
     */
    public void setRecorderParams(FMediaRecorderParams params)
    {
        mRecorderParams = params;
    }

    private FMediaRecorderParams getRecorderParams()
    {
        if (mRecorderParams == null)
            mRecorderParams = FMediaRecorderParams.DEFAULT;
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
     * 返回当前状态
     *
     * @return
     */
    public State getState()
    {
        return mState;
    }

    /**
     * 返回默认的录音文件保存目录
     *
     * @return
     */
    public File getDirFile()
    {
        final boolean ensure = ensureDirectoryExists();
        return ensure ? mDirFile : null;
    }

    private boolean ensureDirectoryExists()
    {
        if (mDirFile == null)
        {
            File dir = mContext.getExternalCacheDir();
            if (dir == null)
                dir = mContext.getCacheDir();

            mDirFile = new File(dir, DIR_NAME);
        }

        if (mDirFile == null)
        {
            notifyException(new RuntimeException("create dir file failed"));
            return false;
        }

        if (mDirFile.exists())
            return true;

        try
        {
            return mDirFile.mkdirs();
        } catch (Exception e)
        {
            notifyException(e);
            return false;
        }
    }

    private final MediaRecorder.OnErrorListener mInternalOnErrorListener = new MediaRecorder.OnErrorListener()
    {
        @Override
        public void onError(MediaRecorder mr, int what, int extra)
        {
            stopRecorder(false);
            notifyException(new RuntimeException(mr + ":" + what + "," + extra));
        }
    };

    private void setState(State state)
    {
        final State oldState = mState;
        if (oldState == state)
            return;

        mState = state;

        if (mState == State.Released)
            mIsInit = false;

        if (mOnStateChangeCallback != null)
            mOnStateChangeCallback.onStateChanged(this, oldState, mState);
    }

    /**
     * 开始录音
     */
    public void start(File file)
    {
        checkInit();
        switch (mState)
        {
            case Idle:
                startRecorder(file);
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
     * 释放资源，一般在录音界面关闭的时候调用，调用后如果想继续使用的话需要先调用init(context)方法初始化
     */
    public void release()
    {
        releaseRecorder();
    }

    private void startRecorder(File file)
    {
        if (file == null)
            file = Utils.createDefaultFileUnderDir(getDirFile(), null);

        if (file == null)
            return;

        mRecordFile = file;

        try
        {
            final FMediaRecorderParams params = getRecorderParams();
            mRecorder.setAudioSource(params.getAudioSource());
            mRecorder.setOutputFormat(params.getOutputFormat());
            mRecorder.setAudioEncoder(params.getAudioEncoder());

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
            setState(State.Idle);

            if (notifySuccess)
                notifyRecordSuccess();

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
    }

    private void notifyRecordSuccess()
    {
        if (mOnRecorderCallback == null)
            return;

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
            mOnExceptionCallback.onException(e);
    }

    public enum State
    {
        /**
         * 空闲
         */
        Idle,
        /**
         * 录音中
         */
        Recording,
        /**
         * 已经释放资源
         */
        Released,
    }

    public interface OnStateChangeCallback
    {
        /**
         * 播放器状态发生变化回调
         *
         * @param recorder
         * @param oldState
         * @param newState
         */
        void onStateChanged(FMediaRecorder recorder, State oldState, State newState);
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
}
