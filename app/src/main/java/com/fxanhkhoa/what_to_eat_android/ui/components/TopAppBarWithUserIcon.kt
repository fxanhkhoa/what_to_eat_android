package com.fxanhkhoa.what_to_eat_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fxanhkhoa.what_to_eat_android.utils.rememberSharedAuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithUserIcon(
    modifier: Modifier = Modifier,
    title: String = "What to Eat",
    onUserIconClick: () -> Unit = {}
) {
    val authViewModel = rememberSharedAuthViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val user by authViewModel.user.collectAsState()

    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        actions = {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        color = if (isLoggedIn) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        },
                        shape = CircleShape
                    )
                    .clickable { onUserIconClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLoggedIn) Icons.Filled.Person else Icons.Filled.AccountCircle,
                    contentDescription = if (isLoggedIn) "User Profile (${user?.name ?: "Logged In"})" else "User Profile (Unauthorized)",
                    tint = if (isLoggedIn) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}
