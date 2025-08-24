package com.hackathon.dinemate.home

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.hackathon.dinemate.config.AppConfig
import com.hackathon.dinemate.restaurant.Restaurant
import com.hackathon.dinemate.ui.theme.Black
import com.hackathon.dinemate.ui.theme.Charcoal
import com.hackathon.dinemate.ui.theme.LightGrey
import com.hackathon.dinemate.ui.theme.MediumGrey
import com.hackathon.dinemate.ui.theme.White
import com.hackathon.dinemate.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun GroupChatScreen(
    userId: String,
    groupId: String,
    groupName: String,
    inviteCode: String?,
    onBack: () -> Unit,
    baseURL: String = AppConfig.BASE_URL,
    viewModel: GroupChatViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Location permission state
    val locationPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    LaunchedEffect(groupId, userId, baseURL) {
        viewModel.initialize(userId, baseURL, groupId)
        viewModel.fetchMessages()
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(ui.messages.size, ui.restaurantPreferences.size) {
        if (ui.messages.isNotEmpty() || ui.restaurantPreferences.isNotEmpty()) {
            listState.animateScrollToItem(
                maxOf(0, ui.messages.size + if (ui.restaurantPreferences.isNotEmpty()) 1 else 0 - 1)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Charcoal
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            groupName,
                            color = Charcoal,
                            style = MaterialTheme.typography.titleMedium
                        )
                        inviteCode?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                "Invite: $it",
                                color = MediumGrey,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (locationPermissionState.allPermissionsGranted) {
                                viewModel.showPreferenceDialog()
                            } else {
                                locationPermissionState.launchMultiplePermissionRequest()
                            }
                        },
                        enabled = !ui.isLoadingPreferences
                    ) {
                        if (ui.isLoadingPreferences) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Charcoal
                            )
                        } else {
                            Icon(
                                Icons.Filled.Restaurant,
                                contentDescription = "Get Restaurant Preferences",
                                tint = Charcoal
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.fetchMessages() },
                        enabled = !ui.isLoading
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Charcoal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightGrey)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (ui.isLoading && ui.messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Messages and restaurants list
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    if (ui.restaurantPreferences.isNotEmpty()) {
                        item {
                            RestaurantRecommendations(
                                restaurants = ui.restaurantPreferences,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }

                    // Regular messages
                    val messagesWithDateSeparators = insertDateSeparators(ui.messages)
                    items(messagesWithDateSeparators, key = { it.id }) { msg ->
                        when (msg.message_type) {
                            "system" -> {
                                SystemMessage(content = msg.content)
                            }

                            "date_separator" -> {
                                DateSeparator(date = msg.content)
                            }

                            else -> {
                                MessageBubble(
                                    message = msg,
                                    isMine = msg.user_id == userId
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // Input row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = ui.input,
                        onValueChange = viewModel::onInputChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (ui.input.isNotBlank() && !ui.isSending) viewModel.sendMessage()
                            }
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = { viewModel.sendMessage() },
                        enabled = ui.input.isNotBlank() && !ui.isSending,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Charcoal,
                            contentColor = White
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }

    // Restaurant Preference Dialog
    if (ui.showPreferenceDialog) {
        RestaurantPreferenceDialog(
            location = ui.preferenceLocation,
            onLocationChange = viewModel::onPreferenceLocationChange,
            onDismiss = { viewModel.hidePreferenceDialog() },
            onSubmit = { viewModel.fetchRestaurantPreferences(context) },
            isLoading = ui.isLoadingPreferences
        )
    }

    // Location Permission Dialog
    if (!locationPermissionState.allPermissionsGranted &&
        locationPermissionState.shouldShowRationale
    ) {
        LocationPermissionDialog(
            onGrantPermission = { locationPermissionState.launchMultiplePermissionRequest() },
            onDismiss = { /* Handle dismiss */ }
        )
    }

    // Error dialog
    ui.error?.let {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
            },
            title = { Text("Error") },
            text = { Text(it) }
        )
    }
}

@Composable
private fun LocationPermissionDialog(
    onGrantPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Charcoal)
                Spacer(Modifier.width(8.dp))
                Text("Location Permission Required")
            }
        },
        text = {
            Text(
                "We need location access to find restaurants near you. This helps us provide personalized recommendations based on your chat preferences.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onGrantPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Charcoal,
                    contentColor = White
                )
            ) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        }
    )
}

@Composable
private fun RestaurantPreferenceDialog(
    location: String,
    onLocationChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = if (!isLoading) onDismiss else {
            {}
        },
        title = {
            Text(
                "🍽️ Get Restaurant Recommendations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                Text(
                    "We'll analyze your chat to find restaurants that match your preferences!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediumGrey
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Leave location blank to use your current GPS location, or enter a specific area.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MediumGrey.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = onLocationChange,
                    label = { Text("Location (optional)") },
                    placeholder = { Text("Enter city or leave blank for current location") },
                    leadingIcon = {
                        Icon(Icons.Filled.LocationOn, contentDescription = "Location")
                    },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Charcoal,
                    contentColor = White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyzing...")
                } else {
                    Text("Get Recommendations")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel", color = MediumGrey)
            }
        }
    )
}

@Composable
private fun RestaurantRecommendations(
    restaurants: List<Restaurant>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = LightGrey
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Recommended for Your Group",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
            }

            restaurants.forEach { restaurant ->
                RestaurantRecommendationItem(restaurant = restaurant)
                if (restaurant != restaurants.last()) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MediumGrey.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun RestaurantRecommendationItem(restaurant: Restaurant) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Show in webview or open maps if needed */ }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = restaurant.image),
            contentDescription = "Rating star",
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Charcoal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            restaurant.description.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_star),
                    contentDescription = "Rating star",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${restaurant.rating}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Text(
                    text = " • ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = restaurant.distance,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isMine: Boolean
) {
    val bubbleColor = if (isMine) Charcoal else LightGrey
    val textColor = if (isMine) White else Black
    val align = if (isMine) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = align
    ) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(containerColor = bubbleColor),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                if (!isMine) {
                    Text(
                        message.user_name,
                        color = MediumGrey,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(2.dp))
                }
                Text(message.content, color = textColor)
                Spacer(Modifier.height(4.dp))
                Text(
                    formatTime(message.created_at),
                    color = if (isMine) White.copy(alpha = 0.7f) else MediumGrey,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun SystemMessage(content: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MediumGrey.copy(alpha = 0.2f),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = content,
                color = MediumGrey,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun DateSeparator(date: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = Charcoal.copy(alpha = 0.1f),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = formatDate(date),
                color = Charcoal,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

// Helper function to format timestamp to HH:mm
private fun formatTime(timestamp: String): String {
    return try {
        val dateTime = LocalDateTime.parse(timestamp)
        dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: DateTimeParseException) {
        timestamp
    }
}

// Helper function to format date
private fun formatDate(dateString: String): String {
    return try {
        val date = LocalDateTime.parse(dateString + "T00:00:00")
        date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
    } catch (e: Exception) {
        dateString
    }
}

// Helper function to insert date separators between messages
private fun insertDateSeparators(messages: List<Message>): List<Message> {
    val result = mutableListOf<Message>()
    var lastDate: String? = null

    messages.forEach { message ->
        val messageDate = extractDate(message.created_at)

        if (messageDate != lastDate) {
            // Insert date separator
            result.add(
                Message(
                    id = "date_separator_$messageDate",
                    group_id = message.group_id,
                    user_id = "",
                    user_name = "",
                    message_type = "date_separator",
                    content = messageDate,
                    restaurant_data = null,
                    created_at = messageDate
                )
            )
            lastDate = messageDate
        }

        result.add(message)
    }

    return result
}

// Helper function to extract date from timestamp
private fun extractDate(timestamp: String): String {
    return try {
        val dateTime = LocalDateTime.parse(timestamp)
        dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    } catch (e: DateTimeParseException) {
        timestamp.substring(0, 10) // fallback to first 10 characters
    }
}
