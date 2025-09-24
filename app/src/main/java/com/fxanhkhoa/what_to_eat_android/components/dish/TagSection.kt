package com.fxanhkhoa.what_to_eat_android.components.dish

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
fun TagSection(
    tags: List<String>,
    modifier: Modifier = Modifier,
    onTagClick: (String) -> Unit = {},
    language: Language
) {
    if (tags.isEmpty()) return

    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    // Try to use compile-time resource R.string.tags, fallback to hardcoded
    val title = try {
        localizationManager.getString(R.string.tags, language)
    } catch (e: Exception) {
        "Tags"
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(start = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tags.forEach { tag ->
                OutlinedButton(
                    onClick = { onTagClick(tag) },
                    content = {
                        Text(text = tag, style = MaterialTheme.typography.bodyLarge)
                    },
                    colors = ButtonDefaults.outlinedButtonColors()
                )
            }
        }
    }
}
