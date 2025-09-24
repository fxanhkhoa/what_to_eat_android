package com.fxanhkhoa.what_to_eat_android.components.dish

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MealCategoriesSection(
    mealCategories: List<String>,
    modifier: Modifier = Modifier,
    onCategoryClick: (String) -> Unit = {}
) {
    if (mealCategories.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            mealCategories.forEachIndexed { index, category ->
                Button(
                    onClick = { onCategoryClick(category) },
                    modifier = Modifier
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (index % 5) {
                            0 -> MaterialTheme.colorScheme.primary
                            1 -> MaterialTheme.colorScheme.secondary
                            2 -> MaterialTheme.colorScheme.tertiary
                            3 -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    )
                ) {
                    Text(
                        text = category,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}
