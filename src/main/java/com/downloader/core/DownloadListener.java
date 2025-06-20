package com.downloader.core;

import com.downloader.model.DownloadTask;

/**
 * 下载事件监听器接口
 */
public interface DownloadListener {
    
    /**
     * 任务添加时触发
     */
    void onTaskAdded(DownloadTask task);
    
    /**
     * 任务更新时触发（进度、状态等）
     */
    void onTaskUpdated(DownloadTask task);
    
    /**
     * 任务删除时触发
     */
    void onTaskRemoved(DownloadTask task);
}