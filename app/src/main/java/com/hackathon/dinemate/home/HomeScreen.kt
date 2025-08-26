package com.hackathon.dinemate.home

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hackathon.dinemate.config.AppConfig
import com.hackathon.dinemate.restaurant.LocationAwareSearchTab
import com.hackathon.dinemate.ui.theme.Black
import com.hackathon.dinemate.ui.theme.Charcoal
import com.hackathon.dinemate.ui.theme.LightGrey
import com.hackathon.dinemate.ui.theme.MediumGrey
import com.hackathon.dinemate.ui.theme.White
import com.hackathon.dinemate.user.ProfileTab
import com.hackathon.dinemate.user.UserViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: String,
    baseURL: String = AppConfig.BASE_URL,
    onNavigateToChat: (GroupSummary) -> Unit = {},
    viewModel: HomeViewModel = viewModel(),
    userViewModel: UserViewModel,
    navController: NavController,
    context: Context
) {
    val ui by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf("home") }
    var fabExpanded by remember { mutableStateOf(false) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var showJoinSheet by remember { mutableStateOf(false) }
    val createSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val joinSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    selected = selectedTab == "home",
                    onClick = {
                        selectedTab = "home"
                        fabExpanded = false
                    },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == "search",
                    onClick = {
                        selectedTab = "search"
                        fabExpanded = false
                    },
                    icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    label = { Text("Search") }
                )
                NavigationBarItem(
                    selected = selectedTab == "profile",
                    onClick = {
                        selectedTab = "profile"
                        fabExpanded = false
                    },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == "home") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 16.dp, bottom = 16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (fabExpanded) {
                            SmallActionFab(
                                label = "Create",
                                onClick = {
                                    fabExpanded = false
                                    showCreateSheet = true
                                },
                                icon = {
                                    Icon(Icons.Filled.Add, contentDescription = "Add Item")
                                }
                            )
                            SmallActionFab(
                                label = "Join",
                                onClick = {
                                    fabExpanded = false
                                    showJoinSheet = true
                                },
                                icon = {
                                    Icon(Icons.Default.GroupAdd, contentDescription = "Add Item")
                                }
                            )
                        }
                        FloatingActionButton(
                            onClick = { fabExpanded = !fabExpanded },
                            containerColor = Charcoal,
                            contentColor = White
                        ) {
                            Text(
                                if (fabExpanded) "×" else "+",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            "home" -> HomeGroupsList(
                ui = ui,
                viewModel = viewModel,
                padding = padding,
                onNavigateToChat = onNavigateToChat
            )

            "search" -> LocationAwareSearchTab(
                padding = padding,
                baseURL = baseURL,
                onRestaurantClick = { restaurant ->
                    Log.d("HomeScreen", "Restaurant clicked: ${restaurant.name}")
                }
            )

            "profile" -> ProfileTab(
                userViewModel,
                padding,
                navController,
                context
            )
        }
    }

    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { if (!ui.isLoading) showCreateSheet = false },
            sheetState = createSheetState,
            containerColor = LightGrey,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            CreateGroupSheetContent(
                name = ui.createGroupName,
                description = ui.createGroupDescription,
                isLoading = ui.isLoading,
                onNameChange = viewModel::onCreateGroupNameChanged,
                onDescriptionChange = viewModel::onCreateGroupDescChanged,
                onCancel = { if (!ui.isLoading) showCreateSheet = false },
                onCreate = {
                    viewModel.createGroup()
                    showCreateSheet = false
                }
            )
        }
    }

    if (showJoinSheet) {
        ModalBottomSheet(
            onDismissRequest = { if (!ui.isLoading) showJoinSheet = false },
            sheetState = joinSheetState,
            containerColor = LightGrey,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            JoinGroupSheetContent(
                inviteCode = ui.inviteCode,
                isLoading = ui.isLoading,
                onInviteCodeChange = viewModel::onInviteCodeChanged,
                onCancel = { if (!ui.isLoading) showJoinSheet = false },
                onJoin = {
                    viewModel.joinGroup()
                    showJoinSheet = false
                }
            )
        }
    }

    ui.error?.let { err ->
        AlertDialog(
            onDismissRequest = { /* consider clearing error in VM if desired */ },
            confirmButton = {
                TextButton(onClick = { /* add a clearError() in VM if you want */ }) { Text("OK") }
            },
            title = { Text("Error") },
            text = { Text(err) }
        )
    }
}

@Composable
private fun CreateGroupSheetContent(
    name: String,
    description: String,
    isLoading: Boolean,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCancel: () -> Unit,
    onCreate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Create Group", style = MaterialTheme.typography.titleMedium, color = Charcoal)
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Group name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel, enabled = !isLoading) {
                Text("Cancel", color = MediumGrey)
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onCreate,
                enabled = !isLoading && name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Charcoal,
                    contentColor = White
                )
            ) {
                Text("Create")
            }
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun JoinGroupSheetContent(
    inviteCode: String,
    isLoading: Boolean,
    onInviteCodeChange: (String) -> Unit,
    onCancel: () -> Unit,
    onJoin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Join Group", style = MaterialTheme.typography.titleMedium, color = Charcoal)
        OutlinedTextField(
            value = inviteCode,
            onValueChange = onInviteCodeChange,
            label = { Text("Invite code") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel, enabled = !isLoading) {
                Text("Cancel", color = MediumGrey)
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onJoin,
                enabled = !isLoading && inviteCode.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Charcoal,
                    contentColor = White
                )
            ) {
                Text("Join")
            }
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SmallActionFab(
    label: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = Charcoal,
        contentColor = White,
        icon = icon,
        text = { Text(label) }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HomeGroupsList(
    ui: HomeUiState,
    viewModel: HomeViewModel,
    padding: PaddingValues,
    onNavigateToChat: (GroupSummary) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        if (ui.isFetchingGroups && ui.groups.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Charcoal)
                    Spacer(Modifier.height(8.dp))
                    Text("Loading your groups...", color = MediumGrey)
                }
            }
        } else if (ui.groups.isEmpty()) {
            EmptyGroupsState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Your Groups",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(ui.groups, key = { it.id }) { group ->
                    EnhancedGroupCard(
                        group = group,
                        onClick = { onNavigateToChat(group) }
                    )
                }
            }
        }
    }
}

@Composable
fun CircularProgressIndicator(color: Color) {
    androidx.compose.material3.CircularProgressIndicator(
        modifier = Modifier.size(24.dp),
        color = color,
        strokeWidth = 4.dp
    )
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun EnhancedGroupCard(
    group: GroupSummary,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.elevatedCardColors(containerColor = White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = if (group.status == "active") Color(0xFF4CAF50) else Color(0xFFFF9800),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = group.status?.capitalize() ?: "Unknown",
                        color = White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (!group.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = group.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GroupStat(
                    icon = "👥",
                    label = "Members",
                    value = "${group.member_count}/${group.max_members}",
                    progress = (group.member_count?.toFloat() ?: 0f) / (group.max_members?.toFloat()
                        ?: 1f)
                )

                GroupStat(
                    icon = "💬",
                    label = "Messages",
                    value = "${group.message_count ?: 0}"
                )

                GroupStat(
                    icon = "📅",
                    label = "Created",
                    value = formatCreatedDate(group.created_at)
                )
            }

            Spacer(Modifier.height(16.dp))

            Surface(
                color = LightGrey,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Invite Code",
                            style = MaterialTheme.typography.labelSmall,
                            color = MediumGrey
                        )
                        Text(
                            text = group.invite_code ?: "N/A",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    }

                    Surface(
                        color = Color(0xFF1976D2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable {
                            group.invite_code?.let { inviteCode ->
                                clipboardManager.setText(AnnotatedString(inviteCode))
                                Toast.makeText(
                                    context,
                                    "Invite code copied to clipboard",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {
                        Text(
                            text = "Copy",
                            color = White,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (!group.last_message_at.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Last activity: ${formatLastActivity(group.last_message_at)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MediumGrey
                )
            }
        }
    }
}

@Composable
private fun GroupStat(
    icon: String,
    label: String,
    value: String,
    progress: Float? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Charcoal
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MediumGrey
        )

        progress?.let {
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = it,
                modifier = Modifier
                    .width(60.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE0E0E0)
            )
        }
    }
}

@Composable
private fun EmptyGroupsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "🍽️",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No Groups Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Charcoal
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Create your first dining group or join one using an invite code",
                style = MaterialTheme.typography.bodyMedium,
                color = MediumGrey,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Tap the + button to get started",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatCreatedDate(dateTime: String?): String {
    if (dateTime.isNullOrBlank()) return "N/A"
    return try {
        val dt = LocalDateTime.parse(dateTime)
        dt.format(DateTimeFormatter.ofPattern("MMM dd"))
    } catch (e: Exception) {
        "N/A"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatLastActivity(dateTime: String?): String {
    if (dateTime.isNullOrBlank()) return "Never"
    return try {
        val dt = LocalDateTime.parse(dateTime)
        val now = LocalDateTime.now()
        val days = java.time.Duration.between(dt, now).toDays()

        when {
            days == 0L -> "Today"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            else -> dt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        }
    } catch (e: Exception) {
        "Unknown"
    }
}