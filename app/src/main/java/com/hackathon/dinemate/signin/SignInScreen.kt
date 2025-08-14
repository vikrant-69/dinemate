package com.hackathon.dinemate.signin


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hackathon.dinemate.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay


@Composable
fun SignInScreen(navController: NavController, context: Context, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestore = com.google.firebase.Firebase.firestore
    val cards = listOf(
        "DINEMATE."
    )
    val auth = Firebase.auth

    val user = auth.currentUser

    // Check if user is already signed in
    LaunchedEffect(user) {
        user?.let {
            val userId = it.uid
            val usersRef = firestore.collection("dinemate_users")

            usersRef.whereEqualTo("userId", userId).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
//                        val userId = document.getString("userId") ?: "User"
                        Log.d("FirestoreDebug", "Document found: ${document.data}")
                        navController.navigate("homeScreen/$userId") {
                            popUpTo("signInScreen") { inclusive = true }
                        }
                    } else {
                        Log.d("FirestoreDebug", "Document does not exist!")
                        navController.navigate("inputUserDetails/$userId") {
                            popUpTo("signInScreen") { inclusive = true }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreDebug", "Error fetching user data", e)
                }
        }
    }
    var currentCardIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            currentCardIndex = (currentCardIndex + 1) % cards.size
        }
    }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        GoogleSignInUtils.performSignIn(
            context,
            scope,
            null,
            login = { userId ->
                Toast.makeText(context, "login successfully", Toast.LENGTH_SHORT).show()
                navController.navigate("inputUserDetails/$userId")
            }
        )
    }


    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color.Green)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Green, // Center color
                        Color.Transparent // Fades outward
                    ),
                    center = Offset.Unspecified, // Automatically centers
                    radius = 800f // Adjust the radius as needed
                )
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val currentText = cards[currentCardIndex]
        val parts = currentText.split(",", limit = 2)
        val annotatedText = buildAnnotatedString {
            append(
                AnnotatedString(
                    text = parts[0] + ",",
                    spanStyle = SpanStyle(color = Color.Red)
                )
            )

            if (parts.size > 1){
                append(
                    AnnotatedString(
                        text = parts[1],
                        spanStyle = SpanStyle(color = Color.Blue)
                    )
                )
            }
        }
        Text(
            text = annotatedText,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                GoogleSignInUtils.performSignIn(
                    context,
                    scope,
                    launcher,
                    login = {
                        val user = Firebase.auth.currentUser
                        user?.let {
                            val userId = it.uid // Get userId from Firebase Auth
                            Log.d("USERID", userId)
                            val usersRef = Firebase.firestore.collection("dinemate_users")

                            usersRef.whereEqualTo("userId", userId).get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (!querySnapshot.isEmpty) {
                                        // Document exists, retrieve userName
                                        val document = querySnapshot.documents[0] // Get the first matching document
                                        val userName = document.getString("userId") ?: "User"
                                        Log.d("FirestoreDebug", "Document found: ${document.data}")
                                        navController.navigate("homeScreen/$userId")
                                    } else {
                                        Log.d("FirestoreDebug", "Document does not exist!")
                                        navController.navigate("inputUserDetails/$userId")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FirestoreDebug", "Error fetching user data", e)
                                }
                        }
                    }
                )
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red, // Background color
                contentColor = Color.Green   // Text color
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.width(250.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Google Icon",
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Sign In With Google",
//                    fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Google Sign In: Move Forward",
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer(scaleX = 1.3f, scaleY = 1.3f)
            )
        }
    }
}