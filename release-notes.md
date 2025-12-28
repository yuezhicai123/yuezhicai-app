# xiaizizi Android App v1.0.0

## 📱 应用概述

一个基于WebView的Android应用，专为嵌入并展示[xiaizizi.cn](https://xiaizizi.cn/)网站内容而设计。通过原生Android应用容器，为用户提供流畅的网站访问体验，同时支持QQ登录等扩展功能。

## ✨ 主要功能

### 核心功能
- **完整网站嵌入**：使用Android WebView组件无缝集成xiaizizi.cn网站
- **自适应布局**：完美适配各种Android手机屏幕尺寸和分辨率
- **QQ登录支持**：集成QQ第三方登录功能，保持用户会话状态
- **自定义应用图标**：使用专属图标替代默认Android图标，提升品牌辨识度

### 技术特性
- **安全的应用签名**：已配置正式发布签名，可直接安装使用
- **智能URL处理**：仅处理HTTP/HTTPS链接，自动忽略自定义协议（如bytedance://）
- **最小权限请求**：仅需要INTERNET权限，保护用户隐私
- **优化的Web体验**：启用JavaScript、DOM存储、混合内容模式等WebView优化

## 🛠️ 技术栈

- **开发语言**：Kotlin（现代化Android开发首选语言）
- **UI框架**：Jetpack Compose（Google推荐的新一代UI工具包）
- **核心组件**：AndroidView (WebView) + Material3
- **构建工具**：Gradle 8.13
- **API支持**：Android 11 (API 30) 至 Android 15 (API 35)

## 📦 安装方法

### 直接安装
1. 下载本Release中的 `app-release.apk` 文件
2. 在Android设备上允许「未知来源应用」安装
3. 点击APK文件完成安装

### 从源代码构建
```bash
# 克隆仓库
git clone https://github.com/yuezhicai123/yuezhicai-app.git

# 构建发布版本
cd yuezhicai-app
./gradlew assembleRelease
```

构建完成后，APK文件位于：`app/build/outputs/apk/release/app-release.apk`

## 📋 更新内容

### v1.1.4 (2025-12-28) - 本地文件访问权限增强

- 📁 **增加本地文件访问权限支持**：支持访问Android设备上的所有文件类型，不再仅限于图片
- 🔧 **更新WebView文件选择器配置**：将文件选择类型从仅图片(`image/*`)扩展为所有文件(`*/*`)
- 🛡️ **添加Android 13+权限支持**：在Android 13及以上版本添加了`MANAGE_EXTERNAL_STORAGE`权限支持
- 📱 **完善权限请求逻辑**：实现了智能权限检查和请求机制，根据不同Android版本自动处理权限
- 🎨 **优化用户体验**：添加了权限引导对话框，指导用户前往设置页面开启必要权限

### v1.1.3 (2025-12-28) - 权限管理优化

- 🔒 **完善权限管理**：优化了Android 13+的媒体权限请求逻辑，确保应用能正确访问相册和文件
- 📱 **版本兼容**：适配Android 11至Android 15的权限系统，提供一致的用户体验
- ⚙️ **动态权限请求**：根据不同Android版本自动请求相应的权限（如READ_MEDIA_IMAGES或READ_EXTERNAL_STORAGE）
- 🎨 **提升稳定性**：修复了可能的权限请求冲突问题，提高应用整体稳定性

### v1.1.2 (2025-12-28) - 图片上传功能

- ✨ **增加上传图片功能**：支持在WebView中上传图片，用户可以通过系统文件选择器选择图片并上传到网页
- 📸 **支持多种图片来源**：兼容不同版本Android系统的图片选择机制
- 🔒 **完善的权限管理**：自动请求必要的存储和相机权限，确保功能正常使用
- 🎨 **优化的用户体验**：选择图片后自动上传，提供流畅的操作体验

### v1.1.1 (2025-12-28) - 功能增强

- ✨ **添加下拉刷新功能**：集成SwipeRefreshLayout，支持通过下拉手势刷新WebView页面
- 📱 **优化用户体验**：页面加载完成后自动停止刷新动画，提供清晰的视觉反馈

### v1.1.0 (2025-12-28) - 修复与优化

- 🐛 **修复循环重定向问题**：解决了WebView中可能出现的无限重定向循环，提升应用稳定性
- ⚠️ **移除QQ登录功能**：因功能调整需要，暂时移除QQ第三方登录支持
- 🔧 **解决构建问题**：修复了非ASCII字符路径导致的构建失败问题，提高了跨平台兼容性

### v1.0.0 (2025-12-27) - 首次发布

- ✅ 完整集成xiaizizi.cn网站内容
- ✅ 实现QQ第三方登录功能
- ✅ 适配各种Android屏幕尺寸
- ✅ 配置正式应用签名
- ✅ 添加自定义应用图标（5种密度适配）
- ✅ 优化WebView性能和兼容性
- ✅ 处理自定义协议链接避免错误

## ⚠️ 注意事项

1. **SSL错误处理**：当前版本为了测试方便，忽略了SSL错误。在生产环境中使用时，建议移除相关配置。

2. **QQ登录配置**：确保您已在QQ开放平台注册应用并正确配置回调URL。

3. **网络连接**：应用需要稳定的网络连接才能正常加载网站内容。

4. **自定义协议**：WebView会自动忽略非HTTP/HTTPS协议的链接（如bytedance://等）。

5. **会话管理**：应用使用CookieManager维护用户登录状态，建议定期清理缓存以保证最佳体验。

## 🤝 贡献与反馈

欢迎提交Issue和Pull Request来帮助改进这个应用！

## 📞 联系方式

- **项目GitHub**：[https://github.com/yuezhicai123/yuezhicai-app](https://github.com/yuezhicai123/yuezhicai-app)
- **官方网站**：[https://xiaizizi.cn/](https://xiaizizi.cn/)

## 📄 许可证

本项目采用MIT许可证 - 查看LICENSE文件了解详情。

---

感谢您使用xiaizizi Android App！如有任何问题或建议，欢迎随时联系我们。