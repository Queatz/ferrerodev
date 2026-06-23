package dev.ferrero

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Moves focus to the next (or previous, when Shift is held) focusable element when the Tab key
 * is pressed, instead of inserting a tab character into the text field.
 */
private fun Modifier.tabFocusTraversal(focusManager: FocusManager): Modifier = onPreviewKeyEvent { event ->
    if (event.type == KeyEventType.KeyDown && event.key == Key.Tab) {
        focusManager.moveFocus(if (event.isShiftPressed) FocusDirection.Previous else FocusDirection.Next)
        true
    } else {
        false
    }
}

@Composable
internal fun ConnectForm() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var service by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var isSent by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var serviceError by remember { mutableStateOf<String?>(null) }
    var messageError by remember { mutableStateOf<String?>(null) }
    var sendError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val services = listOf(
        "New Product Conception",
        "Existing Product Reconception",
        "Market Research & Positioning",
        "Tech Team Augmentation",
        "Complete Tech Solutions",
        "Something else",
    )

    Column(Modifier.fillMaxWidth()) {
        BrandField(
            name,
            {
                name = it
                nameError = null
                isSent = false
                sendError = null
            },
            "Name",
            enabled = !isSending,
            errorText = nameError,
            modifier = Modifier.tabFocusTraversal(focusManager),
        )
        Spacer(Modifier.height(16.dp))
        BrandField(
            email,
            {
                email = it
                emailError = null
                isSent = false
                sendError = null
            },
            "Email",
            enabled = !isSending,
            errorText = emailError,
            modifier = Modifier.tabFocusTraversal(focusManager),
        )
        Spacer(Modifier.height(16.dp))

        Column(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = service,
                    onValueChange = {},
                    readOnly = true,
                    enabled = !isSending,
                    isError = serviceError != null,
                    label = { Text("Service Needed") },
                    modifier = Modifier.fillMaxWidth().tabFocusTraversal(focusManager),
                    shape = RoundedCornerShape(BRAND_CORNER_RADIUS),
                    colors = brandFieldColors(),
                )
                // Transparent overlay to capture clicks for the dropdown.
                if (!isSending) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .clickable { expanded = true }
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    services.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                service = option
                                serviceError = null
                                isSent = false
                                sendError = null
                                expanded = false
                            },
                        )
                    }
                }
            }
            if (serviceError != null) {
                Text(
                    text = serviceError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        BrandField(
            value = message,
            onValueChange = {
                message = it
                messageError = null
                isSent = false
                sendError = null
            },
            label = "Message",
            minHeight = 140.dp,
            enabled = !isSending,
            errorText = messageError,
            modifier = Modifier.tabFocusTraversal(focusManager),
        )
        Spacer(Modifier.height(24.dp))
        if (isSent) {
            Text(
                text = "Thank you! Your message has been sent. We'll be in touch soon.",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif,
            )
        } else {
            Button(
                onClick = {
                    if (!isSending) {
                        nameError = if (name.isBlank()) "Please enter your name." else null
                        emailError = when {
                            email.isBlank() -> "Please enter your email."
                            !isValidEmail(email) -> "Please enter a valid email address."
                            else -> null
                        }
                        serviceError = if (service.isBlank()) "Please select a service." else null
                        messageError = if (message.isBlank()) "Please enter a message." else null
                        sendError = null

                        val isValid = nameError == null && emailError == null &&
                            serviceError == null && messageError == null
                        if (isValid) {
                            isSending = true
                            scope.launch {
                                try {
                                    sendContactMessage(name, email, service, message)
                                    name = ""
                                    email = ""
                                    service = ""
                                    message = ""
                                    isSent = true
                                } catch (e: Throwable) {
                                    sendError = "Something went wrong sending your message. Please try again."
                                } finally {
                                    isSending = false
                                }
                            }
                        }
                    }
                },
                enabled = !isSending,
                shape = RoundedCornerShape(BRAND_CORNER_RADIUS),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                contentPadding = PaddingValues(horizontal = 36.dp, vertical = 18.dp),
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "Send message",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.SansSerif,
                    )
                }
            }
            if (sendError != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = sendError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                )
            }
        }
    }
}

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

private fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

@Composable
private fun BrandField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minHeight: androidx.compose.ui.unit.Dp = 56.dp,
    enabled: Boolean = true,
    errorText: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            enabled = enabled,
            isError = errorText != null,
            modifier = modifier.fillMaxWidth().heightIn(min = minHeight),
            shape = RoundedCornerShape(BRAND_CORNER_RADIUS),
            colors = brandFieldColors(),
        )
        if (errorText != null) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }
    }
}

@Composable
private fun brandFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    cursorColor = MaterialTheme.colorScheme.primary,
)
