# Motrix下载器 - Java版

一个类似Motrix的Java下载器，支持多线程下载、断点续传等功能。

## 功能特性

- ✅ **多线程下载**: 支持1-16个线程并发下载，提高下载速度
- ✅ **断点续传**: 支持暂停和恢复下载，不会丢失已下载的数据
- ✅ **现代化GUI**: 基于Swing的美观用户界面
- ✅ **下载管理**: 添加、暂停、删除、查看下载任务
- ✅ **进度显示**: 实时显示下载进度、速度和状态
- ✅ **并发控制**: 可设置最大并发下载数量
- ✅ **文件管理**: 快速打开下载文件夹
- ✅ **错误处理**: 完善的错误处理和重试机制

## 系统要求

- Java 11 或更高版本
- Maven 3.6 或更高版本
- macOS / Windows / Linux

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd downloader
```

### 2. 编译项目

```bash
mvn clean compile
```

### 3. 运行应用

```bash
mvn exec:java -Dexec.mainClass="com.downloader.DownloaderApp"
```

或者打包后运行：

```bash
mvn clean package
java -jar target/motrix-downloader-1.0.0.jar
```

## 使用说明

### 添加下载任务

1. 在"下载链接"输入框中输入要下载的文件URL
2. 选择保存路径（默认为用户下载文件夹）
3. 点击"添加下载"按钮
4. 在弹出的对话框中确认文件名、保存路径和线程数
5. 点击"确定"开始下载

### 管理下载任务

- **开始下载**: 选中任务后点击"开始"按钮或右键菜单
- **暂停下载**: 选中任务后点击"暂停"按钮或右键菜单
- **删除任务**: 选中任务后点击"删除"按钮或右键菜单
- **打开文件夹**: 选中任务后点击"打开文件夹"按钮或右键菜单
- **复制链接**: 右键菜单中选择"复制下载链接"

### 设置选项

- **最大并发下载数**: 默认为3个，可在代码中修改
- **下载线程数**: 每个任务默认4个线程，可在添加下载时设置
- **保存路径**: 可为每个下载任务单独设置

## 项目结构

```
src/main/java/com/downloader/
├── DownloaderApp.java              # 主应用程序入口
├── model/
│   └── DownloadTask.java           # 下载任务数据模型
├── core/
│   ├── DownloadManager.java        # 下载管理器
│   ├── DownloadListener.java       # 下载事件监听器
│   ├── DownloadCallback.java       # 下载回调接口
│   └── MultiThreadDownloader.java  # 多线程下载器核心
└── ui/
    ├── MainWindow.java             # 主窗口界面
    ├── DownloadTableModel.java     # 下载列表表格模型
    ├── ProgressBarRenderer.java    # 进度条渲染器
    └── AddDownloadDialog.java      # 添加下载对话框
```

## 技术特点

### 多线程下载

- 自动检测服务器是否支持Range请求
- 将文件分割成多个块，每个线程下载一个块
- 支持动态调整线程数量（1-16个线程）

### 断点续传

- 使用临时文件(.tmp)保存下载进度
- 支持暂停后从断点继续下载
- 自动检测已下载的文件大小

### 并发控制

- 支持设置最大并发下载数量
- 超出限制的任务自动进入等待队列
- 任务完成后自动启动等待中的任务

### 错误处理

- 网络异常自动重试
- 详细的错误信息显示
- 优雅的异常处理机制

## 依赖库

- **Apache HttpClient**: HTTP请求处理
- **Jackson**: JSON数据处理
- **SLF4J + Logback**: 日志记录
- **JUnit**: 单元测试

## 开发计划

- [ ] 支持HTTP代理设置
- [ ] 支持下载队列管理
- [ ] 支持下载完成通知
- [ ] 支持下载历史记录
- [ ] 支持主题切换
- [ ] 支持多语言界面
- [ ] 支持BT/磁力链接下载
- [ ] 支持浏览器集成

## 贡献指南

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 致谢

- 感谢 [Motrix](https://motrix.app/) 项目的灵感
- 感谢所有开源依赖库的贡献者

## 联系方式

如有问题或建议，请提交 Issue 或 Pull Request。