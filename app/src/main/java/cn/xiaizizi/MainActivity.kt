package cn.xiaizizi

import android.os.Bundle
import android.os.Environment
import android.app.AlertDialog
import android.provider.Settings
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.net.http.SslError
import android.webkit.SslErrorHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.xiaizizi.ui.theme.DiscuzTheme
import androidx.compose.material3.Scaffold
import android.webkit.WebChromeClient
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import android.os.Build
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.graphics.Bitmap
import android.provider.DocumentsContract
import android.webkit.ValueCallback

// 配置常量
private const val BASE_URL = "https://xiaizizi.cn/"
private const val MAX_VISITED_URLS = 50
private const val MAX_REDIRECTS = 5
private const val IGNORE_SSL_ERRORS = true // 生产环境建议设置为false

// 权限请求码
private const val REQUEST_CODE_PERMISSIONS = 1001
private const val REQUEST_CODE_FILE_PICKER = 1002
private const val REQUEST_CODE_CAMERA = 1003

// 需要的权限列表
private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    // Android 13+ 使用媒体权限、相机权限和通知权限
    arrayOf(
        android.Manifest.permission.READ_MEDIA_IMAGES,
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.POST_NOTIFICATIONS
    )
} else {
    // Android 12- 使用存储权限和相机权限
    arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.CAMERA
    )
}

class MainActivity : ComponentActivity() {
    // 保存WebView引用
    private var webView: WebView? = null
    
    // 保存文件选择回调引用
    public var fileChooserCallback: ValueCallback<Array<Uri>>? = null
    
    // 保存临时图片文件Uri
    public var cameraImageUri: Uri? = null
    
    // 保存需要请求的额外权限列表
    private val additionalPermissions = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 请求必要的权限
        requestPermissionsIfNeeded()
        
        // 初始化通知渠道
        initializeNotificationChannels()
        
        // 使用更现代的方式处理返回键
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView?.canGoBack() == true) {
                    // 如果WebView可以返回，则返回上一页
                    webView?.goBack()
                } else {
                    // 否则禁用此回调并执行默认的返回操作
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        }
        
        // 将回调添加到调度器
        onBackPressedDispatcher.addCallback(this, callback)
        
        setContent {
            DiscuzTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    WebViewScreen(modifier = Modifier.padding(it)) { webView = it }
                }
            }
        }
    }

    // 初始化通知渠道
    private fun initializeNotificationChannels() {
        val notificationService = NotificationService()
        notificationService.createNotificationChannel(this)
    }

    // 优化内存管理：在Activity销毁时释放WebView资源
    override fun onDestroy() {
        super.onDestroy()
        webView?.apply {
            stopLoading()
            removeAllViews()
            destroy()
        }
        webView = null
    }
    
    // 处理权限请求结果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (!allGranted) {
                Toast.makeText(this, "缺少必要权限，部分功能可能无法使用", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // 处理文件选择结果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            REQUEST_CODE_FILE_PICKER -> {
                if (resultCode == RESULT_OK) {
                    val result = when {
                        // 如果data不为空，可能是文件选择或相机拍摄（取决于设备）
                        data != null -> {
                            when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                true -> {
                                    val resultArray: Array<Uri>? = data.clipData?.let { clipData ->
                                        val uris = arrayOfNulls<Uri>(clipData.itemCount)
                                        for (i in 0 until clipData.itemCount) {
                                            uris[i] = clipData.getItemAt(i).uri
                                        }
                                        uris.filterNotNull().toTypedArray()
                                    } ?: data.data?.let { arrayOf(it) }
                                    resultArray
                                }
                                else -> {
                                    data.data?.let { arrayOf(it) }
                                }
                            }
                        }
                        // 如果data为空但resultCode为OK，可能是相机拍摄的结果
                        else -> {
                            cameraImageUri?.let { arrayOf(it) }
                        }
                    }
                    
                    result?.let { uris ->
                        fileChooserCallback?.onReceiveValue(uris)
                    } ?: run {
                        fileChooserCallback?.onReceiveValue(null)
                    }
                } else {
                    fileChooserCallback?.onReceiveValue(null)
                }
                fileChooserCallback = null
                // 清除临时相机Uri，避免内存泄漏
                cameraImageUri = null
            }
            REQUEST_CODE_CAMERA -> {
                // 这个分支可能不再需要，因为相机功能现在通过REQUEST_CODE_FILE_PICKER处理
                // 保留以兼容可能的旧代码
                if (resultCode == RESULT_OK) {
                    cameraImageUri?.let { uri ->
                        fileChooserCallback?.onReceiveValue(arrayOf(uri))
                    }
                } else {
                    fileChooserCallback?.onReceiveValue(null)
                }
                fileChooserCallback = null
                cameraImageUri = null
            }
        }
    }
    
    // 请求必要的权限
    private fun requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Android 6.0以下版本，权限在安装时授予
            return
        }
        
        val permissionsToRequest = mutableListOf<String>()
        
        // 添加必要的权限
        permissionsToRequest.addAll(REQUIRED_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        })
        
        // 添加额外的权限
        permissionsToRequest.addAll(additionalPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        })
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_CODE_PERMISSIONS
            )
        }
        
        // Android 13+ 检查是否需要请求所有文件访问权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkAndRequestAllFilesAccessPermission()
        }
    }
    
    // 检查并请求所有文件访问权限（Android 13+）
    private fun checkAndRequestAllFilesAccessPermission() {
        if (!Environment.isExternalStorageManager()) {
            // 未获得所有文件访问权限，显示对话框引导用户前往设置页面开启
            AlertDialog.Builder(this)
                .setTitle("需要文件访问权限")
                .setMessage("为了能够访问所有类型的本地文件，应用需要获得\"所有文件访问权限\". 请在设置页面中开启此权限。")
                .setPositiveButton("前往设置") { _, _ ->
                    // 跳转到应用设置页面
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                }
                .setNegativeButton("取消") { _, _ ->
                    // 显示提示信息，告知用户某些功能可能无法正常使用
                    Toast.makeText(this, "未授予所有文件访问权限，部分功能可能无法正常使用", Toast.LENGTH_LONG).show()
                }
                .show()
        }
    }
}

@Composable
fun WebViewScreen(modifier: Modifier = Modifier, onWebViewCreated: (WebView) -> Unit) {
    Box(modifier = modifier.fillMaxSize()) {
        // 下拉刷新组件 - 使用原生的SwipeRefreshLayout
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                val swipeRefreshLayout = SwipeRefreshLayout(context)
                
                // 创建WebView
                val webView = WebView(context)
                onWebViewCreated(webView)
                
                // 配置WebView
                configureWebView(webView, swipeRefreshLayout, context)
                
                // 将WebView添加到SwipeRefreshLayout
                swipeRefreshLayout.addView(webView)
                
                // 设置刷新监听器
                swipeRefreshLayout.setOnRefreshListener {
                    // 直接重新加载当前页面，移除不必要的Handler延迟
                    webView.loadUrl(webView.url ?: BASE_URL)
                }
                
                // 返回SwipeRefreshLayout
                swipeRefreshLayout
            }
        )
    }
}

// 优化代码结构：将WebView配置逻辑拆分到单独函数
private fun configureWebView(webView: WebView, swipeRefreshLayout: SwipeRefreshLayout, context: Context) {
    // 启用基本Cookie支持
    val cookieManager = CookieManager.getInstance()
    cookieManager.setAcceptCookie(true)
    
    // 配置WebView设置
    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        loadWithOverviewMode = true
        useWideViewPort = true
        builtInZoomControls = true
        displayZoomControls = false
        setSupportZoom(true)
        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        
        // 启用文件访问和上传
        allowFileAccess = true
        allowContentAccess = true
        
        // 设置UserAgent
        userAgentString = "Mozilla/5.0 (Linux; Android ${android.os.Build.VERSION.RELEASE}) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/${android.os.Build.VERSION.RELEASE} Mobile Safari/537.36"
    }
    
    // 配置WebViewClient
    webView.webViewClient = createWebViewClient(swipeRefreshLayout)
    
    // 配置WebChromeClient以支持文件上传
    webView.webChromeClient = object : WebChromeClient() {
        // Android 5.0+ 处理文件选择
        override fun onShowFileChooser(
            webView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            // 保存回调
            (context as MainActivity).fileChooserCallback = filePathCallback
            
            // 检查是否支持多文件选择
            val isMultipleSelection = fileChooserParams.mode == FileChooserParams.MODE_OPEN_MULTIPLE
            
            // 创建Intent列表，包含文件选择和相机拍摄
            val intents = mutableListOf<Intent>()
            
            // 添加文件选择Intent，支持所有文件类型
            val fileIntent = Intent(Intent.ACTION_GET_CONTENT)
            fileIntent.type = "*/*"
            fileIntent.addCategory(Intent.CATEGORY_OPENABLE)
            
            if (isMultipleSelection) {
                fileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            
            intents.add(fileIntent)
            
            // 检查相机权限并添加相机Intent
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (cameraIntent.resolveActivity(context.packageManager) != null) {
                    // 创建临时文件保存相机拍摄的图片
                    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    try {
                        val tempFile = File.createTempFile(
                            "temp_image_",
                            ".jpg",
                            storageDir
                        )
                        (context as MainActivity).cameraImageUri = FileProvider.getUriForFile(
                            context,
                            context.packageName + ".fileprovider",
                            tempFile
                        )
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, (context as MainActivity).cameraImageUri)
                        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        intents.add(cameraIntent)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            
            // 启动文件选择器（如果有相机选项，会显示选择对话框）
            if (intents.size > 1) {
                (context as MainActivity).startActivityForResult(
                    Intent.createChooser(intents.removeAt(0), "选择文件或拍照").apply {
                        putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())
                    },
                    REQUEST_CODE_FILE_PICKER
                )
            } else {
                (context as MainActivity).startActivityForResult(
                    Intent.createChooser(intents[0], "选择文件"),
                    REQUEST_CODE_FILE_PICKER
                )
            }
            
            return true
        }
        
        // 旧版openFileChooser方法（Android 3.0+）
        @SuppressWarnings("unused")
        fun openFileChooser(
            filePathCallback: ValueCallback<Uri>,
            acceptType: String
        ) {
            this.openFileChooser(filePathCallback, acceptType, null)
        }
        
        @SuppressWarnings("unused")
        fun openFileChooser(
            filePathCallback: ValueCallback<Uri>,
            acceptType: String,
            capture: String?
        ) {
            // 创建文件选择Intent
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = acceptType.ifEmpty { "*/*" }
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            
            // 保存回调，转换为Array<Uri>类型
            (context as MainActivity).fileChooserCallback = object : ValueCallback<Array<Uri>> {
                override fun onReceiveValue(value: Array<Uri>?) {
                    filePathCallback.onReceiveValue(value?.firstOrNull())
                }
            }
            
            // 启动文件选择器
            (context as MainActivity).startActivityForResult(
                Intent.createChooser(intent, "选择文件"),
                REQUEST_CODE_FILE_PICKER
            )
        }
        
        // Android 2.2+ 处理文件选择
        @SuppressWarnings("unused")
        fun openFileChooser(filePathCallback: ValueCallback<Uri>) {
            this.openFileChooser(filePathCallback, "", null)
        }
    }
    
    // 设置WebView背景透明
    webView.setBackgroundColor(0x00000000)
    
    // 加载初始URL
    webView.loadUrl(BASE_URL)
}

// 优化代码结构：将WebViewClient创建逻辑拆分到单独函数
private fun createWebViewClient(swipeRefreshLayout: SwipeRefreshLayout): WebViewClient {
    return object : WebViewClient() {
        private val visitedUrls = mutableSetOf<String>()
        private var redirectCount = 0
        
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
            
            // 防止循环重定向
            redirectCount++
            if (redirectCount > MAX_REDIRECTS) {
                view.loadUrl(BASE_URL) // 重置到首页
                return true
            }
            
            // 如果URL已经访问过，可能是循环
            if (visitedUrls.contains(url)) {
                return true
            }
            
            // 优化：限制集合大小，防止内存问题
            if (visitedUrls.size >= MAX_VISITED_URLS) {
                visitedUrls.clear()
            }
            visitedUrls.add(url)
            
            // 只处理HTTP/HTTPS链接
            return !(url.startsWith("http://") || url.startsWith("https://"))
        }
        
        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)
            // 页面加载完成后重置状态
            redirectCount = 0
            
            // 优化：对于主要页面，清空已访问URL集合以允许新的导航
            if (url != null && (
                url == BASE_URL || 
                url.startsWith("$BASE_URL/thread-") || 
                url.startsWith("$BASE_URL/forum-")
            )) {
                visitedUrls.clear()
            }
            
            // 页面加载完成后停止刷新
            swipeRefreshLayout.isRefreshing = false
        }
        
        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            // 安全优化：添加配置选项控制是否忽略SSL错误
            if (IGNORE_SSL_ERRORS) {
                handler.proceed()
            } else {
                handler.cancel()
            }
        }
    }
}