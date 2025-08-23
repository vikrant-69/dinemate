package com.hackathon.dinemate.home

import android.util.Log
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hackathon.dinemate.config.AppConfig
import com.hackathon.dinemate.restaurant.LocationAwareSearchTab
import com.hackathon.dinemate.restaurant.SearchTab
import com.hackathon.dinemate.ui.theme.Black
import com.hackathon.dinemate.ui.theme.Charcoal
import com.hackathon.dinemate.ui.theme.LightGrey
import com.hackathon.dinemate.ui.theme.MediumGrey
import com.hackathon.dinemate.ui.theme.White
import com.hackathon.dinemate.user.ProfileTab
import com.hackathon.dinemate.user.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: String,
    baseURL: String = AppConfig.BASE_URL,
    onNavigateToChat: (GroupSummary) -> Unit = {},
    viewModel: HomeViewModel = viewModel(),
    userViewModel: UserViewModel
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
                    onClick = { selectedTab = "home" },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == "search",
                    onClick = { selectedTab = "search" },
                    icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    label = { Text("Search") }
                )
                NavigationBarItem(
                    selected = selectedTab == "profile",
                    onClick = { selectedTab = "profile" },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        },
        floatingActionButton = {
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
                    // Handle restaurant click
                    Log.d("HomeScreen", "Restaurant clicked: ${restaurant.name}")
                }
            )
            "profile" -> ProfileTab(userViewModel)
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

    // Optional inline error/info banners as dialogs (if you want unobtrusive UX, you can remove this):
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
private fun HomeGroupsList(
    ui: HomeUiState,
    viewModel: HomeViewModel,
    padding: PaddingValues,
    onNavigateToChat: (GroupSummary) -> Unit // Add this parameter
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        if (ui.isFetchingGroups && ui.groups.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LinearProgressIndicator()
            }
        } else {
            if (ui.groups.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(ui.groups, key = { it.id }) { g ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onNavigateToChat(g) // Navigate to chat instead of selectGroup
                                },
                            colors = CardDefaults.elevatedCardColors(containerColor = LightGrey)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(g.name, fontWeight = FontWeight.SemiBold, color = Charcoal)
                                val desc = g.description ?: ""
                                if (desc.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(desc, color = Black)
                                }
                            }
                        }
                    }
                }
            }
        }
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
