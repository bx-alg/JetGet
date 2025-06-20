package com.downloader.model;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 下载任务模型
 */
public class DownloadTask
{

    public enum Status {
        WAITING("等待中"), DOWNLOADING("下载中"), PAUSED("已暂停"), COMPLETED("已完成"), ERROR("错误"), CANCELLED("已取消");

        private final String displayName;

        Status(String displayName)
        {
            this.displayName = displayName;
        }

        public String getDisplayName()
        {
            return displayName;
        }
    }

    private String id;
    private String url;
    private String fileName;
    private String savePath;
    private long totalSize;
    private AtomicLong downloadedSize;
    private Status status;
    private LocalDateTime createTime;
    private LocalDateTime startTime;
    private LocalDateTime completeTime;
    private String errorMessage;
    private int threadCount;
    private long speed; // 字节/秒

    public DownloadTask(String url, String fileName, String savePath)
    {
        this.id = generateId();
        this.url = url;
        this.fileName = fileName;
        this.savePath = savePath;
        this.downloadedSize = new AtomicLong(0);
        this.status = Status.WAITING;
        this.createTime = LocalDateTime.now();
        this.threadCount = 4; // 默认4个线程
    }

    private String generateId()
    {
        return "task_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
    }

    // Getters and Setters
    public String getId()
    {
        return id;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getSavePath()
    {
        return savePath;
    }

    public void setSavePath(String savePath)
    {
        this.savePath = savePath;
    }

    public long getTotalSize()
    {
        return totalSize;
    }

    public void setTotalSize(long totalSize)
    {
        this.totalSize = totalSize;
    }

    public long getDownloadedSize()
    {
        return downloadedSize.get();
    }

    public void setDownloadedSize(long size)
    {
        this.downloadedSize.set(size);
    }

    public void addDownloadedSize(long size)
    {
        this.downloadedSize.addAndGet(size);
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public LocalDateTime getCreateTime()
    {
        return createTime;
    }

    public LocalDateTime getStartTime()
    {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime)
    {
        this.startTime = startTime;
    }

    public LocalDateTime getCompleteTime()
    {
        return completeTime;
    }

    public void setCompleteTime(LocalDateTime completeTime)
    {
        this.completeTime = completeTime;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public int getThreadCount()
    {
        return threadCount;
    }

    public void setThreadCount(int threadCount)
    {
        this.threadCount = threadCount;
    }

    public long getSpeed()
    {
        return speed;
    }

    public void setSpeed(long speed)
    {
        this.speed = speed;
    }

    /**
     * 获取下载进度百分比
     */
    public double getProgress()
    {
        if (totalSize <= 0)
            return 0.0;
        return (double) downloadedSize.get() / totalSize * 100.0;
    }

    /**
     * 获取完整的文件路径
     */
    public String getFullPath()
    {
        return savePath + "/" + fileName;
    }

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long size)
    {
        if (size < 1024)
            return size + " B";
        if (size < 1024 * 1024)
            return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024)
            return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }

    /**
     * 格式化下载速度
     */
    public static String formatSpeed(long bytesPerSecond)
    {
        return formatFileSize(bytesPerSecond) + "/s";
    }

    @Override
    public String toString()
    {
        return String.format("DownloadTask{id='%s', fileName='%s', status=%s, progress=%.1f%%}", id, fileName, status,
                getProgress());
    }
}