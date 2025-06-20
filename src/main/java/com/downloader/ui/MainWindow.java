package com.downloader.ui;

import com.downloader.core.DownloadListener;
import com.downloader.core.DownloadManager;
import com.downloader.model.DownloadTask;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * 主窗口界面
 */
public class MainWindow extends JFrame implements DownloadListener
{

    private final DownloadManager downloadManager;
    private DownloadTableModel tableModel;
    private JTable downloadTable;
    private JTextField urlField;
    private JTextField savePathField;
    private JLabel statusLabel;
    private JProgressBar totalProgressBar;

    public MainWindow(DownloadManager downloadManager)
    {
        this.downloadManager = downloadManager;
        this.downloadManager.addListener(this);

        initializeUI();
        setupEventHandlers();
    }

    private void initializeUI()
    {
        setTitle("Motrix下载器 - Java版");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // 设置应用图标
        try
        {
            setIconImage(createAppIcon());
        } catch (Exception e)
        {
            // 忽略图标设置错误
        }

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 顶部工具栏
        mainPanel.add(createToolbar(), BorderLayout.NORTH);

        // 中央下载列表
        mainPanel.add(createDownloadPanel(), BorderLayout.CENTER);

        // 底部状态栏
        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createToolbar()
    {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBorder(BorderFactory.createTitledBorder("添加下载"));

        // URL输入区域
        JPanel urlPanel = new JPanel(new BorderLayout(5, 5));
        urlPanel.add(new JLabel("下载链接:"), BorderLayout.WEST);

        urlField = new JTextField();
        urlField.setToolTipText("请输入要下载的文件URL");
        urlPanel.add(urlField, BorderLayout.CENTER);

        JButton addButton = new JButton("添加下载");
        addButton.setPreferredSize(new Dimension(100, 30));
        addButton.addActionListener(e -> showAddDownloadDialog());
        urlPanel.add(addButton, BorderLayout.EAST);

        toolbar.add(urlPanel, BorderLayout.NORTH);

        // 保存路径区域
        JPanel pathPanel = new JPanel(new BorderLayout(5, 5));
        pathPanel.add(new JLabel("保存路径:"), BorderLayout.WEST);

        savePathField = new JTextField(System.getProperty("user.home") + "/Downloads");
        savePathField.setToolTipText("选择文件保存路径");
        pathPanel.add(savePathField, BorderLayout.CENTER);

        JButton browseButton = new JButton("浏览");
        browseButton.setPreferredSize(new Dimension(80, 30));
        browseButton.addActionListener(e -> chooseSavePath());
        pathPanel.add(browseButton, BorderLayout.EAST);

        toolbar.add(pathPanel, BorderLayout.SOUTH);

        return toolbar;
    }

    private JPanel createDownloadPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("下载列表"));

        // 创建表格模型和表格
        tableModel = new DownloadTableModel();
        downloadTable = new JTable(tableModel);

        // 设置表格属性
        downloadTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        downloadTable.setRowHeight(25);
        downloadTable.getTableHeader().setReorderingAllowed(false);

        // 设置列宽
        downloadTable.getColumnModel().getColumn(0).setPreferredWidth(200); // 文件名
        downloadTable.getColumnModel().getColumn(1).setPreferredWidth(80); // 大小
        downloadTable.getColumnModel().getColumn(2).setPreferredWidth(100); // 进度
        downloadTable.getColumnModel().getColumn(3).setPreferredWidth(80); // 速度
        downloadTable.getColumnModel().getColumn(4).setPreferredWidth(80); // 状态
        downloadTable.getColumnModel().getColumn(5).setPreferredWidth(300); // URL

        // 设置进度条渲染器
        downloadTable.getColumnModel().getColumn(2).setCellRenderer(new ProgressBarRenderer());

        JScrollPane scrollPane = new JScrollPane(downloadTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 右键菜单
        JPopupMenu popupMenu = createPopupMenu();
        downloadTable.setComponentPopupMenu(popupMenu);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton startButton = new JButton("开始");
        startButton.addActionListener(e -> startSelectedDownload());
        buttonPanel.add(startButton);

        JButton pauseButton = new JButton("暂停");
        pauseButton.addActionListener(e -> pauseSelectedDownload());
        buttonPanel.add(pauseButton);

        JButton removeButton = new JButton("删除");
        removeButton.addActionListener(e -> removeSelectedDownload());
        buttonPanel.add(removeButton);

        JButton openFolderButton = new JButton("打开文件夹");
        openFolderButton.addActionListener(e -> openDownloadFolder());
        buttonPanel.add(openFolderButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatusBar()
    {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());

        statusLabel = new JLabel("就绪");
        statusLabel.setBorder(new EmptyBorder(2, 5, 2, 5));
        statusBar.add(statusLabel, BorderLayout.WEST);

        // 总体进度条
        totalProgressBar = new JProgressBar(0, 100);
        totalProgressBar.setStringPainted(true);
        totalProgressBar.setString("0%");
        totalProgressBar.setPreferredSize(new Dimension(200, 20));
        statusBar.add(totalProgressBar, BorderLayout.EAST);

        return statusBar;
    }

    private JPopupMenu createPopupMenu()
    {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem startItem = new JMenuItem("开始下载");
        startItem.addActionListener(e -> startSelectedDownload());
        popupMenu.add(startItem);

        JMenuItem pauseItem = new JMenuItem("暂停下载");
        pauseItem.addActionListener(e -> pauseSelectedDownload());
        popupMenu.add(pauseItem);

        popupMenu.addSeparator();

        JMenuItem removeItem = new JMenuItem("删除任务");
        removeItem.addActionListener(e -> removeSelectedDownload());
        popupMenu.add(removeItem);

        popupMenu.addSeparator();

        JMenuItem openFolderItem = new JMenuItem("打开所在文件夹");
        openFolderItem.addActionListener(e -> openDownloadFolder());
        popupMenu.add(openFolderItem);

        JMenuItem copyUrlItem = new JMenuItem("复制下载链接");
        copyUrlItem.addActionListener(e -> copyDownloadUrl());
        popupMenu.add(copyUrlItem);

        return popupMenu;
    }

    private void setupEventHandlers()
    {
        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e)
            {
                int option = JOptionPane.showConfirmDialog(MainWindow.this, "确定要退出下载器吗？正在下载的任务将被暂停。", "确认退出",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (option == JOptionPane.YES_OPTION)
                {
                    downloadManager.shutdown();
                    System.exit(0);
                }
            }
        });

        // URL输入框回车事件
        urlField.addActionListener(e -> showAddDownloadDialog());
    }

    private void showAddDownloadDialog()
    {
        String url = urlField.getText().trim();
        if (url.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "请输入下载链接", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AddDownloadDialog dialog = new AddDownloadDialog(this, url, savePathField.getText());
        dialog.setVisible(true);

        if (dialog.isConfirmed())
        {
            try
            {
                String taskId = downloadManager.addDownload(dialog.getDownloadUrl(), dialog.getFileName(),
                        dialog.getSavePath());

                urlField.setText("");
                statusLabel.setText("已添加下载任务: " + dialog.getFileName());

            } catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, "添加下载失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void chooseSavePath()
    {
        JFileChooser fileChooser = new JFileChooser(savePathField.getText());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("选择保存路径");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            savePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void startSelectedDownload()
    {
        int selectedRow = downloadTable.getSelectedRow();
        if (selectedRow >= 0)
        {
            DownloadTask task = tableModel.getTaskAt(selectedRow);
            downloadManager.startDownload(task.getId());
        }
    }

    private void pauseSelectedDownload()
    {
        int selectedRow = downloadTable.getSelectedRow();
        if (selectedRow >= 0)
        {
            DownloadTask task = tableModel.getTaskAt(selectedRow);
            downloadManager.pauseDownload(task.getId());
        }
    }

    private void removeSelectedDownload()
    {
        int selectedRow = downloadTable.getSelectedRow();
        if (selectedRow >= 0)
        {
            DownloadTask task = tableModel.getTaskAt(selectedRow);

            int option = JOptionPane.showConfirmDialog(this, "确定要删除任务 \"" + task.getFileName() + "\" 吗？", "确认删除",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (option == JOptionPane.YES_OPTION)
            {
                downloadManager.removeTask(task.getId());
            }
        }
    }

    private void openDownloadFolder()
    {
        int selectedRow = downloadTable.getSelectedRow();
        if (selectedRow >= 0)
        {
            DownloadTask task = tableModel.getTaskAt(selectedRow);
            try
            {
                Desktop.getDesktop().open(new File(task.getSavePath()));
            } catch (Exception e)
            {
                JOptionPane.showMessageDialog(this, "无法打开文件夹: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void copyDownloadUrl()
    {
        int selectedRow = downloadTable.getSelectedRow();
        if (selectedRow >= 0)
        {
            DownloadTask task = tableModel.getTaskAt(selectedRow);
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(task.getUrl()), null);
            statusLabel.setText("已复制下载链接");
        }
    }

    private Image createAppIcon()
    {
        // 创建一个简单的应用图标
        int size = 32;
        java.awt.image.BufferedImage icon = new java.awt.image.BufferedImage(size, size,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制下载图标
        g2d.setColor(new Color(0, 120, 215));
        g2d.fillRoundRect(2, 2, size - 4, size - 4, 8, 8);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));

        // 绘制下载箭头
        int centerX = size / 2;
        int centerY = size / 2;
        g2d.drawLine(centerX, centerY - 8, centerX, centerY + 8);
        g2d.drawLine(centerX - 4, centerY + 4, centerX, centerY + 8);
        g2d.drawLine(centerX + 4, centerY + 4, centerX, centerY + 8);

        g2d.dispose();
        return icon;
    }

    // DownloadListener 实现
    @Override
    public void onTaskAdded(DownloadTask task)
    {
        SwingUtilities.invokeLater(() -> {
            tableModel.addTask(task);
            updateTotalProgress();
        });
    }

    @Override
    public void onTaskUpdated(DownloadTask task)
    {
        SwingUtilities.invokeLater(() -> {
            tableModel.updateTask(task);
            updateTotalProgress();
        });
    }

    @Override
    public void onTaskRemoved(DownloadTask task)
    {
        SwingUtilities.invokeLater(() -> {
            tableModel.removeTask(task);
            updateTotalProgress();
        });
    }

    private void updateTotalProgress()
    {
        java.util.List<DownloadTask> tasks = tableModel.getAllTasks();
        if (tasks.isEmpty())
        {
            totalProgressBar.setValue(0);
            totalProgressBar.setString("0%");
            statusLabel.setText("就绪");
            return;
        }

        long totalSize = 0;
        long downloadedSize = 0;
        int activeCount = 0;

        for (DownloadTask task : tasks)
        {
            totalSize += task.getTotalSize();
            downloadedSize += task.getDownloadedSize();
            if (task.getStatus() == DownloadTask.Status.DOWNLOADING)
            {
                activeCount++;
            }
        }

        if (totalSize > 0)
        {
            int progress = (int) (downloadedSize * 100 / totalSize);
            totalProgressBar.setValue(progress);
            totalProgressBar.setString(progress + "%");
        }

        if (activeCount > 0)
        {
            statusLabel.setText(String.format("正在下载 %d 个任务", activeCount));
        } else
        {
            statusLabel.setText("就绪");
        }
    }
}