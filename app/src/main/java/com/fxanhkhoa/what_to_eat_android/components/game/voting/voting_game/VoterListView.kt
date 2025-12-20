package com.fxanhkhoa.what_to_eat_android.components.game.voting.voting_game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.fxanhkhoa.what_to_eat_android.R
import com.fxanhkhoa.what_to_eat_android.model.UserModel
import com.fxanhkhoa.what_to_eat_android.services.UserService
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Voter list view component
 * Displays list of users who voted with their avatars and names
 * Fetches user data from UserService and shows anonymous vote count
 */
@Composable
fun VoterListView(
    userIds: List<String>,
    anonymousCount: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    val userService = remember { UserService.getInstance(context) }

    var language by remember { mutableStateOf(Language.ENGLISH) }
    var users by remember { mutableStateOf<List<UserModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Observe language changes
    LaunchedEffect(Unit) {
        language = localizationManager.currentLanguage.first()
    }

    // Load users when userIds change
    LaunchedEffect(userIds) {
        loadUsers(
            userIds = userIds,
            userService = userService,
            onLoading = { isLoading = it },
            onUsersLoaded = { users = it }
        )
    }

    if (users.isNotEmpty() || anonymousCount > 0) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Voters count header
            Text(
                text = "${users.size + anonymousCount} voters", // TODO: Add localization
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.heightIn(max = 200.dp) // Limit height for scrolling
            ) {
                // Show registered users
                items(users) { user ->
                    VoterRowView(user = user)
                }

                // Show anonymous voters count
                if (anonymousCount > 0) {
                    item {
                        AnonymousVoterRow(
                            anonymousCount = anonymousCount,
                            localizationManager = localizationManager,
                            language = language
                        )
                    }
                }
            }

            // Loading indicator
            if (isLoading && users.isEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Loading voters...", // TODO: Add localization
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Voter row view component
 * Displays a single voter with avatar and name
 */
@Composable
fun VoterRowView(
    user: UserModel,
    modifier: Modifier = Modifier
) {
    val displayName = if (!user.name.isNullOrEmpty()) {
        user.name
    } else {
        user.email
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User avatar
        SubcomposeAsyncImage(
            model = user.avatar,
            contentDescription = displayName,
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            error = { _ ->
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            },
            loading = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        )

        Text(
            text = displayName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

/**
 * Anonymous voter row component
 * Displays anonymous vote count with icon
 */
@Composable
private fun AnonymousVoterRow(
    anonymousCount: Int,
    localizationManager: LocalizationManager,
    language: Language,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Anonymous voters",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = if (anonymousCount == 1) {
                "$anonymousCount anonymous voter" // TODO: Add localization
            } else {
                "$anonymousCount anonymous voters"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}

// MARK: - Helper Functions

/**
 * Load users from UserService
 * Fetches user data for all user IDs in parallel
 */
@OptIn(DelicateCoroutinesApi::class)
private fun loadUsers(
    userIds: List<String>,
    userService: UserService,
    onLoading: (Boolean) -> Unit,
    onUsersLoaded: (List<UserModel>) -> Unit
) {
    if (userIds.isEmpty()) {
        onUsersLoaded(emptyList())
        return
    }

    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
        onLoading(true)

        try {
            // Fetch all users in parallel
            val fetchedUsers: List<UserModel> = userIds.map { userId ->
                async(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        userService.findOne(userId).firstOrNull()?.getOrNull()
                    } catch (_: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull()

            // Sort users by name or email
            val sortedUsers = fetchedUsers.sortedBy { user: UserModel ->
                user.name?.takeIf { it.isNotEmpty() } ?: user.email
            }

            onUsersLoaded(sortedUsers)
        } catch (_: Exception) {
            onUsersLoaded(emptyList())
        } finally {
            onLoading(false)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VoterListViewPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // With users and anonymous votes
            VoterListView(
                userIds = listOf("user1", "user2"),
                anonymousCount = 3
            )

            Divider()

            // Only anonymous votes
            VoterListView(
                userIds = emptyList(),
                anonymousCount = 2
            )

            Divider()

            // Only users
            VoterListView(
                userIds = listOf("user1"),
                anonymousCount = 0
            )
        }
    }
}

@Preview(showBackground = true, name = "Voter Row")
@Composable
private fun VoterRowViewPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VoterRowView(
                user = UserModel(
                    id = "1",
                    email = "john@example.com",
                    name = "John Doe",
                    avatar = null
                )
            )

            VoterRowView(
                user = UserModel(
                    id = "2",
                    email = "jane@example.com",
                    name = null,
                    avatar = null
                )
            )
        }
    }
}
