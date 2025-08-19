package com.hackathon.dinemate

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hackathon.dinemate.home.GroupChatScreen
import com.hackathon.dinemate.user.InputUserDetailsScreen
import com.hackathon.dinemate.signin.SignInScreen
import com.hackathon.dinemate.ui.theme.DineMateTheme
import com.hackathon.dinemate.user.UserViewModel
import com.hackathon.dinemate.home.HomeScreen
import com.hackathon.dinemate.questionnaire.QuestionnaireScreen

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DineMateTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    FirebaseApp.initializeApp(LocalContext.current)

                    // Get application context
                    val application = LocalContext.current.applicationContext as Application

                    // Pass the context
                    AppNavigation(
                        activity = this@MainActivity,
                        application = application
                    )
                }
            }
        }
    }
}


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun AppNavigation(
    activity: MainActivity,
    application: Application
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(LocalContext.current.applicationContext as Application)
    )

    // A state to track if the authentication check is complete
    var isAuthChecked by remember { mutableStateOf(false) }
    // A state to hold the determined start destination
    var startDestination by remember { mutableStateOf("loading") } // Start with a temporary loading route

    // Perform the authentication check once
    LaunchedEffect(Unit) {
        val user = Firebase.auth.currentUser
        Log.d("USER", user.toString())
        if (user != null) {
            Log.d("Navigation", "User is signed in. Navigating to Home Screen.")
            startDestination = "homeScreen/${user.uid}"
        } else {
            Log.d("Navigation", "User is not signed in. Navigating to Sign-In Screen.")
            startDestination = "signInScreen"
        }
        // Mark the check as complete
        isAuthChecked = true
    }

    // Show a loading screen while checking auth, then show the NavHost
    if (isAuthChecked) {
        NavHost(navController = navController, startDestination = startDestination) {
            composable("signInScreen") {
                SignInScreen(navController, context)
            }

            composable(
                "inputUserDetails/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                InputUserDetailsScreen(userId, navController, userViewModel, context)
            }

            composable(
                "questionnaire/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                QuestionnaireScreen(
                    userId = userId,
                    onComplete = {
                        navController.navigate("homeScreen/$userId") {
                            popUpTo("questionnaire/$userId") { inclusive = true }
                        }
                    }
                )
            }

            composable(
                "homeScreen/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                HomeScreen(
                    userId = userId,
                    onNavigateToChat = { group ->
                        navController.navigate(
                            "groupChat/${group.id}/${group.name}/${group.invite_code ?: ""}/${userId}"
                        )
                    }
                )
            }

            composable(
                "groupChat/{groupId}/{groupName}/{inviteCode}/{userId}",
                arguments = listOf(
                    navArgument("groupId") { type = NavType.StringType },
                    navArgument("groupName") { type = NavType.StringType },
                    navArgument("inviteCode") {
                        type = NavType.StringType
                        defaultValue = ""
                        nullable = true
                    },
                    navArgument("userId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                val groupName = backStackEntry.arguments?.getString("groupName") ?: ""
                val inviteCode = backStackEntry.arguments?.getString("inviteCode")
                val userId = backStackEntry.arguments?.getString("userId") ?: ""

                GroupChatScreen(
                    userId = userId,
                    groupId = groupId,
                    groupName = groupName,
                    inviteCode = inviteCode,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

class UserViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}