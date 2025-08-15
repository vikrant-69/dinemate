package com.hackathon.dinemate.home

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.hackathon.dinemate.user.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    userId: String,
    context: Context,
    userViewModel: UserViewModel
){
    Column {
        Text(
            text = "Welcome Home"
        )
    }
}


