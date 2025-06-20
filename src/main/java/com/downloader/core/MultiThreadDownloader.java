package com.downloader.core;

import com.downloader.model.DownloadTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 多线程下载器 支持断点续传和多线程下载
 */
public class MultiThreadDownloader implements Runnable
{

    private static final Logger logger = LoggerFactory.getLogger(MultiThreadDownloader.class);
    private static final int BUFFER_SIZE = 8192; // 8KB缓冲区
    private static final int PROGRESS_UPDATE_INTERVAL = 1000; // 进度更新间隔（毫秒）

    private final DownloadTask task;
    private final DownloadCallback callback;
    private final AtomicBoolean paused;
    private final AtomicBoolean cancelled;
    private final AtomicLong totalDownloaded;
    private final ExecutorService threadPool;
    private final List<Future<?>> downloadThreads;

    public MultiThreadDownloader(DownloadTask task, DownloadCallback callback)
    {
        this.task = task;
        this.callback = callback;
        this.paused = new AtomicBoolean(false);
        this.cancelled = new AtomicBoolean(false);
        this.totalDownloaded = new AtomicLong(0);
        this.threadPool = Executors.newFixedThreadPool(task.getThreadCount());
        this.downloadThreads = new ArrayList<>();
    }

    @Override
    public void run()
    {
        try
        {
            logger.info("开始下载: {}", task.getUrl());

            // 获取文件信息
            FileInfo fileInfo = getFileInfo(task.getUrl());
            if (fileInfo == null)
            {
                callback.onError(task, "无法获取文件信息");
                return;
            }

            task.setTotalSize(fileInfo.size);

            // 检查是否支持断点续传
            boolean supportResume = fileInfo.supportResume;

            // 创建目标文件
            File targetFile = new File(task.getFullPath());
            File tempFile = new File(task.getFullPath() + ".tmp");

            // 检查已下载的部分
            long startPosition = 0;
            if (tempFile.exists() && supportResume)
            {
                startPosition = tempFile.length();
                totalDownloaded.set(startPosition);
                logger.info("检测到临时文件，从位置 {} 继续下载", startPosition);
            }

            if (startPosition >= fileInfo.size)
            {
                // 文件已完整下载
                if (tempFile.renameTo(targetFile))
                {
                    callback.onCompleted(task);
                } else
                {
                    callback.onError(task, "无法重命名临时文件");
                }
                return;
            }

            // 启动进度监控
            ScheduledExecutorService progressMonitor = Executors.newSingleThreadScheduledExecutor();
            AtomicLong lastDownloaded = new AtomicLong(startPosition);
            AtomicLong lastTime = new AtomicLong(System.currentTimeMillis());

            progressMonitor.scheduleAtFixedRate(() -> {
                if (!paused.get() && !cancelled.get())
                {
                    long currentDownloaded = totalDownloaded.get();
                    long currentTime = System.currentTimeMillis();
                    long timeDiff = currentTime - lastTime.get();
                    long sizeDiff = currentDownloaded - lastDownloaded.get();

                    long speed = timeDiff > 0 ? (sizeDiff * 1000 / timeDiff) : 0;

                    callback.onProgress(task, currentDownloaded, fileInfo.size, speed);

                    lastDownloaded.set(currentDownloaded);
                    lastTime.set(currentTime);
                }
            }, 0, PROGRESS_UPDATE_INTERVAL, TimeUnit.MILLISECONDS);

            try
            {
                if (supportResume && fileInfo.size > 1024 * 1024)
                { // 大于1MB才使用多线程
                  // 多线程下载
                    downloadMultiThread(fileInfo, tempFile, startPosition);
                } else
                {
                    // 单线程下载
                    downloadSingleThread(task.getUrl(), tempFile, startPosition);
                }

                // 检查下载是否完成
                if (!paused.get() && !cancelled.get() && totalDownloaded.get() >= fileInfo.size)
                {
                    if (tempFile.renameTo(targetFile))
                    {
                        callback.onCompleted(task);
                    } else
                    {
                        callback.onError(task, "无法重命名临时文件");
                    }
                }

            } finally
            {
                progressMonitor.shutdown();
                threadPool.shutdown();
            }

        } catch (Exception e)
        {
            logger.error("下载失败: {}", e.getMessage(), e);
            callback.onError(task, e.getMessage());
        }
    }

    /**
     * 获取文件信息
     */
    private FileInfo getFileInfo(String url)
    {
        try (CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            HttpHead headRequest = new HttpHead(url);
            HttpResponse response = httpClient.execute(headRequest);

            long size = -1;
            boolean supportResume = false;

            // 获取文件大小
            if (response.getFirstHeader("Content-Length") != null)
            {
                size = Long.parseLong(response.getFirstHeader("Content-Length").getValue());
            }

            // 检查是否支持断点续传
            if (response.getFirstHeader("Accept-Ranges") != null)
            {
                String acceptRanges = response.getFirstHeader("Accept-Ranges").getValue();
                supportResume = "bytes".equalsIgnoreCase(acceptRanges);
            }

            logger.info("文件信息: 大小={}, 支持断点续传={}", size, supportResume);
            return new FileInfo(size, supportResume);

        } catch (Exception e)
        {
            logger.error("获取文件信息失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 多线程下载
     */
    private void downloadMultiThread(FileInfo fileInfo, File tempFile, long startPosition)
    {
        long remainingSize = fileInfo.size - startPosition;
        long chunkSize = remainingSize / task.getThreadCount();

        logger.info("使用{}个线程下载，每个线程下载{}字节", task.getThreadCount(), chunkSize);

        for (int i = 0; i < task.getThreadCount(); i++)
        {
            long start = startPosition + i * chunkSize;
            long end = (i == task.getThreadCount() - 1) ? fileInfo.size - 1 : start + chunkSize - 1;

            Future<?> future = threadPool.submit(new DownloadThread(task.getUrl(), tempFile, start, end, i));
            downloadThreads.add(future);
        }

        // 等待所有线程完成
        for (Future<?> future : downloadThreads)
        {
            try
            {
                future.get();
            } catch (Exception e)
            {
                logger.error("下载线程异常: {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 单线程下载
     */
    private void downloadSingleThread(String url, File tempFile, long startPosition)
    {
        try (CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            HttpGet request = new HttpGet(url);

            if (startPosition > 0)
            {
                request.setHeader("Range", "bytes=" + startPosition + "-");
            }

            HttpResponse response = httpClient.execute(request);

            try (InputStream inputStream = response.getEntity().getContent();
                    RandomAccessFile outputFile = new RandomAccessFile(tempFile, "rw"))
            {

                outputFile.seek(startPosition);

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1 && !paused.get() && !cancelled.get())
                {
                    outputFile.write(buffer, 0, bytesRead);
                    totalDownloaded.addAndGet(bytesRead);
                }
            }

        } catch (Exception e)
        {
            logger.error("单线程下载失败: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 暂停下载
     */
    public void pause()
    {
        paused.set(true);
        downloadThreads.forEach(future -> future.cancel(true));
        threadPool.shutdownNow();
        logger.info("下载已暂停: {}", task.getFileName());
    }

    /**
     * 取消下载
     */
    public void cancel()
    {
        cancelled.set(true);
        pause();

        // 删除临时文件
        File tempFile = new File(task.getFullPath() + ".tmp");
        if (tempFile.exists())
        {
            tempFile.delete();
        }

        logger.info("下载已取消: {}", task.getFileName());
    }

    /**
     * 下载线程
     */
    private class DownloadThread implements Runnable
    {
        private final String url;
        private final File file;
        private final long start;
        private final long end;
        private final int threadId;

        public DownloadThread(String url, File file, long start, long end, int threadId)
        {
            this.url = url;
            this.file = file;
            this.start = start;
            this.end = end;
            this.threadId = threadId;
        }

        @Override
        public void run()
        {
            try (CloseableHttpClient httpClient = HttpClients.createDefault())
            {
                HttpGet request = new HttpGet(url);
                request.setHeader("Range", "bytes=" + start + "-" + end);

                HttpResponse response = httpClient.execute(request);

                try (InputStream inputStream = response.getEntity().getContent();
                        RandomAccessFile outputFile = new RandomAccessFile(file, "rw"))
                {

                    outputFile.seek(start);

                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    long downloaded = 0;
                    long maxDownload = end - start + 1;

                    while ((bytesRead = inputStream.read(buffer)) != -1 && downloaded < maxDownload && !paused.get()
                            && !cancelled.get())
                    {

                        int writeSize = (int) Math.min(bytesRead, maxDownload - downloaded);
                        outputFile.write(buffer, 0, writeSize);
                        downloaded += writeSize;
                        totalDownloaded.addAndGet(writeSize);
                    }

                    logger.debug("线程{}下载完成: {}-{}", threadId, start, end);
                }

            } catch (Exception e)
            {
                if (!paused.get() && !cancelled.get())
                {
                    logger.error("下载线程{}失败: {}", threadId, e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 文件信息
     */
    private static class FileInfo
    {
        final long size;
        final boolean supportResume;

        FileInfo(long size, boolean supportResume)
        {
            this.size = size;
            this.supportResume = supportResume;
        }
    }
}