# xiaizizi Android App

一个基于WebView的Android应用，用于嵌入并展示[xiaizizi.cn](https://xiaizizi.cn/)网站内容。

## 功能特性

- 📱 **网站嵌入**：使用WebView组件完整嵌入xiaizizi.cn网站
- 🔄 **自适应布局**：适配各种手机屏幕尺寸
- 🎨 **自定义图标**：使用自定义应用图标替代默认Android图标
- 🔧 **应用签名**：已配置发布签名，可直接构建发布APK
- 📋 **权限管理**：仅请求必要的网络权限
- 🌐 **自定义URL处理**：支持处理HTTP/HTTPS链接，忽略自定义协议

## 技术栈

- **开发语言**：Kotlin
- **框架**：Jetpack Compose
- **UI组件**：
  - AndroidView (WebView)
  - Material3
- **构建工具**：Gradle 8.13
- **最低SDK**：Android 11 (API 30)
- **目标SDK**：Android 15 (API 35)

## 项目结构

```
app/
├── src/
│   ├── main/
│   │   ├── java/cn/xiaizizi/
│   │   │   └── MainActivity.kt       # 主活动，WebView实现
│   │   ├── res/
│   │   │   ├── mipmap-*/            # 多密度应用图标
│   │   │   ├── values/
│   │   │   │   ├── colors.xml       # 颜色配置
│   │   │   │   ├── strings.xml      # 字符串资源
│   │   │   │   └── themes.xml       # 主题配置
│   │   │   └── AndroidManifest.xml  # 应用清单
│   └── test/                        # 单元测试
├── build.gradle.kts                 # 模块构建配置
└── keystores/app.keystore           # 应用签名密钥库
├── build.gradle.kts                 # 项目构建配置
├── gradle/libs.versions.toml        # 依赖版本管理
└── settings.gradle.kts              # 项目设置
```

## 安装和运行

### 环境要求

- JDK 17+
- Android Studio Giraffe+ 或 IntelliJ IDEA
- Android SDK 30+
- Android NDK (可选，用于某些原生库)

### 开发步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/yuezhicai123/yuezhicai-app.git
   ```

2. **打开项目**
   - 使用Android Studio或IntelliJ IDEA打开项目目录
   - 等待Gradle同步完成

3. **运行应用**
   - 连接Android设备或启动模拟器
   - 点击"Run"按钮或使用快捷键`Shift+F10`

## 构建APK

### 构建调试版本

```bash
./gradlew assembleDebug
```

生成的APK文件位置：`app/build/outputs/apk/debug/app-debug.apk`

### 构建发布版本

```bash
./gradlew assembleRelease
```

生成的APK文件位置：`app/build/outputs/apk/release/app-release.apk`

## 关键功能实现

### WebView集成

在`MainActivity.kt`中实现了WebView的核心配置：

```kotlin
// WebViewClient配置，处理URL加载和SSL错误
webViewClient = object : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return false // 让WebView处理HTTP/HTTPS链接
        } else {
            return true // 忽略自定义协议
        }
    }
    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        handler.proceed() // 忽略SSL错误（仅用于测试）
    }
}

// WebSettings配置
settings.apply {
    javaScriptEnabled = true
    domStorageEnabled = true
    loadWithOverviewMode = true
    useWideViewPort = true
    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
    userAgentString = "Mozilla/5.0 (Linux; Android ${android.os.Build.VERSION.RELEASE}) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/${android.os.Build.VERSION.RELEASE} Mobile Safari/537.36"
}

// CookieManager配置
val cookieManager = CookieManager.getInstance()
cookieManager.setAcceptCookie(true)
```

### 应用签名配置

在`app/build.gradle.kts`中配置了应用签名：

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../keystores/app.keystore")
        storePassword = "android"
        keyAlias = "app_release"
        keyPassword = "android"
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs["release"]
        isMinifyEnabled = false
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### 图标适配

应用图标已适配多种屏幕密度：

- mipmap-mdpi (48x48px)
- mipmap-hdpi (72x72px)
- mipmap-xhdpi (96x96px)
- mipmap-xxhdpi (144x144px)
- mipmap-xxxhdpi (192x192px)

## 注意事项

1. **SSL错误处理**：当前配置忽略了SSL错误，仅用于测试环境。在生产环境中应移除该配置。

2. **网络权限**：应用需要`INTERNET`权限才能加载网页内容。

3. **自定义协议**：WebView会忽略非HTTP/HTTPS协议的链接，如`bytedance://`等。

4. **会话管理**：使用CookieManager维护用户登录状态。

## 许可证

本项目采用MIT许可证 - 查看LICENSE文件了解详情。

## 贡献

欢迎提交Issue和Pull Request！

## 联系方式

- 项目URL：[https://github.com/yuezhicai123/yuezhicai-app](https://github.com/yuezhicai123/yuezhicai-app)
- 网站：[https://xiaizizi.cn/](https://xiaizizi.cn/)

## 更新日志

### v1.1.5 (2025-12-28)

- 优化了文件选择和相机拍摄的权限配置
- 添加了FileProvider支持，确保在Android 7.0+上能安全访问文件
- 修复了相机拍摄功能，现在可以在WebView中使用相机拍摄照片
- 完善了文件选择器逻辑，支持从图库选择或直接拍摄照片
- 解决了构建错误，确保应用能正常编译和运行
- 增加了访问本地文件的权限支持
- 更新WebView文件选择器配置，支持所有文件类型
- 添加Android 13+所有文件访问权限支持
- 完善权限请求逻辑，适配不同Android版本

### v1.1.3 (2025-12-28)

- 完善权限管理，优化Android 13+的媒体权限请求逻辑
- 适配Android 11至Android 15的权限系统
- 动态权限请求，根据不同Android版本自动请求相应的权限
- 提升应用整体稳定性

### v1.1.1 (2025-12-28)

- 添加了下拉刷新功能
- 优化了用户体验

### v1.1.0 (2025-12-28)

- 修复了循环重定向问题
- 移除了QQ登录功能
- 解决了非ASCII字符路径导致的构建问题

### v1.0.0 (2025-12-27)

- 首次发布
- 完整集成xiaizizi.cn网站
- 适配各种屏幕尺寸
- 配置应用签名