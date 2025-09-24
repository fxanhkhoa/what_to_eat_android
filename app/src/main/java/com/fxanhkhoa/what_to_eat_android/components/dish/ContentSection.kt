package com.fxanhkhoa.what_to_eat_android.components.dish

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager

@Composable
fun ContentSection(
    content: String,
    modifier: Modifier = Modifier,
    language: Language = Language.ENGLISH
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    val title = try {
        localizationManager.getString(R.string.recipe_content, language)
    } catch (_: Exception) {
        "Recipe"
    }

    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val strokeColor = MaterialTheme.colorScheme.outline

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        // Card-like container for HTML content with rounded border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // Keep a reasonable minimum height but constrain the maximum so
                // the inner WebView can scroll instead of expanding the whole page.
                .heightIn(min = 400.dp, max = 500.dp)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(width = 1.dp, color = strokeColor, shape = RoundedCornerShape(12.dp))
                .padding(8.dp)
                // IMPORTANT: Do NOT add Compose's verticalScroll here. The child is an Android
                // WebView (AndroidView) which manages its own touch/scrolling. Wrapping it with
                // verticalScroll can cause touch/scroll interop problems and crashes.
        ) {
            // HtmlWebView is already implemented in the project
            HtmlWebView(
                htmlContent = content,
                modifier = Modifier
                    .fillMaxSize(),
            )
        }
    }
}
