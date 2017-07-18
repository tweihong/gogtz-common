package com.gogtz.common.file;

/**
 * Stream进度条
 *
 * @author t
 */
public interface StreamProgress {

    /**
     * 开始
     */
    public void start();

    /**
     * 进行中
     *
     * @param progressSize 已经进行的大小
     */
    public void progress(long progressSize);

    /**
     * 结束
     */
    public void finish();
}
