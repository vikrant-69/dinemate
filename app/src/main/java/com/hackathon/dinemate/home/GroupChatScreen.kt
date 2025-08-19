package com.hackathon.dinemate.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hackathon.dinemate.config.AppConfig
import com.hackathon.dinemate.ui.theme.Black
import com.hackathon.dinemate.ui.theme.Charcoal
import com.hackathon.dinemate.ui.theme.LightGrey
import com.hackathon.dinemate.ui.theme.MediumGrey
import com.hackathon.dinemate.ui.theme.White


@OptIn(ExperimentalMaterial3Api::class)
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

    LaunchedEffect(groupId, userId, baseURL) {
        viewModel.initialize(userId, baseURL, groupId)
        viewModel.fetchMessages()
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(ui.messages.size) {
        if (ui.messages.isNotEmpty()) {
            listState.animateScrollToItem(ui.messages.size - 1)
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
                        Text(groupName, color = Charcoal, style = MaterialTheme.typography.titleMedium)
                        inviteCode?.takeIf { it.isNotBlank() }?.let {
                            Text("Invite: $it", color = MediumGrey, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                actions = {
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
                // Messages list
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(ui.messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            isMine = msg.user_id == userId
                        )
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
                        Icon(Icons.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
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
                        color = if (isMine) White else MediumGrey,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(2.dp))
                }
                Text(message.content, color = textColor)
                Spacer(Modifier.height(4.dp))
                Text(
                    message.created_at,
                    color = if (isMine) White.copy(alpha = 0.7f) else MediumGrey,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
