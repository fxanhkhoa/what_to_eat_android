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
import kotlinx.coroutines.launch

@Composable
fun ContactSection(
    modifier: Modifier = Modifier,
    contactEmail: String = "fxanhkhoa@gmail.com"
) {
    val context = LocalContext.current
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
            text = "Contact Us", // Replace with localization if available
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Name Field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Message Field
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            maxLines = 5
        )

        // Send Button
        Button(
            onClick = {
                if (name.isBlank()) {
                    alertMessage = "Name is required"
                    showAlert = true
                    isSuccess = false
                    return@Button
                }
                if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    alertMessage = "Valid email is required"
                    showAlert = true
                    isSuccess = false
                    return@Button
                }
                if (message.isBlank()) {
                    alertMessage = "Message is required"
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
                        alertMessage = "Message sent successfully"
                        name = ""
                        email = ""
                        message = ""
                    } catch (e: Exception) {
                        isSuccess = false
                        alertMessage = e.localizedMessage ?: "Failed to send message"
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
                Text("Send Message")
            }
        }

        // Contact Info
        Spacer(modifier = Modifier.height(24.dp))
        Text("Or contact directly:", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
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
                Text(if (isSuccess) "Success" else "Error")
            },
            text = {
                Text(alertMessage)
            },
            confirmButton = {
                Button(onClick = { showAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
}
