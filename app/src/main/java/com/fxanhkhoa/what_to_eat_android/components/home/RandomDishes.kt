package com.fxanhkhoa.what_to_eat_android.components.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.components.dish.DishCardFancy
import com.fxanhkhoa.what_to_eat_android.model.DishModel
import com.fxanhkhoa.what_to_eat_android.services.DishService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import kotlinx.coroutines.flow.first
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider.createService

@Composable
fun RandomDishes(
    modifier: Modifier = Modifier,
    refreshTrigger: Int = 0, // Add refresh trigger parameter
    navController: NavController? = null
) {
    var dishes by remember { mutableStateOf<List<DishModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fetch dishes when refreshTrigger changes
    LaunchedEffect(refreshTrigger) {
        isLoading = true
        error = null
        val service = createService<DishService>()
        try {
            val result = withContext(Dispatchers.IO) {
                service.findRandom(limit = 5)
            }
            dishes = result
        } catch (e: Exception) {
            error = e.localizedMessage ?: "Unknown error"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            isLoading -> {
                Box(modifier = Modifier.height(180.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(modifier = Modifier.height(180.dp), contentAlignment = Alignment.Center) {
                    Text(text = error ?: "Error", color = MaterialTheme.colorScheme.error)
                }
            }

            dishes.isEmpty() -> {
                Box(modifier = Modifier.height(180.dp), contentAlignment = Alignment.Center) {
                    Text(text = "No random dishes found.")
                }
            }

            else -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    dishes.forEach { dish ->
                        DishCardFancy(
                            dish = dish,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            onClick = {
                                navController?.navigate("dish/${dish.slug}")
                            },

                            )
                    }
                }
            }
        }
    }
}
