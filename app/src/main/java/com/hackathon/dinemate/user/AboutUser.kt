package com.hackathon.dinemate.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hackathon.dinemate.questionnaire.SelectedOptionsStore
import com.hackathon.dinemate.questionnaire.SelectedOptionsViewer
import com.hackathon.dinemate.ui.theme.Charcoal
import com.hackathon.dinemate.ui.theme.LightGrey
import com.hackathon.dinemate.ui.theme.White

@Composable
fun PreferencesSection(
    userViewModel: UserViewModel,
    navController: NavController
) {
    // State for managing edit mode
    var isEditing by remember { mutableStateOf(false) }
    val user by userViewModel.user.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = LightGrey), // set background color
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp) // remove shadow
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Pushes items to ends
                verticalAlignment = Alignment.CenterVertically // Aligns items vertically
            ) {
                Text(
                    text = "Dining Preferences",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = Charcoal
                )
                Row {
                    // Single, clear IconButton to enter edit mode
                    IconButton(
                        onClick = {
                            navController.navigate("questionnaire/${user?.userId}")
                        },
                        modifier = Modifier.padding(top = 4.dp) // Adjust padding slightly if needed
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Preferences", // For accessibility
//                            tint = LightGrey // Make icon visually distinct
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // Space below the header row

            PreferenceList(preferences = user!!.preferences!!)
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