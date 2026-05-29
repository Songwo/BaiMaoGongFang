# VideoApp

## 项目概述
`VideoApp` 是一个 Android 应用项目，目前项目中包含了多种资源文件、布局文件、Gradle 配置文件以及测试代码等，用于实现视频相关功能（具体功能根据现有文件涉及用户资料管理、论坛、搜索等功能）。

## 环境要求
### 开发环境
- **JDK**：需要配置合适的 JDK 版本，Gradle 配置的 JVM 参数为 `-Xmx2048m -Dfile.encoding=UTF-8`。
- **Android Studio**：建议使用最新稳定版本，IDE 配置的 Gradle 设置会覆盖 `gradle.properties` 文件中的设置。
- **Gradle**：项目使用的 Gradle 版本可能需要根据 `settings.gradle.kts` 和 `build.gradle.kts` 文件进行适配。

### 依赖库
项目依赖了多个 AndroidX 库和其他第三方库，如：
- `androidx.transition:transition:1.2.0`
- `androidx.recyclerview:recyclerview:1.3.1`
- `com.github.bumptech.glide:glide:4.12.0` 等，具体可查看 `d:\VideoApp\app\build\intermediates\source_set_path_map\debug\mapDebugSourceSetPaths\file-map.txt` 文件。

## 项目结构
```plaintext
VideoApp/
├── app/
│   ├── build/                  # 构建生成的中间文件
│   ├── proguard-rules.pro      # ProGuard 混淆规则
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/           # Java 源代码
│   │   │   └── res/            # 资源文件
│   │   ├── androidTest/        # 仪器化测试代码
│   │   └── test/               # 本地单元测试代码
├── gradle.properties           # Gradle 全局配置
├── settings.gradle.kts         # 项目依赖解析管理配置
├── build.gradle.kts            # 顶级构建文件
└── local.properties            # 本地配置文件
