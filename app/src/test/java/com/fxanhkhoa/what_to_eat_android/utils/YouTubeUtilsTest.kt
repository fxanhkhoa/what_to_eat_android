package com.fxanhkhoa.what_to_eat_android.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YouTubeUtilsTest {

    @Test
    fun `extract id from various youtube urls`() {
        val samples = mapOf(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://www.youtube.com/embed/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://www.youtube.com/v/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=30s" to "dQw4w9WgXcQ",
            "https://www.youtube.com/watch?time_continue=1&v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://youtube.com/shorts/dQw4w9WgXcQ" to "dQw4w9WgXcQ"
        )

        for ((input, expected) in samples) {
            val id = YouTubeUtils.extractYouTubeId(input)
            assertEquals("Failed for: $input", expected, id)
        }
    }

    @Test
    fun `returns null for invalid inputs`() {
        assertNull(YouTubeUtils.extractYouTubeId(null))
        assertNull(YouTubeUtils.extractYouTubeId(""))
        assertNull(YouTubeUtils.extractYouTubeId("https://example.com/watch?v=short"))
        assertNull(YouTubeUtils.extractYouTubeId("not a url or id"))
    }
}

