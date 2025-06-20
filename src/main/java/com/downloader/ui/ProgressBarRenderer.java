package com.downloader.ui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * 进度条表格单元格渲染器
 */
public class ProgressBarRenderer extends JProgressBar implements TableCellRenderer
{

    public ProgressBarRenderer()
    {
        super(0, 100);
        setStringPainted(true);
        setBorderPainted(true);
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column)
    {

        // 设置进度值
        if (value instanceof Double)
        {
            double progress = (Double) value;
            setValue((int) Math.round(progress));
            setString(String.format("%.1f%%", progress));
        } else
        {
            setValue(0);
            setString("0%");
        }

        // 设置颜色
        if (isSelected)
        {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else
        {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }

        // 根据进度设置进度条颜色
        int progress = getValue();
        if (progress == 100)
        {
            // 完成 - 绿色
            setForeground(new Color(0, 150, 0));
        } else if (progress > 0)
        {
            // 下载中 - 蓝色
            setForeground(new Color(0, 120, 215));
        } else
        {
            // 未开始 - 灰色
            setForeground(Color.GRAY);
        }

        return this;
    }
}