package com.fxanhkhoa.what_to_eat_android.components.home

import android.widget.Button
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.fxanhkhoa.what_to_eat_android.R
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeBanner(
    navController: NavController,
    modifier: Modifier = Modifier,
    onNavigateToDish: () -> Unit
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var currentLanguage by remember { mutableStateOf(Language.ENGLISH) }

    val bannerTitle = remember(currentLanguage) {
        localizationManager.getString(
            R.string.home_banner_title,
            currentLanguage
        )
    }

    val buttonText = remember(currentLanguage) {
        localizationManager.getString(
            R.string.home_banner_button,
            currentLanguage
        )
    }

    // Observe language changes
    LaunchedEffect(Unit) {
        currentLanguage = localizationManager.currentLanguage.first()
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(modifier = modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(R.drawable.hero_bread),
                contentDescription = "Home Banner",
                modifier = modifier.fillMaxWidth()
            )

            Image(
                painter = painterResource(R.drawable.splash_3),
                contentDescription = "Splash Image",
                modifier = modifier
                    .width(200.dp)
                    .height(200.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 0.dp, y = (-25).dp)
            )

            Image(
                painter = painterResource(R.drawable.what_to_eat_high_resolution_logo_black_transparent),
                contentDescription = "Logo",
                modifier = modifier
                    .width(135.dp)
                    .height(60.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 25.dp, y = 50.dp)
            )

            Text(
                text = bannerTitle,
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFFFFFFFF),
                modifier = modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 25.dp, y = (-25).dp)
            )
        }

        Button(
            onClick = { onNavigateToDish() },
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 0.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            )
        ) {
            Text(text = buttonText)
        }
    }
}