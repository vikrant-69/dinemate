package com.hackathon.dinemate

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.hackathon.dinemate.ui.theme.Charcoal
import com.hackathon.dinemate.ui.theme.LightGrey
import com.hackathon.dinemate.ui.theme.MediumGrey
import com.hackathon.dinemate.user.AboutSection
import com.hackathon.dinemate.user.UserViewModel
import kotlinx.coroutines.launch

/** Profile domain model */
data class UserProfile(
    val firstName: String = "",
    val secondName: String = "",
    val email: String = "",
    val phone: String = "",
    val dietaryPreference: DietaryPreference = DietaryPreference.None,
    val favoriteCuisines: Set<String> = emptySet(),
    val notifyOrders: Boolean = true,
    val notifyPromos: Boolean = false
)

enum class DietaryPreference { None, Vegetarian, Vegan, NonVegetarian, Eggetarian }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTab(
    userViewModel: UserViewModel,
    padding: PaddingValues
) {
    val user by userViewModel.user.collectAsState()

//    Log.d("USER INFO", user.toString())

    var profilePicUri by remember { mutableStateOf<Uri?>(Uri.parse(user?.profilePic))}
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
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
//                .padding(horizontal = 16.dp)
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
                            modifier = Modifier.align(Alignment.Center).size(50.dp),
//                                    tint = Color.White
                        )
                    }

                }

                Spacer(modifier = Modifier.width(20.dp))

                Column (
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ){
                    user?.let {
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = Charcoal
                        )
                    }

                    Row (
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ){
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






                if (showProfilePicDialog) {
                    AlertDialog(
                        containerColor = Color.White,
                        onDismissRequest = { showProfilePicDialog = false },
                        title = {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Update Profile Picture",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Charcoal
                                )
                            }
                        },
                        text = {
                            Column {
                                Box(
                                    modifier = Modifier
                                        //                                            .background(LightGrayBackground)
                                        .size(150.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, LightGrey, RoundedCornerShape(8.dp))
                                        .align(Alignment.CenterHorizontally)
                                ) {
                                    if (newProfilePicUri != null) {
                                        Image(
                                            painter = rememberImagePainter(newProfilePicUri),
                                            contentDescription = "New Profile Picture",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Default Profile Picture",
                                            modifier = Modifier.fillMaxSize(),
                                            tint = Charcoal
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { imageProfilePickerLauncher.launch("image/*") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = LightGrey)
                                ) {
                                    Text("Choose Photo", color = Color.White)
                                }
                            }
                        },
                        confirmButton = {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Button(
                                    onClick = {
                                        showProfilePicDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LightGrey, // Background color
                                        contentColor = Charcoal    // Text color
                                    )
                                ) {
                                    Text("Cancel")
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Row(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Charcoal)
                                ){
                                    Button(
                                        onClick = {
                                            newProfilePicUri?.let {
    //                                            userViewModel.saveProfilePicture(context, userId, it) { _, message ->
    //                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    //                                            }
                                            }
                                            showProfilePicDialog = false
                                        },
                                        enabled = newProfilePicUri != null
                                    ) {
                                        Text("Save", color = LightGrey)
                                    }
                                }

                            }
                        }
                    )
                }


            }
            AboutSection(aboutText = aboutText)

        }

    }
}


//@Preview(showBackground = true)
//@Composable
//private fun ProfileTabPreview() {
//    ProfileTab(
//
//    )
//}
