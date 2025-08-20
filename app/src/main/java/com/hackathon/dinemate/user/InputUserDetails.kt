package com.hackathon.dinemate.user

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.hackathon.dinemate.DineMateIconOnly
import com.hackathon.dinemate.LogoSize


@Composable
fun InputUserDetailsScreen(
    userId: String,
    navController: NavHostController,
    userViewModel: UserViewModel,
    context: Context
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var firstNameError by remember { mutableStateOf(false) }
    var lastNameError by remember { mutableStateOf(false) }
    var userNameInvalidError by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Validate individual field
    fun validateField(value: String, fieldName: String): String? {
        return when {
            value.isBlank() -> "Please enter your $fieldName"
            value.length < 2 -> "$fieldName must be at least 2 characters"
            else -> null
        }
    }

    // Check if form is valid
    val isFormValid = firstName.length >= 2 &&
            lastName.length >= 2 &&
            firstNameError == null &&
            lastNameError == null

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderSection()

            Column {
                // Welcome Text
                Text(
                    text = "Welcome! Let's get to know you better to personalize your dining experience.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF7f8c8d),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )

                // Success Message
                AnimatedVisibility(
                    visible = showSuccess,
                    enter = slideInVertically() + fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFd4edda)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Profile created successfully! 🎉",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF155724),
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }

                Column (
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){


                    // First Name Field
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = {
                            firstName = it
                            firstNameError = it.isBlank()
                            userNameInvalidError = !it.matches(Regex("^[a-zA-Z0-9_]+\$"))
                        },
                        label = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                Text(
                                    when {
                                        firstNameError -> "This field can't be empty"
                                        userNameInvalidError -> "Only letters, numbers, and underscore allowed"
                                        else -> "First Name"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (firstNameError || userNameInvalidError) Color.Red else Color.Unspecified
                                )
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        isError = firstNameError || userNameInvalidError,
                    )

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = {
                            lastName = it
                            lastNameError = it.isBlank()
                        },
                        label = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
//                        .background(LightGrayBackground)

                            ) {
                                Text(
                                    when {
                                        lastNameError -> "This field can't be empty"
                                        userNameInvalidError -> "Only letters, numbers, and underscore allowed"
                                        else -> "Last Name"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (firstNameError || userNameInvalidError) Color.Red else Color.Unspecified
                                )
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        isError = lastNameError
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }

            }

            // Continue Button
            val buttonScale by animateFloatAsState(
                targetValue = if (isFormValid) 1f else 0.98f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )

            Button(
                onClick = {
                    firstNameError = firstName.isBlank()
                    lastNameError = lastName.isBlank()

                    if (firstNameError || lastNameError) {
                        return@Button
                    }
                    isLoading = true
//                    userViewModel.completeInitialUserInfo(userId, firstName, lastName, context, navController)
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(buttonScale),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color(0xFFbdc3c7)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (isFormValid) {
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFff6b6b), Color(0xFFee5a24))
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFbdc3c7), Color(0xFFbdc3c7))
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Continue to Preferences",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }

}

@Composable
private fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
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

        Icon(
            imageVector = Icons.Default.Restaurant,
            contentDescription = "Dining",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
        )
    }
}

//
//@Preview(showBackground = true)
//@Composable
//fun InputUserDetailsScreenPreview() {
//    MaterialTheme {
//        UserDetailsScreen(
////            "lol",
//////            NavController,
////            userViewModel = TODO(),
////            context = TODO(),
//        )
//    }
//}
