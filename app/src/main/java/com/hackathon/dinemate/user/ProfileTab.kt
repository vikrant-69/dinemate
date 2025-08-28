package com.hackathon.dinemate.user

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.hackathon.dinemate.ui.theme.Charcoal
import com.hackathon.dinemate.ui.theme.LightGrey


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTab(
    userViewModel: UserViewModel,
    padding: PaddingValues,
    navController: NavController,
    context: Context
) {
    val user by userViewModel.user.collectAsState()
    LaunchedEffect(user) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            Log.d("ProfileTab", "User is null but Firebase user exists, reloading...")
            userViewModel.reloadUserFromFirestore(currentUser.uid)
        }
    }
    Log.d("USER_INFO1", user.toString())

    var profilePicUri by remember { mutableStateOf<Uri?>(user?.profilePic?.toUri()) }
    var newProfilePicUri by remember { mutableStateOf<Uri?>(null) }
    val imageProfilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        newProfilePicUri = uri
    }
    var showProfilePicDialog by remember { mutableStateOf(false) }


    var aboutText by remember { mutableStateOf("") }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .padding(10.dp)
                        .clip(CircleShape)
                        .border(3.dp, Charcoal, CircleShape)
                        .clickable { showProfilePicDialog = true }
                        .background(Color.White)
                ) {
                    if (profilePicUri != null) {
                        Image(
                            painter = rememberImagePainter(profilePicUri),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(50.dp)
                        )
                    }

                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ) {
                    user?.let {
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = Charcoal
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Subscription",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = Charcoal.copy(0.6f)
                        )

                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .offset(y = (-4).dp)
                                .size(18.dp),
                            tint = Charcoal.copy(0.6f)
                        )
                    }

                }

            }
            PreferencesSection(
                userViewModel,
                navController
            )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
//                            navController.navigate("contactUs")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Charcoal, // Background color
                            contentColor = LightGrey   // Text color
                        ),
                        modifier = Modifier.fillMaxWidth(0.4f)
                    ) {
                        AutoResizeText(
                            text = "About Us",
                            style = TextStyle(fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            userViewModel.signOut(
                                onComplete = {
                                    navController.navigate("signInScreen") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onError = { error ->
                                    Toast.makeText(context, "Sign-out failed: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Charcoal, // Background color
                            contentColor = LightGrey   // Text color
                        ),
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        AutoResizeText(
                            text = "Sign Out",
                            style = TextStyle(fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                        )
                    }
                }
            }
        }
    }
}
