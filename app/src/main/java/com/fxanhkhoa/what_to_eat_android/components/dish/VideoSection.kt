package com.fxanhkhoa.what_to_eat_android.components.dish

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.utils.YouTubeUtils
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView


@Composable
fun YouTubePlayerWebView(
    videoId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Store the original orientation to restore it later
    val originalOrientation = remember {
        (context as? Activity)?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    DisposableEffect(Unit) {
        onDispose {
            // Cleanup will be handled by the YouTubePlayerView itself
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val iFramePlayerOptions = IFramePlayerOptions.Builder(ctx)
                .controls(1) // enable player controls
                .fullscreen(1) // enable fullscreen button
                .build()

            YouTubePlayerView(ctx).apply {
                // Disable automatic initialization since we're initializing manually
                enableAutomaticInitialization = false

                // Add lifecycle observer for proper lifecycle management
                lifecycleOwner.lifecycle.addObserver(this)

                // Add fullscreen listener to enable fullscreen functionality
                addFullscreenListener(object : FullscreenListener {
                    override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: () -> Unit) {
                        // Get the activity's root view
                        val activity = context as? Activity
                        val rootView = activity?.window?.decorView?.findViewById<ViewGroup>(android.R.id.content)

                        if (rootView != null && activity != null) {
                            // Force landscape orientation - use LANDSCAPE instead of SENSOR_LANDSCAPE
                            // This works even when device auto-rotate is disabled
                            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                            // Wait for rotation to complete before adding fullscreen view
                            Handler(Looper.getMainLooper()).postDelayed({
                                // Hide system UI for immersive fullscreen
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    activity.window.insetsController?.let { controller ->
                                        controller.hide(WindowInsets.Type.systemBars())
                                        controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                                    }
                                } else {
                                    @Suppress("DEPRECATION")
                                    activity.window.decorView.systemUiVisibility = (
                                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                    )
                                }

                                // Add fullscreen view to activity's root
                                rootView.addView(fullscreenView, ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                ))
                            }, 300) // 300ms delay for rotation animation
                        }
                    }

                    override fun onExitFullscreen() {
                        // Get the activity's root view
                        val activity = context as? Activity
                        val rootView = activity?.window?.decorView?.findViewById<ViewGroup>(android.R.id.content)

                        if (rootView != null && rootView.childCount > 1 && activity != null) {
                            // Restore original orientation
                            activity.requestedOrientation = originalOrientation

                            // Restore system UI
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                activity.window.insetsController?.show(WindowInsets.Type.systemBars())
                            } else {
                                @Suppress("DEPRECATION")
                                activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                            }

                            // Remove the fullscreen view (last child added)
                            rootView.removeViewAt(rootView.childCount - 1)
                        }
                    }
                })

                // Initialize with IFramePlayerOptions to enable fullscreen and controls
                initialize(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        super.onReady(youTubePlayer)
                        // Cue the video (don't autoplay)
                        youTubePlayer.cueVideo(videoId, 0f)
                    }
                }, iFramePlayerOptions)
            }
        },
        update = { _ ->
            // Video will auto-update when videoId changes
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

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
