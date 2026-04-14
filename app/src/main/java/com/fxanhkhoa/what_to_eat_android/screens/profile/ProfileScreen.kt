package com.fxanhkhoa.what_to_eat_android.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.fxanhkhoa.what_to_eat_android.data.dto.User
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.utils.rememberSharedAuthViewModel
import com.fxanhkhoa.what_to_eat_android.utils.rememberGoogleSignInHelper
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToMyLists: () -> Unit = {}
) {
    val authViewModel = rememberSharedAuthViewModel()
    val googleSignInHelper = rememberGoogleSignInHelper()
    val user by authViewModel.user.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    var currentLanguage by remember { mutableStateOf(Language.ENGLISH) }
    LaunchedEffect(Unit) {
        currentLanguage = localizationManager.currentLanguage.first()
    }

    // Localized strings
    val str = remember(currentLanguage) {
        fun s(id: Int) = localizationManager.getString(id, currentLanguage)
        object {
            val title                 = s(R.string.profile)
            val sectionAccount        = s(R.string.profile_section_account)
            val editProfile           = s(R.string.profile_edit_profile)
            val myDishLists            = s(R.string.my_dish_lists)
            val preferences           = s(R.string.profile_preferences)
            val privacy               = s(R.string.profile_privacy)
            val sectionAppSettings    = s(R.string.profile_section_app_settings)
            val notifications         = s(R.string.profile_notifications)
            val theme                 = s(R.string.profile_theme)
            val language              = s(R.string.profile_language)
            val sectionSupport        = s(R.string.profile_section_support)
            val helpCenter            = s(R.string.profile_help_center)
            val contactUs             = s(R.string.profile_contact_us)
            val about                 = s(R.string.about)
            val sectionAccountActions = s(R.string.profile_section_account_actions)
            val signOut               = s(R.string.profile_sign_out)
            val signOutTitle          = s(R.string.profile_sign_out_confirm_title)
            val signOutMessage        = s(R.string.profile_sign_out_confirm_message)
            val cancel                = s(R.string.cancel)
            val version               = s(R.string.profile_version)
        }
    }

    var showSignOutDialog by remember { mutableStateOf(false) }

    // Handle sign out
    fun handleSignOut() {
        googleSignInHelper.signOut {
            authViewModel.signOut()
            onNavigateToLogin()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = str.title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold) },
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Profile Section
            item {
                UserProfileCard(
                    user = user,
                    isLoggedIn = isLoggedIn,
                    language = currentLanguage,
                    localizationManager = localizationManager,
                    onSignInClick = onNavigateToLogin
                )
            }

            // Account Settings Section
            if (isLoggedIn) {
                item {
                    ProfileSection(
                        title = str.sectionAccount,
                        items = listOf(
                            ProfileItem(str.editProfile, Icons.Filled.Edit, onClick = onNavigateToEditProfile),
                            ProfileItem(str.myDishLists, Icons.Filled.Bookmarks, onClick = onNavigateToMyLists),
                        )
                    )
                }

                // App Settings Section
                item {
                    ProfileSection(
                        title = str.sectionAppSettings,
                        items = listOf(
                            ProfileItem(str.notifications, Icons.Filled.Notifications, onClick = {}),
                        )
                    )
                }

                // Support Section
                item {
                    ProfileSection(
                        title = str.sectionSupport,
                        items = listOf(
                            ProfileItem(str.helpCenter, Icons.AutoMirrored.Filled.Help, onClick = {}),
                            ProfileItem(str.contactUs, Icons.Filled.Email, onClick = {}),
                            ProfileItem(str.about, Icons.Filled.Info, onClick = {})
                        )
                    )
                }

                // Sign Out Section
                item {
                    ProfileSection(
                        title = str.sectionAccountActions,
                        items = listOf(
                            ProfileItem(
                                title = str.signOut,
                                icon = Icons.AutoMirrored.Filled.ExitToApp,
                                onClick = { showSignOutDialog = true },
                                textColor = MaterialTheme.colorScheme.error
                            )
                        )
                    )
                }
            }

            // Version Info
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = str.version,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Sign Out Confirmation Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(str.signOutTitle) },
            text = { Text(str.signOutMessage) },
            confirmButton = {
                TextButton(onClick = { showSignOutDialog = false; handleSignOut() }) {
                    Text(str.signOut, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text(str.cancel) }
            }
        )
    }
}

@Composable
private fun UserProfileCard(
    user: User?,
    isLoggedIn: Boolean,
    language: Language = Language.ENGLISH,
    localizationManager: LocalizationManager? = null,
    onSignInClick: () -> Unit
) {
    val str = remember(language) {
        fun s(id: Int) = localizationManager?.getString(id, language) ?: ""
        object {
            val notSignedIn  = s(R.string.profile_not_signed_in)
            val signInPrompt = s(R.string.profile_sign_in_prompt)
            val signIn       = s(R.string.profile_sign_in)
            val online       = s(R.string.profile_online)
        }
    }

    // Format ISO dateOfBirth → locale-aware human-readable string
    val formattedDob = remember(user?.dateOfBirth, language) {
        user?.dateOfBirth?.takeIf { it.isNotEmpty() }?.let { raw ->
            val locale = java.util.Locale.forLanguageTag(language.code)
                .let { if (it.language == "en") java.util.Locale.ENGLISH else it }
            val displayFmt = java.time.format.DateTimeFormatter.ofPattern(
                if (language == Language.ENGLISH) "MMMM dd, yyyy" else "dd MMMM, yyyy",
                locale
            )
            // Try full ISO datetime: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
            runCatching {
                java.time.LocalDate.parse(
                    raw.substringBefore('T').ifEmpty { raw },
                    java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
                ).format(displayFmt)
            }.getOrElse { raw }
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        if (isLoggedIn) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
                    .border(
                        2.dp,
                        if (isLoggedIn) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoggedIn && user?.avatar != null) {
                    AsyncImage(
                        model = user.avatar,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = if (isLoggedIn) Icons.Filled.Person else Icons.Filled.AccountCircle,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(40.dp),
                        tint = if (isLoggedIn) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoggedIn && user != null) {
                // User is signed in
                Text(
                    text = user.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                user.email.let { email ->
                    Text(
                        text = email,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Date of birth (if available), formatted in app language
                formattedDob?.let { dob ->
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Cake,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dob,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // User status indicator
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Green)
                    )
                    Text(
                        text = str.online,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else {
                // User is not signed in
                Text(
                    text = str.notSignedIn,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = str.signInPrompt,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Sign In Button
                OutlinedButton(
                    onClick = onSignInClick,
                    modifier = Modifier.padding(top = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(str.signIn)
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    items: List<ProfileItem>
) {
    Column {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    ProfileItemRow(
                        item = item,
                        showDivider = index < items.size - 1
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileItemRow(
    item: ProfileItem,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { item.onClick() }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.iconColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = item.title,
                fontSize = 16.sp,
                color = item.textColor ?: MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )
        }
    }
}

data class ProfileItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val textColor: androidx.compose.ui.graphics.Color? = null,
    val iconColor: androidx.compose.ui.graphics.Color? = null
)
