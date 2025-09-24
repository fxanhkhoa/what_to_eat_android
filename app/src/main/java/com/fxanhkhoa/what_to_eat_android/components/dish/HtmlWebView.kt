package com.fxanhkhoa.what_to_eat_android.components.dish

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.toArgb
import android.view.MotionEvent
import android.view.View

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HtmlWebView(
    htmlContent: String,
    modifier: Modifier = Modifier,
    onInteractionChanged: (Boolean) -> Unit = {}
) {
    // Get secondary color for text in dark mode
    val secondaryColor = MaterialTheme.colorScheme.onSurface
    val backgroundColor = MaterialTheme.colorScheme.background

    // Convert Compose Color to CSS hex string (#RRGGBB)
    val secondaryColorHex = String.format("#%06X", 0xFFFFFF and secondaryColor.toArgb())
    val backgroundColorHex = String.format("#%06X", 0xFFFFFF and backgroundColor.toArgb())

    // Calculate htmlString here, it will be recalculated on recomposition
    // if htmlContent or darkMode changes.
    val currentHtmlString = """
        <html>
        <head>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <style>
        html, body {
          height: 100%;
          margin: 0;
          padding: 0;
          -webkit-text-size-adjust: 100%;
        }
        body {
          box-sizing: border-box;
          font-family: '-apple-system', 'HelveticaNeue', 'Helvetica', 'Arial', sans-serif;
          color: $secondaryColorHex;
          background-color: $backgroundColorHex;
          padding: 20px;
          overflow-y: auto;
          -webkit-overflow-scrolling: touch;
        }
        img { max-width: 100%; height: auto; }
        </style>
        </head>
        <body>
        $htmlContent
        </body>
        </html>
    """

    // Persist the WebView reference across recompositions
    val webViewState = remember { mutableStateOf<WebView?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            @SuppressLint("SetJavaScriptEnabled")
            val webView = WebView(ctx).apply {
                setBackgroundColor(AndroidColor.TRANSPARENT)
                settings.javaScriptEnabled = false
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.domStorageEnabled = true
                isFocusable = true
                isFocusableInTouchMode = true
                requestFocus()
                webViewClient = WebViewClient()

                // enable scrollbars for visibility
                isVerticalScrollBarEnabled = true
                overScrollMode = View.OVER_SCROLL_ALWAYS

                // IMPORTANT: When this WebView is placed inside a Compose scrollable parent
                // (for example a Column with verticalScroll), touches may be intercepted by the
                // parent which leads to bad touch/scroll interop and occasionally crashes.
                // Here we request that the parent not intercept touch events while the user is
                // actively interacting with this WebView. This makes it safe to keep a
                // Compose parent with verticalScroll if desired.
                setOnTouchListener(View.OnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> v.parent?.requestDisallowInterceptTouchEvent(true)
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                    // Return false so that WebView still handles the touch event normally.
                    false
                })
            }

            // initial load once
            webView.loadDataWithBaseURL(null, currentHtmlString, "text/html", "utf-8", null)
            webView.tag = currentHtmlString
            webViewState.value = webView
            webView
        },
        update = { webView ->
            // Only reload if content actually changes - avoid interrupting touch handling
            if (webView.tag != currentHtmlString) {
                webView.loadDataWithBaseURL(null, currentHtmlString, "text/html", "utf-8", null)
                webView.tag = currentHtmlString
            }
        }
    )
}
