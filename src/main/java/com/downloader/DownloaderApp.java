package com.downloader;

import com.downloader.ui.MainWindow;
import com.downloader.core.DownloadManager;
import javax.swing.*;

/**
 * 下载器主应用程序 类似Motrix的Java下载器
 */
public class DownloaderApp
{

    public static void main(String[] args)
    {
        // 设置系统外观
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        // 在事件调度线程中启动GUI
        SwingUtilities.invokeLater(() -> {
            try
            {
                // 初始化下载管理器
                DownloadManager downloadManager = new DownloadManager();

                // 创建并显示主窗口
                MainWindow mainWindow = new MainWindow(downloadManager);
                mainWindow.setVisible(true);

                System.out.println("Motrix下载器已启动");
            } catch (Exception e)
            {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "启动失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}