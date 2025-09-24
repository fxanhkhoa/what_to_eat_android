package com.fxanhkhoa.what_to_eat_android.components.dish

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
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
fun DescriptionSection(
    description: String,
    modifier: Modifier = Modifier,
    language: Language = Language.ENGLISH
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }

    val title = try {
        localizationManager.getString(R.string.description, language)
    } catch (e: Exception) {
        "Description"
    }

    val darkMode = isSystemInDarkTheme()
    val backgroundColor = if (darkMode) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background
    val strokeColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        // Styled description box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = backgroundColor, shape = RoundedCornerShape(12.dp))
                .border(width = 1.dp, color = strokeColor, shape = RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
