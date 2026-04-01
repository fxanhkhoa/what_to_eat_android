package com.fxanhkhoa.what_to_eat_android.utils

/**
 * Lightweight singleton to track whether the app is currently in the foreground.
 * Updated by MainActivity lifecycle callbacks.
 */
object AppState {
    /** True when at least one Activity is in the STARTED/RESUMED state */
    @Volatile
    var isInForeground: Boolean = false
}

