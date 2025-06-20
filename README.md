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

## 打包成独立应用

### macOS应用打包

1. 首先确保项目已经成功编译打包：
```bash
mvn clean package
```

2. 使用jpackage创建.app应用：
```bash
jpackage --input target/ \
         --name "Motrix下载器" \
         --main-jar motrix-downloader-1.0.0.jar \
         --main-class com.downloader.DownloaderApp \
         --type dmg \
         --icon src/main/resources/icons/app.icns \
         --app-version 1.0.0 \
         --vendor "Your Name" \
         --copyright "Copyright 2024" \
         --mac-package-name "Motrix下载器"
```

### Windows应用打包

1. 确保项目编译打包：
```bash
mvn clean package
```

2. 使用jpackage创建.exe安装程序：
```bash
jpackage --input target/ \
         --name "MotrixDownloader" \
         --main-jar motrix-downloader-1.0.0.jar \
         --main-class com.downloader.DownloaderApp \
         --type exe \
         --icon src/main/resources/icons/app.ico \
         --app-version 1.0.0 \
         --vendor "Your Name" \
         --win-dir-chooser \
         --win-menu \
         --win-shortcut
```

### Linux应用打包

1. 确保项目编译打包：
```bash
mvn clean package
```

2. 使用jpackage创建.deb包：
```bash
jpackage --input target/ \
         --name "motrix-downloader" \
         --main-jar motrix-downloader-1.0.0.jar \
         --main-class com.downloader.DownloaderApp \
         --type deb \
         --icon src/main/resources/icons/app.png \
         --app-version 1.0.0 \
         --vendor "Your Name" \
         --linux-shortcut
```

注意事项：
- 确保已安装JDK 14或更高版本（包含jpackage工具）
- 图标文件需要根据不同平台准备相应格式：
  - macOS: .icns文件
  - Windows: .ico文件
  - Linux: .png文件
- 打包前请确保所有依赖都已正确配置在pom.xml中

## 使用说明