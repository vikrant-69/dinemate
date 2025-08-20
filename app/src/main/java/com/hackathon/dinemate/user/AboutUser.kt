package com.hackathon.dinemate.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hackathon.dinemate.ui.theme.Charcoal
import com.hackathon.dinemate.ui.theme.LightGrey

@Composable
fun AboutSection(
    aboutText: String, // Comes from your ViewModel state
) {
    // State for managing edit mode
    var isEditing by remember { mutableStateOf(false) }
    // State to hold the text *during* editing, initialized with the current aboutText
    // It's reset every time isEditing becomes true
    var editedAboutText by remember(aboutText, isEditing) { mutableStateOf(aboutText) }

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
                            editedAboutText = aboutText // Load current text into editor state
                            isEditing = true
                        },
                        modifier = Modifier.padding(top = 4.dp) // Adjust padding slightly if needed
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit About Section", // For accessibility
//                            tint = LightGrey // Make icon visually distinct
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // Space below the header row

            if (isEditing) {
                // --- Edit Mode ---
                OutlinedTextField(
                    value = editedAboutText,
                    onValueChange = { editedAboutText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    label = {
                        Text(
                            "Professional Summary",
                            color = Color.Black
                        )
                    }, // More descriptive label
                    maxLines = 5,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Charcoal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        cursorColor = Charcoal // Cursor matches text color
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isEditing = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LightGrey, // Background color
                            contentColor = Charcoal    // Text color
                        )
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            // TODO: Add optional loading state indication here
//                            userViewModel.updateUserAbout(userId, editedAboutText)
                            // Note: The parent composable observing the ViewModel state
                            // should provide the updated `aboutText` prop eventually.
                            isEditing = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Charcoal, // Background color
                            contentColor = LightGrey   // Text color
                        )
                    ) {
                        Text("Save")
                    }
                }
            } else {
                // --- Display Mode ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top // Align icon nicely with text
                ) {
                    Text(
                        // Display saved text or the placeholder
                        text = aboutText.ifEmpty { "Add your dining preferences" },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp, bottom = 8.dp, end = 8.dp),
                        color = Charcoal
                    )

                }
            }
        }
    }
}