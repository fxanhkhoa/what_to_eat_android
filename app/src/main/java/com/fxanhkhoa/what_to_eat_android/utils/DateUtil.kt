package com.fxanhkhoa.what_to_eat_android.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {
    private const val ISO_DATE_FORMAT_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private const val ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    private const val DISPLAY_DATE_FORMAT = "MMM dd, yyyy"

    fun formatDate(dateString: String): String {
        return try {
            val date = parseIsoDate(dateString)
            val displayFormat = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
            date?.let { displayFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatDateTime(dateString: String): String {
        return try {
            val date = parseIsoDate(dateString)
            val displayFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            date?.let { displayFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    private fun parseIsoDate(dateString: String): Date? {
        return try {
            // First try with milliseconds format
            val isoFormatWithMillis = SimpleDateFormat(ISO_DATE_FORMAT_WITH_MILLIS, Locale.getDefault())
            isoFormatWithMillis.timeZone = TimeZone.getTimeZone("UTC")
            isoFormatWithMillis.parse(dateString)
        } catch (e: Exception) {
            try {
                // Fallback to format without milliseconds
                val isoFormat = SimpleDateFormat(ISO_DATE_FORMAT, Locale.getDefault())
                isoFormat.timeZone = TimeZone.getTimeZone("UTC")
                isoFormat.parse(dateString)
            } catch (e: Exception) {
                null
            }
        }
    }
}
