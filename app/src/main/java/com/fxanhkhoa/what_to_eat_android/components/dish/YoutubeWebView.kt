package com.fxanhkhoa.what_to_eat_android.components.dish

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun YoutubeWebView(
    videoId: String,
    modifier: Modifier = Modifier
) {
    val embedHtml = """
        <html><body style='margin:0;padding:0;'>
        <iframe width='100%' height='100%' src='https://www.youtube.com/embed/$videoId?playsinline=1' frameborder='0' allowfullscreen></iframe>
        </body></html>
    """
    AndroidView(
        modifier = modifier,
        factory = { context ->
            @SuppressLint("SetJavaScriptEnabled")
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                setBackgroundColor(0x00000000)
                webViewClient = WebViewClient()
                loadDataWithBaseURL(null, embedHtml, "text/html", "utf-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, embedHtml, "text/html", "utf-8", null)
        }
    )
}
