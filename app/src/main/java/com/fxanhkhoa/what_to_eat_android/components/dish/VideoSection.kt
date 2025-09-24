package com.fxanhkhoa.what_to_eat_android.components.dish

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.utils.YouTubeUtils

@Composable
fun YouTubePlayerWebView(
    videoId: String,
    modifier: Modifier = Modifier
) {
    val html = remember(videoId) {
        // Minimal, responsive iframe embed
        """
        <html>
        <head>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <style> html,body{margin:0;padding:0;background:transparent;} .video{position:relative;padding-bottom:56.25%;height:0;overflow:hidden;} .video iframe{position:absolute;top:0;left:0;width:100%;height:100%;border:0;} </style>
        </head>
        <body>
        <div class="video">
        <iframe src="https://www.youtube.com/embed/$videoId" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
        </div>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { webView: WebView ->
            webView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null)
        }
    )
}

@Composable
fun VideoSection(
    videos: List<String>,
    modifier: Modifier = Modifier,
    onOpenUrl: (String) -> Unit = {},
    language: Language = Language.ENGLISH
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    val title = try {
        localizationManager.getString(R.string.videos, language)
    } catch (e: Exception) {
        "Videos"
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            videos.forEach { video ->
                val id = YouTubeUtils.extractYouTubeId(video)
                if (id != null) {
                    YouTubePlayerWebView(
                        videoId = id,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(bottom = 8.dp)
                    )
                } else {
                    // Show clickable link text
                    Text(
                        text = video,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOpenUrl(video)
                                // default behavior: open external browser
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    // ignore if malformed
                                }
                            }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
