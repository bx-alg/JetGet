package com.downloader;

import com.downloader.core.DownloadManager;
import com.downloader.model.DownloadTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * 下载管理器测试类
 */
public class DownloadManagerTest {
    
    private DownloadManager downloadManager;
    private String testDownloadPath;
    
    @Before
    public void setUp() {
        downloadManager = new DownloadManager();
        testDownloadPath = System.getProperty("java.io.tmpdir") + "/test_downloads";
        
        // 创建测试目录
        File testDir = new File(testDownloadPath);
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
    }
    
    @After
    public void tearDown() {
        if (downloadManager != null) {
            downloadManager.shutdown();
        }
        
        // 清理测试文件
        File testDir = new File(testDownloadPath);
        if (testDir.exists()) {
            File[] files = testDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            testDir.delete();
        }
    }
    
    @Test
    public void testAddDownload() {
        // 测试添加下载任务
        String url = "https://httpbin.org/bytes/1024"; // 1KB测试文件
        String fileName = "test_file.bin";
        
        String taskId = downloadManager.addDownload(url, fileName, testDownloadPath);
        
        assertNotNull("任务ID不应为空", taskId);
        
        DownloadTask task = downloadManager.getTask(taskId);
        assertNotNull("任务不应为空", task);
        assertEquals("URL应该匹配", url, task.getUrl());
        assertEquals("文件名应该匹配", fileName, task.getFileName());
        assertEquals("保存路径应该匹配", testDownloadPath, task.getSavePath());
    }
    
    @Test
    public void testDownloadManagerInitialization() {
        // 测试下载管理器初始化
        assertNotNull("下载管理器不应为空", downloadManager);
        assertEquals("默认最大并发下载数应为3", 3, downloadManager.getMaxConcurrentDownloads());
        assertTrue("初始任务列表应为空", downloadManager.getAllTasks().isEmpty());
    }
    
    @Test
    public void testSetMaxConcurrentDownloads() {
        // 测试设置最大并发下载数
        downloadManager.setMaxConcurrentDownloads(5);
        assertEquals("最大并发下载数应为5", 5, downloadManager.getMaxConcurrentDownloads());
        
        // 测试边界值
        downloadManager.setMaxConcurrentDownloads(0);
        assertEquals("最大并发下载数最小应为1", 1, downloadManager.getMaxConcurrentDownloads());
    }
    
    @Test
    public void testRemoveTask() {
        // 测试删除任务
        String url = "https://httpbin.org/bytes/1024";
        String fileName = "test_remove.bin";
        
        String taskId = downloadManager.addDownload(url, fileName, testDownloadPath);
        assertNotNull("任务应该存在", downloadManager.getTask(taskId));
        
        downloadManager.removeTask(taskId);
        assertNull("任务应该被删除", downloadManager.getTask(taskId));
    }
    
    @Test(expected = RuntimeException.class)
    public void testAddDownloadWithInvalidUrl() {
        // 测试添加无效URL的下载任务
        downloadManager.addDownload("", "test.txt", testDownloadPath);
    }
    
    @Test(expected = RuntimeException.class)
    public void testAddDownloadWithInvalidFileName() {
        // 测试添加无效文件名的下载任务
        downloadManager.addDownload("https://httpbin.org/bytes/1024", "", testDownloadPath);
    }
    
    @Test(expected = RuntimeException.class)
    public void testAddDownloadWithInvalidPath() {
        // 测试添加无效路径的下载任务
        downloadManager.addDownload("https://httpbin.org/bytes/1024", "test.txt", "");
    }
}