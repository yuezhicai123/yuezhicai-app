package cn.xiaizizi

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.net.http.SslError
import android.webkit.SslErrorHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import cn.xiaizizi.ui.theme.DiscuzTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiscuzTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    WebViewScreen(modifier = Modifier.padding(it))
                }
            }
        }
    }
}

@Composable
fun WebViewScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context: android.content.Context ->
                val webView = WebView(context)
                
                // 启用Cookie支持以确保QQ登录会话正常
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(webView, true)
                
                webView.apply {
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            val url = request.url.toString()
                            
                            // 处理所有HTTP和HTTPS链接，让WebView继续加载
                            // 包括QQ登录页面和回调URL
                            if (url.startsWith("http://") || url.startsWith("https://")) {
                                // 让WebView自己处理所有网络请求，包括QQ登录回调
                                return false
                            } else {
                                // 处理自定义协议（如qqlogin://）
                                // 如果有需要，可以在这里添加特殊处理逻辑
                                return true
                            }
                        }
                        
                        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                            // 忽略SSL错误（生产环境不建议这样做）
                            handler.proceed()
                        }
                    }
                    
                    // 优化WebView设置以适配不同屏幕和支持QQ登录
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        setSupportZoom(true)
                        // 启用混合内容（HTTP和HTTPS）
                        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                        
                        // 启用第三方Cookie支持（QQ登录需要）
                        allowContentAccess = true
                        allowFileAccess = true
                        allowFileAccessFromFileURLs = false
                        allowUniversalAccessFromFileURLs = false
                        
                        // 设置UserAgent以模拟真实浏览器
                        userAgentString = "Mozilla/5.0 (Linux; Android ${android.os.Build.VERSION.RELEASE}) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/${android.os.Build.VERSION.RELEASE} Mobile Safari/537.36"
                        
                        // 启用表单自动填充
                        saveFormData = true
                        
                        // 提高渲染性能
                        setRenderPriority(WebSettings.RenderPriority.HIGH)
                    }
                    
                    // 加载网站
                    loadUrl("https://xiaizizi.cn/")
                    
                    // 设置WebView背景透明
                    setBackgroundColor(0x00000000)
                }
            }
        )
    }
}