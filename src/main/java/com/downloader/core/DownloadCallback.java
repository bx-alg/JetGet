package com.downloader.core;
import com.downloader.model.DownloadTask;

/**
 * 下载回调接口
 */
public interface DownloadCallback {
    
    /**
     * 下载进度更新
     * @param task 下载任务
     * @param downloadedBytes 已下载字节数
     * @param totalBytes 总字节数
     * @param speed 下载速度（字节/秒）
     */
    void onProgress(DownloadTask task, long downloadedBytes, long totalBytes, long speed);
    
    /**
     * 下载完成
     * @param task 下载任务
     */
    void onCompleted(DownloadTask task);
    
    /**
     * 下载出错
     * @param task 下载任务
     * @param error 错误信息
     */
    void onError(DownloadTask task, String error);
}