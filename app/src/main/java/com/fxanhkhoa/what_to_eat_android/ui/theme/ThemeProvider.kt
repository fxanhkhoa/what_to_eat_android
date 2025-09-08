package com.fxanhkhoa.what_to_eat_android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun rememberThemeState(): ThemeState {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val isSystemDark = isSystemInDarkTheme()

    val followSystemTheme by themeManager.followSystemTheme.collectAsState(initial = true)
    val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = false)
    val useDynamicColor by themeManager.useDynamicColor.collectAsState(initial = true)

    val effectiveDarkTheme = if (followSystemTheme) isSystemDark else isDarkTheme

    return remember(followSystemTheme, isDarkTheme, useDynamicColor, effectiveDarkTheme) {
        ThemeState(
            isDarkTheme = effectiveDarkTheme,
            followSystemTheme = followSystemTheme,
            useDynamicColor = useDynamicColor,
            toggleTheme = {
                coroutineScope.launch {
                    themeManager.setDarkTheme(!isDarkTheme)
                    themeManager.setFollowSystemTheme(false)
                }
            },
            setFollowSystemTheme = { follow ->
                coroutineScope.launch {
                    themeManager.setFollowSystemTheme(follow)
                }
            },
            setUseDynamicColor = { useDynamic ->
                coroutineScope.launch {
                    themeManager.setDynamicColor(useDynamic)
                }
            }
        )
    }
}

data class ThemeState(
    val isDarkTheme: Boolean,
    val followSystemTheme: Boolean,
    val useDynamicColor: Boolean,
    val toggleTheme: () -> Unit,
    val setFollowSystemTheme: (Boolean) -> Unit,
    val setUseDynamicColor: (Boolean) -> Unit
)

val LocalThemeState = compositionLocalOf<ThemeState> {
    error("No ThemeState provided")
}

@Composable
fun ThemeProvider(
    content: @Composable () -> Unit
) {
    val themeState = rememberThemeState()

    CompositionLocalProvider(LocalThemeState provides themeState) {
        What_to_eat_androidTheme(
            darkTheme = themeState.isDarkTheme,
            dynamicColor = themeState.useDynamicColor,
            content = content
        )
    }
}
