package com.fxanhkhoa.what_to_eat_android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import kotlinx.coroutines.flow.first

private data class PolicySection(
    val title: String,
    val body: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {}
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var currentLanguage by remember { mutableStateOf(Language.ENGLISH) }

    LaunchedEffect(Unit) {
        currentLanguage = localizationManager.currentLanguage.first()
    }

    val str = remember(currentLanguage) {
        fun s(id: Int) = localizationManager.getString(id, currentLanguage)
        object {
            val title       = s(R.string.privacy_policy)
            val lastUpdated = s(R.string.privacy_policy_last_updated)
            val sections = listOf(
                PolicySection(s(R.string.privacy_policy_s1_title),   s(R.string.privacy_policy_s1_body)),
                PolicySection(s(R.string.privacy_policy_s2_title),   ""),
                PolicySection(s(R.string.privacy_policy_s2_1_title), s(R.string.privacy_policy_s2_1_body)),
                PolicySection(s(R.string.privacy_policy_s2_2_title), s(R.string.privacy_policy_s2_2_body)),
                PolicySection(s(R.string.privacy_policy_s2_3_title), s(R.string.privacy_policy_s2_3_body)),
                PolicySection(s(R.string.privacy_policy_s3_title),   s(R.string.privacy_policy_s3_body)),
                PolicySection(s(R.string.privacy_policy_s4_title),   s(R.string.privacy_policy_s4_body)),
                PolicySection(s(R.string.privacy_policy_s5_title),   s(R.string.privacy_policy_s5_body)),
                PolicySection(s(R.string.privacy_policy_s6_title),   s(R.string.privacy_policy_s6_body)),
                PolicySection(s(R.string.privacy_policy_s7_title),   s(R.string.privacy_policy_s7_body)),
                PolicySection(s(R.string.privacy_policy_s8_title),   s(R.string.privacy_policy_s8_body)),
                PolicySection(s(R.string.privacy_policy_s9_title),   s(R.string.privacy_policy_s9_body)),
                PolicySection(s(R.string.privacy_policy_s10_title),  s(R.string.privacy_policy_s10_body)),
                PolicySection(s(R.string.privacy_policy_s11_title),  s(R.string.privacy_policy_s11_body)),
                PolicySection(s(R.string.privacy_policy_s12_title),  s(R.string.privacy_policy_s12_body))
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = str.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Last updated chip
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = str.lastUpdated,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // Policy sections
            items(str.sections) { section ->
                PolicySectionCard(section = section)
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun PolicySectionCard(section: PolicySection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = section.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (section.body.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = section.body,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


