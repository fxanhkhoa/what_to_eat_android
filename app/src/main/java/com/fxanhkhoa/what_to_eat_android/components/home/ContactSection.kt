package com.fxanhkhoa.what_to_eat_android.components.home

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fxanhkhoa.what_to_eat_android.model.CreateContactDto
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider.createService
import com.fxanhkhoa.what_to_eat_android.services.ContactService
import com.fxanhkhoa.what_to_eat_android.ui.localization.Language
import com.fxanhkhoa.what_to_eat_android.ui.localization.LocalizationManager
import kotlinx.coroutines.launch
import com.fxanhkhoa.what_to_eat_android.R

@Composable
fun ContactSection(
    modifier: Modifier = Modifier,
    contactEmail: String = "fxanhkhoa@gmail.com",
    language: Language = Language.ENGLISH
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = localizationManager.getString(R.string.contact_us, language),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Name Field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(localizationManager.getString(R.string.name, language)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(localizationManager.getString(R.string.email, language)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Message Field
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text(localizationManager.getString(R.string.message, language)) },
            modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            maxLines = 5
        )

        // Send Button
        Button(
            onClick = {
                if (name.isBlank()) {
                    alertMessage = localizationManager.getString(R.string.name_required, language)
                    showAlert = true
                    isSuccess = false
                    return@Button
                }
                if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    alertMessage = localizationManager.getString(R.string.email_required, language)
                    showAlert = true
                    isSuccess = false
                    return@Button
                }
                if (message.isBlank()) {
                    alertMessage = localizationManager.getString(R.string.message_required, language)
                    showAlert = true
                    isSuccess = false
                    return@Button
                }
                isSending = true
                coroutineScope.launch {
                    try {
                        val contactService = createService<ContactService>()
                        contactService.create(
                            CreateContactDto(
                                email = email,
                                name = name,
                                message = message
                            )
                        )
                        isSuccess = true
                        alertMessage = localizationManager.getString(R.string.message_sent_success, language)
                        name = ""
                        email = ""
                        message = ""
                    } catch (e: Exception) {
                        isSuccess = false
                        alertMessage = e.localizedMessage ?: localizationManager.getString(R.string.message_send_failed, language)
                    } finally {
                        isSending = false
                        showAlert = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = !isSending,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(localizationManager.getString(R.string.send_message, language))
            }
        }

        // Contact Info
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            localizationManager.getString(R.string.or_contact_directly, language),
            style = MaterialTheme.typography.titleMedium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = localizationManager.getString(R.string.email, language),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(contactEmail, style = MaterialTheme.typography.bodyMedium)
        }
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = {
                Text(
                    if (isSuccess)
                        localizationManager.getString(R.string.success, language)
                    else
                        localizationManager.getString(R.string.error, language)
                )
            },
            text = {
                Text(alertMessage)
            },
            confirmButton = {
                Button(onClick = { showAlert = false }) {
                    Text(localizationManager.getString(R.string.ok, language))
                }
            }
        )
    }
}
