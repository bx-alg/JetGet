package com.downloader.ui;

import com.downloader.model.DownloadTask;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * 下载任务表格模型
 */
public class DownloadTableModel extends AbstractTableModel {
    
    private static final String[] COLUMN_NAMES = {
        "文件名", "大小", "进度", "速度", "状态", "下载链接"
    };
    
    private final List<DownloadTask> tasks;
    
    public DownloadTableModel() {
        this.tasks = new ArrayList<>();
    }
    
    @Override
    public int getRowCount() {
        return tasks.size();
    }
    
    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: // 文件名
            case 1: // 大小
            case 3: // 速度
            case 4: // 状态
            case 5: // URL
                return String.class;
            case 2: // 进度
                return Double.class;
            default:
                return Object.class;
        }
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= tasks.size()) {
            return null;
        }
        
        DownloadTask task = tasks.get(rowIndex);
        
        switch (columnIndex) {
            case 0: // 文件名
                return task.getFileName();
                
            case 1: // 大小
                if (task.getTotalSize() > 0) {
                    return DownloadTask.formatFileSize(task.getDownloadedSize()) + 
                           " / " + DownloadTask.formatFileSize(task.getTotalSize());
                } else {
                    return DownloadTask.formatFileSize(task.getDownloadedSize());
                }
                
            case 2: // 进度
                return task.getProgress();
                
            case 3: // 速度
                if (task.getStatus() == DownloadTask.Status.DOWNLOADING && task.getSpeed() > 0) {
                    return DownloadTask.formatSpeed(task.getSpeed());
                } else {
                    return "-";
                }
                
            case 4: // 状态
                return task.getStatus().getDisplayName();
                
            case 5: // URL
                String url = task.getUrl();
                // 截断过长的URL
                if (url.length() > 50) {
                    return url.substring(0, 47) + "...";
                }
                return url;
                
            default:
                return null;
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // 所有单元格都不可编辑
    }
    
    /**
     * 添加任务
     */
    public void addTask(DownloadTask task) {
        tasks.add(task);
        int row = tasks.size() - 1;
        fireTableRowsInserted(row, row);
    }
    
    /**
     * 更新任务
     */
    public void updateTask(DownloadTask updatedTask) {
        for (int i = 0; i < tasks.size(); i++) {
            DownloadTask task = tasks.get(i);
            if (task.getId().equals(updatedTask.getId())) {
                tasks.set(i, updatedTask);
                fireTableRowsUpdated(i, i);
                break;
            }
        }
    }
    
    /**
     * 删除任务
     */
    public void removeTask(DownloadTask taskToRemove) {
        for (int i = 0; i < tasks.size(); i++) {
            DownloadTask task = tasks.get(i);
            if (task.getId().equals(taskToRemove.getId())) {
                tasks.remove(i);
                fireTableRowsDeleted(i, i);
                break;
            }
        }
    }
    
    /**
     * 获取指定行的任务
     */
    public DownloadTask getTaskAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < tasks.size()) {
            return tasks.get(rowIndex);
        }
        return null;
    }
    
    /**
     * 获取所有任务
     */
    public List<DownloadTask> getAllTasks() {
        return new ArrayList<>(tasks);
    }
    
    /**
     * 清空所有任务
     */
    public void clear() {
        int size = tasks.size();
        if (size > 0) {
            tasks.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }
    
    /**
     * 根据任务ID查找行索引
     */
    public int findRowByTaskId(String taskId) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(taskId)) {
                return i;
            }
        }
        return -1;
    }
}