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
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import cn.xiaizizi.ui.theme.DiscuzTheme

class MainActivity : ComponentActivity() {
    // 保存WebView引用
    private var webView: WebView? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
                    WebViewScreen(modifier = Modifier.padding(it)) { view ->
                        webView = view
                    }
                }
            }
        }
    }
}

@Composable
fun WebViewScreen(modifier: Modifier = Modifier, onWebViewCreated: (WebView) -> Unit) {
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context: android.content.Context ->
                val webView = WebView(context)
                // 传递WebView引用给MainActivity
                onWebViewCreated(webView)
                
                // 启用基本Cookie支持
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                
                webView.apply {
                    webViewClient = object : WebViewClient() {
                        private val visitedUrls = mutableSetOf<String>()
                        private val MAX_REDIRECTS = 5 // 恢复正常的重定向限制
                        private var redirectCount = 0
                        
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            val url = request.url.toString()
                            
                            // 防止循环重定向
                            redirectCount++
                            if (redirectCount > MAX_REDIRECTS) {
                                // 显示错误信息或处理
                                view.loadUrl("about:blank")
                                view.loadUrl("https://xiaizizi.cn/") // 重置到首页
                                return true // 停止加载以防止无限循环
                            }
                            
                            // 如果URL已经访问过，可能是循环
                            if (visitedUrls.contains(url)) {
                                return true
                            }
                            
                            // 添加到已访问URL集合（限制集合大小，防止内存问题）
                            if (visitedUrls.size > 50) {
                                visitedUrls.clear()
                            }
                            visitedUrls.add(url)
                            
                            // 处理所有HTTP和HTTPS链接，让WebView继续加载
                            if (url.startsWith("http://") || url.startsWith("https://")) {
                                return false
                            } else {
                                // 处理自定义协议
                                return true
                            }
                        }
                        
                        override fun onPageFinished(view: WebView, url: String?) {
                            super.onPageFinished(view, url)
                            // 页面加载完成后重置重定向计数
                            redirectCount = 0
                            // 对于主要页面，清空已访问URL集合以允许新的导航
                            if (url != null && (url == "https://xiaizizi.cn/" || url.startsWith("https://xiaizizi.cn/thread-") || url.startsWith("https://xiaizizi.cn/forum-"))) {
                                visitedUrls.clear()
                            }
                        }
                        
                        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                            // 忽略SSL错误（生产环境不建议这样做）
                            handler.proceed()
                        }
                    }
                    
                    // WebView基本设置
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        setSupportZoom(true)
                        
                        // 设置UserAgent
                        userAgentString = "Mozilla/5.0 (Linux; Android ${android.os.Build.VERSION.RELEASE}) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/${android.os.Build.VERSION.RELEASE} Mobile Safari/537.36"
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