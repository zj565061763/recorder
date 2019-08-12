# About
对MediaRecorder进行了封装，使用起来更方便

## Gradle
[![](https://jitpack.io/v/zj565061763/recorder.svg)](https://jitpack.io/#zj565061763/recorder)

## 常用方法
```java

FMediaRecorder mRecorder = new FMediaRecorder();

//初始化
mRecorder.init(this);
//设置状态变化回调
mRecorder.setOnStateChangeCallback(new FMediaRecorder.OnStateChangeCallback()
{
    @Override
    public void onStateChanged(FMediaRecorder recorder, FMediaRecorder.State oldState, FMediaRecorder.State newState)
    {
        Log.i(TAG, "Recorder onStateChanged:" + newState);
    }
});
//设置录音回调
mRecorder.setOnRecorderCallback(new FMediaRecorder.OnRecorderCallback()
{
    @Override
    public void onRecordSuccess(File file, long duration)
    {
        Log.i(TAG, "Recorder onRecordSuccess:" + file.getAbsolutePath() + "," + duration);
        FMediaPlayer.getInstance().setDataPath(file.getAbsolutePath());
    }
});
//设置异常回调
mRecorder.setOnExceptionCallback(new FMediaRecorder.OnExceptionCallback()
{
    @Override
    public void onException(Exception e)
    {
        Log.i(TAG, "Recorder onException:" + e);
    }
});

//开始录音，如果File为null，则内部会自动创建File
mRecorder.start(new File(getExternalCacheDir(), "record.aac"));
//停止录音
mRecorder.stop();
//释放录音器，释放后需要重新调用初始化方法才可以继续使用
mRecorder.release();
```
