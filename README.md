# About
对MediaRecorder进行了封装，使用起来更方便

## Gradle
[![](https://jitpack.io/v/zj565061763/recorder.svg)](https://jitpack.io/#zj565061763/recorder)

## 常用方法
```java
//初始化
FMediaRecorder.getInstance().init(this);
//设置最大录音时长
FMediaRecorder.getInstance().setMaxRecordTime(60 * 1000);
//设置倒计时回调
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
//设置状态变化回调
FMediaRecorder.getInstance().setOnStateChangeCallback(new FMediaRecorder.OnStateChangeCallback()
{
    @Override
    public void onStateChanged(FMediaRecorder.State oldState, FMediaRecorder.State newState, FMediaRecorder recorder)
    {
        Log.i(TAG, "Recorder onStateChanged:" + newState);
    }
});
//设置录音回调
FMediaRecorder.getInstance().setOnRecorderCallback(new FMediaRecorder.OnRecorderCallback()
{
    @Override
    public void onRecordSuccess(File file, long duration)
    {
        Log.i(TAG, "Recorder onRecordSuccess:" + file.getAbsolutePath() + "," + duration);
        FMediaPlayer.getInstance().setDataPath(file.getAbsolutePath());
    }
});
//设置异常回调
FMediaRecorder.getInstance().setOnExceptionCallback(new FMediaRecorder.OnExceptionCallback()
{
    @Override
    public void onException(Exception e)
    {
        Log.i(TAG, "Recorder onException:" + e);
    }
});

//开始录音，如果File为null，则内部会自动创建File
FMediaRecorder.getInstance().start(new File(getExternalCacheDir(), "record.aac"));
//停止录音
FMediaRecorder.getInstance().stop();
//释放录音器，释放后需要重新调用初始化方法才可以继续使用
FMediaRecorder.getInstance().release();
```
