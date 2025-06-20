package com.downloader.core;

import com.downloader.model.DownloadTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 下载管理器 负责管理所有下载任务
 */
public class DownloadManager
{

    private static final Logger logger = LoggerFactory.getLogger(DownloadManager.class);

    private final Map<String, DownloadTask> tasks;
    private final Map<String, MultiThreadDownloader> downloaders;
    private final ExecutorService executorService;
    private final List<DownloadListener> listeners;
    private final AtomicInteger maxConcurrentDownloads;
    private final AtomicInteger activeDownloads;

    public DownloadManager()
    {
        this.tasks = new ConcurrentHashMap<>();
        this.downloaders = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "DownloadManager-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        });
        this.listeners = new ArrayList<>();
        this.maxConcurrentDownloads = new AtomicInteger(3); // 默认最多3个并发下载
        this.activeDownloads = new AtomicInteger(0);

        logger.info("下载管理器已初始化");
    }

    /**
     * 添加下载任务
     */
    public String addDownload(String url, String fileName, String savePath)
    {
        try
        {
            // 验证参数
            if (url == null || url.trim().isEmpty())
            {
                throw new IllegalArgumentException("URL不能为空");
            }
            if (fileName == null || fileName.trim().isEmpty())
            {
                throw new IllegalArgumentException("文件名不能为空");
            }
            if (savePath == null || savePath.trim().isEmpty())
            {
                throw new IllegalArgumentException("保存路径不能为空");
            }

            // 创建保存目录
            File saveDir = new File(savePath);
            if (!saveDir.exists())
            {
                saveDir.mkdirs();
            }

            // 检查文件是否已存在
            File targetFile = new File(savePath, fileName);
            if (targetFile.exists())
            {
                fileName = generateUniqueFileName(savePath, fileName);
            }

            // 创建下载任务
            DownloadTask task = new DownloadTask(url, fileName, savePath);
            tasks.put(task.getId(), task);

            logger.info("添加下载任务: {} -> {}", url, task.getFullPath());

            // 通知监听器
            notifyTaskAdded(task);

            // 自动开始下载
            startDownload(task.getId());

            return task.getId();

        } catch (Exception e)
        {
            logger.error("添加下载任务失败: {}", e.getMessage(), e);
            throw new RuntimeException("添加下载任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 开始下载
     */
    public void startDownload(String taskId)
    {
        DownloadTask task = tasks.get(taskId);
        if (task == null)
        {
            logger.warn("任务不存在: {}", taskId);
            return;
        }

        if (task.getStatus() == DownloadTask.Status.DOWNLOADING)
        {
            logger.warn("任务已在下载中: {}", taskId);
            return;
        }

        // 检查并发下载限制
        if (activeDownloads.get() >= maxConcurrentDownloads.get())
        {
            task.setStatus(DownloadTask.Status.WAITING);
            notifyTaskUpdated(task);
            logger.info("任务等待中，当前并发下载数已达上限: {}", taskId);
            return;
        }

        task.setStatus(DownloadTask.Status.DOWNLOADING);
        task.setStartTime(java.time.LocalDateTime.now());
        activeDownloads.incrementAndGet();

        // 创建多线程下载器
        MultiThreadDownloader downloader = new MultiThreadDownloader(task, new DownloadCallback() {
            @Override
            public void onProgress(DownloadTask task, long downloadedBytes, long totalBytes, long speed)
            {
                task.setTotalSize(totalBytes);
                task.setDownloadedSize(downloadedBytes);
                task.setSpeed(speed);
                notifyTaskUpdated(task);
            }

            @Override
            public void onCompleted(DownloadTask task)
            {
                task.setStatus(DownloadTask.Status.COMPLETED);
                task.setCompleteTime(java.time.LocalDateTime.now());
                activeDownloads.decrementAndGet();
                downloaders.remove(task.getId());
                notifyTaskUpdated(task);
                logger.info("下载完成: {}", task.getFileName());

                // 启动等待中的任务
                startNextWaitingTask();
            }

            @Override
            public void onError(DownloadTask task, String error)
            {
                task.setStatus(DownloadTask.Status.ERROR);
                task.setErrorMessage(error);
                activeDownloads.decrementAndGet();
                downloaders.remove(task.getId());
                notifyTaskUpdated(task);
                logger.error("下载失败: {} - {}", task.getFileName(), error);

                // 启动等待中的任务
                startNextWaitingTask();
            }
        });

        downloaders.put(taskId, downloader);
        executorService.submit(downloader);

        notifyTaskUpdated(task);
        logger.info("开始下载: {}", task.getFileName());
    }

    /**
     * 暂停下载
     */
    public void pauseDownload(String taskId)
    {
        DownloadTask task = tasks.get(taskId);
        if (task == null)
            return;

        MultiThreadDownloader downloader = downloaders.get(taskId);
        if (downloader != null)
        {
            downloader.pause();
            task.setStatus(DownloadTask.Status.PAUSED);
            activeDownloads.decrementAndGet();
            downloaders.remove(taskId);
            notifyTaskUpdated(task);
            logger.info("暂停下载: {}", task.getFileName());

            // 启动等待中的任务
            startNextWaitingTask();
        }
    }

    /**
     * 删除任务
     */
    public void removeTask(String taskId)
    {
        DownloadTask task = tasks.get(taskId);
        if (task == null)
            return;

        // 先暂停下载
        pauseDownload(taskId);

        // 删除任务
        tasks.remove(taskId);
        notifyTaskRemoved(task);
        logger.info("删除任务: {}", task.getFileName());
    }

    /**
     * 启动下一个等待中的任务
     */
    private void startNextWaitingTask()
    {
        if (activeDownloads.get() >= maxConcurrentDownloads.get())
        {
            return;
        }

        tasks.values().stream().filter(task -> task.getStatus() == DownloadTask.Status.WAITING)
                .min(Comparator.comparing(DownloadTask::getCreateTime)).ifPresent(task -> startDownload(task.getId()));
    }

    /**
     * 生成唯一文件名
     */
    private String generateUniqueFileName(String savePath, String fileName)
    {
        String baseName = fileName;
        String extension = "";

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0)
        {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        int counter = 1;
        String newFileName;
        do
        {
            newFileName = baseName + "(" + counter + ")" + extension;
            counter++;
        } while (new File(savePath, newFileName).exists());

        return newFileName;
    }

    // 监听器相关方法
    public void addListener(DownloadListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(DownloadListener listener)
    {
        listeners.remove(listener);
    }

    private void notifyTaskAdded(DownloadTask task)
    {
        listeners.forEach(listener -> listener.onTaskAdded(task));
    }

    private void notifyTaskUpdated(DownloadTask task)
    {
        listeners.forEach(listener -> listener.onTaskUpdated(task));
    }

    private void notifyTaskRemoved(DownloadTask task)
    {
        listeners.forEach(listener -> listener.onTaskRemoved(task));
    }

    // Getters
    public Collection<DownloadTask> getAllTasks()
    {
        return new ArrayList<>(tasks.values());
    }

    public DownloadTask getTask(String taskId)
    {
        return tasks.get(taskId);
    }

    public int getMaxConcurrentDownloads()
    {
        return maxConcurrentDownloads.get();
    }

    public void setMaxConcurrentDownloads(int max)
    {
        maxConcurrentDownloads.set(Math.max(1, max));
    }

    /**
     * 关闭下载管理器
     */
    public void shutdown()
    {
        logger.info("正在关闭下载管理器...");

        // 停止所有下载
        downloaders.values().forEach(MultiThreadDownloader::pause);

        // 关闭线程池
        executorService.shutdown();
        try
        {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS))
            {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e)
        {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("下载管理器已关闭");
    }
}