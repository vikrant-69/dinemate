package com.hackathon.dinemate.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hackathon.dinemate.ProfileTab
import com.hackathon.dinemate.config.AppConfig
import com.hackathon.dinemate.ui.theme.Black
import com.hackathon.dinemate.ui.theme.Charcoal
import com.hackathon.dinemate.ui.theme.DarkGrey
import com.hackathon.dinemate.ui.theme.LightGrey
import com.hackathon.dinemate.ui.theme.MediumGrey
import com.hackathon.dinemate.ui.theme.White
import com.hackathon.dinemate.user.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: String,
    baseURL: String = AppConfig.BASE_URL,
    viewModel: HomeViewModel = viewModel(),
    userViewModel: UserViewModel
) {
    val ui by viewModel.uiState.collectAsState()

    LaunchedEffect(userId, baseURL) {
        viewModel.initialize(userId, baseURL)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DineMate", color = Charcoal) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightGrey)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = LightGrey) {
                NavigationBarItem(
                    selected = ui.currentTab == BottomTab.Home,
                    onClick = { viewModel.setTab(BottomTab.Home) },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = ui.currentTab == BottomTab.Groups,
                    onClick = { viewModel.setTab(BottomTab.Groups) },
                    icon = { Icon(Icons.Filled.Group, contentDescription = "Groups") },
                    label = { Text("Groups") }
                )
                NavigationBarItem(
                    selected = ui.currentTab == BottomTab.Profile,
                    onClick = { viewModel.setTab(BottomTab.Profile) },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        when (ui.currentTab) {
            BottomTab.Home -> HomeTab(ui, viewModel, padding)
            BottomTab.Groups -> GroupsTab(ui, viewModel, padding)
            BottomTab.Profile -> ProfileTab(userViewModel, padding) // placeholder
        }
    }
}

@Composable
private fun HomeTab(ui: HomeUiState, viewModel: HomeViewModel, padding: PaddingValues) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        Text("Create Group", style = MaterialTheme.typography.titleMedium, color = Charcoal)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = ui.createGroupName,
            onValueChange = viewModel::onCreateGroupNameChanged,
            label = { Text("Group name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = ui.createGroupDescription,
            onValueChange = viewModel::onCreateGroupDescChanged,
            label = { Text("Description (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = viewModel::createGroup,
            enabled = !ui.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = White)
        ) {
            Text("Create Group")
        }

        ui.inviteCodeCreated?.let {
            Spacer(Modifier.height(8.dp))
            AssistChip(
                onClick = { /* copy to clipboard if you want */ },
                label = { Text("Invite code: $it") }
            )
        }

        Spacer(Modifier.height(24.dp))

        Text("Join Group", style = MaterialTheme.typography.titleMedium, color = Charcoal)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = ui.inviteCode,
            onValueChange = viewModel::onInviteCodeChanged,
            label = { Text("Invite code") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = viewModel::joinGroup,
            enabled = !ui.isLoading && ui.inviteCode.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = DarkGrey, contentColor = White)
        ) {
            Text("Join")
        }

        Spacer(Modifier.height(16.dp))

        ui.info?.let {
            InfoBanner(it)
        }
        ui.error?.let {
            Spacer(Modifier.height(8.dp))
            ErrorBanner(it)
        }
    }
}

@Composable
private fun GroupsTab(ui: HomeUiState, viewModel: HomeViewModel, padding: PaddingValues) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        // Groups list
        Text(
            text = "Your Groups",
            style = MaterialTheme.typography.titleMedium,
            color = Charcoal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        if (ui.isFetchingGroups && ui.groups.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (ui.groups.isEmpty()) {
            Text(
                text = "No groups yet. Create or join a group from Home.",
                color = MediumGrey,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(ui.groups, key = { it.id }) { g ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { viewModel.selectGroup(g.id) },
                        colors = CardDefaults.elevatedCardColors(containerColor = LightGrey)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(g.name, fontWeight = FontWeight.SemiBold, color = Charcoal)
                            if (!g.description.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(g.description, color = Black)
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = MediumGrey
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "${g.message_count ?: 0} messages",
                                    color = MediumGrey,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Chat panel for selected group
        ui.openGroupId?.let { gid ->
            Divider()
            Column(Modifier.padding(16.dp)) {
                Text("Chat: $gid", color = Charcoal, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))

                if (ui.isFetchingMessages && ui.messages.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Loading messages...", color = MediumGrey)
                    }
                } else if (ui.messages.isEmpty()) {
                    Text("No messages yet.", color = MediumGrey)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 320.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(ui.messages, key = { it.id }) { msg ->
                            MessageItem(msg)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = ui.messageInput,
                        onValueChange = viewModel::onMessageInputChanged,
                        label = { Text("Message") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = viewModel::sendMessage,
                        enabled = !ui.isSendingMessage && ui.messageInput.isNotBlank(),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Charcoal, contentColor = White)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }

        ui.error?.let {
            Spacer(Modifier.height(8.dp))
            ErrorBanner(it)
        }
    }
}

//@Composable
//private fun ProfileTab(padding: PaddingValues) {
//    Box(
//        Modifier
//            .fillMaxSize()
//            .padding(padding),
//        contentAlignment = Alignment.Center
//    ) {
//        Text("Profile coming soon", color = MediumGrey)
//    }
//}

@Composable
private fun MessageItem(msg: Message) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = LightGrey)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = msg.user_name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Charcoal
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = msg.content,
                style = MaterialTheme.typography.bodyMedium,
                color = Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = msg.created_at,
                style = MaterialTheme.typography.labelSmall,
                color = MediumGrey
            )
        }
    }
}

@Composable
private fun InfoBanner(text: String) {
    Surface(color = LightGrey, tonalElevation = 1.dp) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            color = Charcoal,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ErrorBanner(text: String) {
    Surface(color = MaterialTheme.colorScheme.errorContainer) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
