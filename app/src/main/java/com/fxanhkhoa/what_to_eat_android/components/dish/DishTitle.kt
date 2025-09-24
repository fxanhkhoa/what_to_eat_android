package com.fxanhkhoa.what_to_eat_android.components.dish

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.shared.contant.DishTitleHorizontalGradient

@Composable
fun DishTitleSection(
    dish: DishModel,
    modifier: Modifier = Modifier,
    language: Language = Language.ENGLISH
) {
    val title = remember(dish, language) {
        dish.title.firstOrNull { it.lang == language.code }?.data ?: "Unknown Dish"
    }

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                brush = DishTitleHorizontalGradient
            ),
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}
