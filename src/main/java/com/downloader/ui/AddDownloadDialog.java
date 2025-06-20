package com.downloader.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

/**
 * 添加下载对话框
 */
public class AddDownloadDialog extends JDialog {
    
    private JTextField urlField;
    private JTextField fileNameField;
    private JTextField savePathField;
    private JSpinner threadCountSpinner;
    private boolean confirmed = false;
    
    public AddDownloadDialog(Frame parent, String initialUrl, String initialPath) {
        super(parent, "添加下载", true);
        
        initializeUI(initialUrl, initialPath);
        setupEventHandlers();
        
        setSize(500, 300);
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    private void initializeUI(String initialUrl, String initialPath) {
        setLayout(new BorderLayout());
        
        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // URL输入
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("下载链接:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        urlField = new JTextField(initialUrl, 30);
        urlField.addActionListener(e -> extractFileName());
        mainPanel.add(urlField, gbc);
        
        // 文件名输入
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("文件名:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        fileNameField = new JTextField(30);
        mainPanel.add(fileNameField, gbc);
        
        // 保存路径
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(new JLabel("保存路径:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        savePathField = new JTextField(initialPath, 25);
        mainPanel.add(savePathField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JButton browseButton = new JButton("浏览");
        browseButton.addActionListener(e -> chooseSavePath());
        mainPanel.add(browseButton, gbc);
        
        // 线程数设置
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("下载线程数:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        threadCountSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 16, 1));
        threadCountSpinner.setPreferredSize(new Dimension(80, 25));
        mainPanel.add(threadCountSpinner, gbc);
        
        gbc.gridx = 2; gbc.gridy = 3;
        gbc.gridwidth = 1;
        JLabel threadHintLabel = new JLabel("(1-16个线程)");
        threadHintLabel.setFont(threadHintLabel.getFont().deriveFont(Font.ITALIC, 11f));
        threadHintLabel.setForeground(Color.GRAY);
        mainPanel.add(threadHintLabel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(0, 15, 15, 15));
        
        JButton okButton = new JButton("确定");
        okButton.setPreferredSize(new Dimension(80, 30));
        okButton.addActionListener(e -> confirmDownload());
        buttonPanel.add(okButton);
        
        JButton cancelButton = new JButton("取消");
        cancelButton.setPreferredSize(new Dimension(80, 30));
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 自动提取文件名
        if (initialUrl != null && !initialUrl.trim().isEmpty()) {
            extractFileName();
        }
    }
    
    private void setupEventHandlers() {
        // 设置默认按钮
        getRootPane().setDefaultButton((JButton) ((JPanel) getContentPane().getComponent(1)).getComponent(0));
        
        // ESC键关闭对话框
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    private void extractFileName() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            return;
        }
        
        try {
            // 从URL中提取文件名
            String fileName = extractFileNameFromUrl(url);
            if (fileName != null && !fileName.isEmpty()) {
                fileNameField.setText(fileName);
            }
        } catch (Exception e) {
            // 忽略提取文件名的错误
        }
    }
    
    private String extractFileNameFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath();
            
            // 移除查询参数
            int queryIndex = path.indexOf('?');
            if (queryIndex > 0) {
                path = path.substring(0, queryIndex);
            }
            
            // 获取最后一个路径段
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            
            // 如果没有文件名，生成一个默认名称
            if (fileName.isEmpty() || !fileName.contains(".")) {
                fileName = "download_" + System.currentTimeMillis();
            }
            
            // URL解码
            fileName = java.net.URLDecoder.decode(fileName, "UTF-8");
            
            return fileName;
            
        } catch (Exception e) {
            return "download_" + System.currentTimeMillis();
        }
    }
    
    private void chooseSavePath() {
        JFileChooser fileChooser = new JFileChooser(savePathField.getText());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("选择保存路径");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            savePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void confirmDownload() {
        // 验证输入
        String url = urlField.getText().trim();
        String fileName = fileNameField.getText().trim();
        String savePath = savePathField.getText().trim();
        
        if (url.isEmpty()) {
            showError("请输入下载链接");
            urlField.requestFocus();
            return;
        }
        
        if (fileName.isEmpty()) {
            showError("请输入文件名");
            fileNameField.requestFocus();
            return;
        }
        
        if (savePath.isEmpty()) {
            showError("请选择保存路径");
            savePathField.requestFocus();
            return;
        }
        
        // 验证URL格式
        try {
            new URL(url);
        } catch (Exception e) {
            showError("下载链接格式不正确");
            urlField.requestFocus();
            return;
        }
        
        // 验证保存路径
        File saveDir = new File(savePath);
        if (!saveDir.exists()) {
            int option = JOptionPane.showConfirmDialog(
                this,
                "保存路径不存在，是否创建？",
                "确认",
                JOptionPane.YES_NO_OPTION
            );
            
            if (option == JOptionPane.YES_OPTION) {
                if (!saveDir.mkdirs()) {
                    showError("无法创建保存路径");
                    return;
                }
            } else {
                return;
            }
        }
        
        // 检查文件名是否包含非法字符
        if (fileName.matches(".*[<>:\"/\\|?*].*")) {
            showError("文件名包含非法字符");
            fileNameField.requestFocus();
            return;
        }
        
        // 检查文件是否已存在
        File targetFile = new File(saveDir, fileName);
        if (targetFile.exists()) {
            int option = JOptionPane.showConfirmDialog(
                this,
                "文件已存在，是否覆盖？\n" + targetFile.getAbsolutePath(),
                "文件已存在",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (option != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        confirmed = true;
        dispose();
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "输入错误",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    // Getters
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public String getDownloadUrl() {
        return urlField.getText().trim();
    }
    
    public String getFileName() {
        return fileNameField.getText().trim();
    }
    
    public String getSavePath() {
        return savePathField.getText().trim();
    }
    
    public int getThreadCount() {
        return (Integer) threadCountSpinner.getValue();
    }
}