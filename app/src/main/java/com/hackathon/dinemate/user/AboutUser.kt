package com.hackathon.dinemate.user

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hackathon.dinemate.ui.theme.Charcoal
import com.hackathon.dinemate.ui.theme.LightGrey
import com.hackathon.dinemate.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PreferencesScreen(
    userViewModel: UserViewModel
) {
    // Observe the user state from the UserViewModel
    val user by userViewModel.user.collectAsState()
    Log.d("USER_INFO2", user?.preferences.toString())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Preferences") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightGrey)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            // Show a loading indicator while the user object is being loaded
            if (user == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                // Once the user is loaded, display their info and preferences
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Hi, ${user?.name ?: "User"}!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Here are your saved dining preferences:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Check if the preferences list is empty or null
                    if (user?.preferences.isNullOrEmpty()) {
                        Text("You haven't set any preferences yet.")
                    } else {
                        // Display the list of preferences
                        PreferenceList(preferences = user!!.preferences!!)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreferenceList(preferences: List<String>) {
    // Use FlowRow to display the preference chips in a wrap-around layout
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        preferences.forEach { preference ->
            SuggestionChip(
                onClick = { /* Chips are for display only */ },
                label = { Text(preference) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = Charcoal,
                    labelColor = White
                )
            )
        }
    }
}
