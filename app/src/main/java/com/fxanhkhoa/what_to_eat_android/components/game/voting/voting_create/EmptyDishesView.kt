package com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_create

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import androidx.compose.ui.platform.LocalContext
import com.fxanhkhoa.what_to_eat_android.R

@Composable
fun EmptyDishesView(language: Language = Language.ENGLISH) {
    val context = LocalContext.current
    val localizationManager = LocalizationManager(context)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.empty),
            modifier = Modifier.size(40.dp),
            contentDescription = null
        )
        Text(
            text = localizationManager.getString(R.string.no_dishes_selected, language),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

