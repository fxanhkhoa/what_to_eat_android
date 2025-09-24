package com.fxanhkhoa.what_to_eat_android.utils

import android.net.Uri

/**
 * Utilities for YouTube related helpers.
 */
object YouTubeUtils {
    // Enforce full-match for exactly 11 allowed characters
    private val idRegex = Regex("^[A-Za-z0-9_-]{11}$")

    /**
     * Extracts a YouTube video id (11 chars) from a URL or raw id string.
     * Returns null when no valid id can be found.
     */
    fun extractYouTubeId(input: String?): String? {
        if (input == null) return null
        val trimmed = input.trim()

        // Quick path: if input already looks like a plain id
        if (idRegex.matches(trimmed)) return trimmed

        // Try parsing as URI and look for common locations
        try {
            val uri = Uri.parse(trimmed)

            // 1) Check query parameter 'v'
            uri.getQueryParameter("v")?.let { v ->
                if (idRegex.matches(v)) return v
            }

            // 2) Handle youtu.be short links and path-based ids
            val host = uri.host ?: ""
            val path = uri.path?.trim('/') ?: ""

            if (host.contains("youtu.be", ignoreCase = true)) {
                val candidate = path.split('/').firstOrNull() ?: ""
                if (idRegex.matches(candidate)) return candidate
            }

            // 3) Handle /embed/<id>, /v/<id>, /shorts/<id> or other path segments
            val segments = path.split('/').filter { it.isNotBlank() }
            for (seg in segments.reversed()) {
                if (idRegex.matches(seg)) return seg
            }
        } catch (e: Exception) {
            // ignore and fallback to regex
        }

        // 4) Fallback: regex search in the whole string for various patterns, include /shorts/
        val fallbackRegex = Regex("(?:youtu\\.be/|youtube\\.com\\S*(?:/embed/|/v/|/shorts/|/watch\\?v=|\\S*?v=))([A-Za-z0-9_-]{11})", RegexOption.IGNORE_CASE)
        val match = fallbackRegex.find(trimmed)
        return match?.groups?.get(1)?.value
    }
}
