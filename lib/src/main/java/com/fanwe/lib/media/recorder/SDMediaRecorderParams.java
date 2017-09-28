/*
 * Copyright (C) 2017 zhengjun, fanwe (http://www.fanwe.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fanwe.lib.media.recorder;

import android.media.MediaRecorder;

public class SDMediaRecorderParams
{
    private int audioSource;
    private int outputFormat;
    private int audioEncoder;

    public SDMediaRecorderParams()
    {
        setAudioSource(MediaRecorder.AudioSource.MIC);
        setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
    }

    public int getAudioSource()
    {
        return audioSource;
    }

    /**
     * 设置音频数据源
     *
     * @param audioSource {@link MediaRecorder.AudioSource}
     * @return
     */
    public SDMediaRecorderParams setAudioSource(int audioSource)
    {
        this.audioSource = audioSource;
        return this;
    }

    public int getOutputFormat()
    {
        return outputFormat;
    }

    /**
     * 设置输出格式
     *
     * @param outputFormat {@link MediaRecorder.OutputFormat}
     * @return
     */
    public SDMediaRecorderParams setOutputFormat(int outputFormat)
    {
        this.outputFormat = outputFormat;
        return this;
    }

    public int getAudioEncoder()
    {
        return audioEncoder;
    }

    /**
     * 设置编码格式
     *
     * @param audioEncoder {@link MediaRecorder.AudioEncoder}
     * @return
     */
    public SDMediaRecorderParams setAudioEncoder(int audioEncoder)
    {
        this.audioEncoder = audioEncoder;
        return this;
    }
}
