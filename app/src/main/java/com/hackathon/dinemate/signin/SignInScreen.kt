package com.hackathon.dinemate.signin


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hackathon.dinemate.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hackathon.dinemate.DineMateIconOnly
import com.hackathon.dinemate.LogoSize
import com.hackathon.dinemate.user.User
import com.hackathon.dinemate.user.UserViewModel


@Composable
fun SignInScreen(
    userViewModel: UserViewModel,
    navController: NavController,
    context: Context,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
        GoogleSignInUtils.performSignIn(
            context,
            scope,
            null,
            login = { userId ->
                Toast.makeText(context, "signin successfully", Toast.LENGTH_SHORT).show()
                navController.navigate("homeScreen/$userId")
            }
        )
    }


    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            DineMateIconOnly(
                size = LogoSize.Medium,
                showAnimation = true
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFFFF6B6B))) {
                        append("Dine")
                    }
                    withStyle(SpanStyle(color = Color(0xFF4ECDC4))) {
                        append("Mate")
                    }
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your trusted dining companion",
            color = Color(0xFF6B7280),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Welcome text
        Text(
            text = "Welcome!",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A)
        )

        Text(
            text = "Sign in to discover amazing restaurants tailored just for you",
            color = Color(0xFF6B7280),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier
                .fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedButton(
            onClick = {
                isLoading = true
                GoogleSignInUtils.performSignIn(
                    context,
                    scope,
                    launcher,
                    login = {
                        val user = Firebase.auth.currentUser

                        user?.let {
                            val userId = it.uid
                            val name = it.displayName ?: "Unknown"
                            val userName = it.email?.split("@")?.get(0) ?: "NoName"
                            val profilPicUri = it.photoUrl?.toString() ?: ""

                            val currentUser = User(
                                userId,
                                userName,
                                name,
                                profilPicUri
                            )
                            val usersRef = Firebase.firestore.collection("dinemate_users")

                            usersRef.whereEqualTo("userId", userId).get()
                                .addOnSuccessListener { querySnapshot ->
                                    if (querySnapshot.isEmpty) {
                                        userViewModel.saveUserProfile(currentUser, context, navController)
                                        userViewModel.loadUserAfterAuth()
                                    }

                                    isLoading = false


                                    navController.navigate("homeScreen/$userId") {
                                        popUpTo("signInScreen") { inclusive = true }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FirestoreDebug", "Error fetching user data", e)
                                    isLoading = false
                                }
                        }
                    }
                )
            },
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,        // White background
                contentColor = Color.DarkGray        // Dark gray text
            ),
            border = BorderStroke(
                width = 1.dp,
                color = Color.DarkGray               // Dark gray outline
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.width(250.dp)
        ){
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.DarkGray,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sign In With Google",
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

        Spacer(modifier = Modifier.height(32.dp))

        // Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(
                modifier = Modifier.weight(1f),
                color = Color(0xFFE5E7EB)
            )
            Text(
                text = "Secure & Fast",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color(0xFF9CA3AF),
                fontSize = 14.sp
            )
            Divider(
                modifier = Modifier.weight(1f),
                color = Color(0xFFE5E7EB)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Terms text
        Text(
            text = "By continuing, you agree to our Terms of Service and Privacy Policy",
            color = Color(0xFF9CA3AF),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier
                .fillMaxWidth(0.8f  )
        )
    }
}