# 关于android.overridePathCheck警告的解决方案

## 问题说明

在构建项目时，您可能会看到以下警告：
```
The option setting 'android.overridePathCheck=true' is experimental.
The current default is 'false'.
```

## 根本原因

这个警告出现是因为：
1. 您的项目路径包含**非ASCII字符**（中文"文件"和"桌面"）
2. Android构建工具默认不支持在包含非ASCII字符的路径中构建项目
3. 为了解决这个问题，我们启用了实验性配置`android.overridePathCheck=true`

## 当前解决方案

### 保留实验性配置（当前已启用）

由于无法移动项目（权限限制），我们需要继续使用这个实验性配置：

```properties
# 在gradle.properties文件中
android.overridePathCheck=true
```

**优点**：
- 项目可以正常构建
- 无需移动项目文件

**缺点**：
- 会显示实验性配置警告
- 这个配置可能在未来版本中被移除

## 长期解决方案

当您有适当权限时，建议将项目移动到不包含非ASCII字符的路径：

1. **创建新路径**：
   ```bash
   mkdir D:\yuezhicai-app
   ```

2. **复制项目文件**：
   ```bash
   xcopy /E /Y "d:\文件\桌面\1\yuezhicai-app\*" D:\yuezhicai-app\
   ```

3. **更新配置**：
   在新路径的gradle.properties文件中移除或注释掉：
   ```properties
   # android.overridePathCheck=true
   ```

4. **在新路径中打开项目**

## 验证方法

无论使用哪种方案，都可以通过以下命令验证构建：

```bash
./gradlew assembleDebug --no-build-cache
```

## 注意事项

1. 这个警告**不会影响应用的功能**
2. 实验性配置在大多数情况下可以正常工作
3. 建议在生产环境中使用标准路径（不包含非ASCII字符）

如果您有任何疑问，请参考Android官方文档：http://b.android.com/95744