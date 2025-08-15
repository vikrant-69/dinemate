package com.hackathon.dinemate.user


import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun InputUserDetailsScreen(
    userId: String,
    navController: NavController,
    userViewModel: UserViewModel,
    context: Context
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var firstNameError by remember { mutableStateOf(false) }
    var lastNameError by remember { mutableStateOf(false) }
    var userNameInvalidError by remember { mutableStateOf(false) }

    val cards = listOf(
        "DINEMATE"
    )
    var currentCardIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            currentCardIndex = (currentCardIndex + 1) % cards.size
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Green)
            .padding(16.dp)
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
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Complete Sign In",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )

        OutlinedTextField(
            value = firstName,
            onValueChange = {
                firstName = it
                firstNameError = it.isBlank()
                userNameInvalidError = !it.matches(Regex("^[a-zA-Z0-9_]+\$") )
            },
            label = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Green)

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
//            textStyle = MaterialTheme.typography.bodyLarge.copy(color = HireColor),
            colors = OutlinedTextFieldDefaults.colors(
//                focusedBorderColor = Color.Black,
//                cursorColor = HireColor, // Cursor matches text color,
//                focusedContainerColor = LightGrayBackground,
//                unfocusedContainerColor = LightGrayBackground,
//                errorContainerColor = LightGrayBackground,
//                unfocusedLabelColor = HireColor,
//                focusedLabelColor = HireColor
            )
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
                            firstNameError -> "This field can't be empty"
                            userNameInvalidError -> "Only letters, numbers, and underscore allowed"
                            else -> "Last Name"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (firstNameError || userNameInvalidError) Color.Red else Color.Unspecified
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            isError = lastNameError,
//            textStyle = MaterialTheme.typography.bodyLarge.copy(color = HireColor),
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedBorderColor = Color.Black,
//                cursorColor = HireColor, // Cursor matches text color,
//                focusedContainerColor = LightGrayBackground,
//                unfocusedContainerColor = LightGrayBackground,
//                errorContainerColor = LightGrayBackground,
//                unfocusedLabelColor = HireColor,
//                focusedLabelColor = HireColor
//            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            firstNameError = firstName.isBlank()
            lastNameError = lastName.isBlank()

            if (firstNameError || lastNameError) {
                return@Button
            }

            userViewModel.completeInitialUserInfo(userId, firstName, lastName, context, navController)
        },
            colors = ButtonDefaults.buttonColors(
//                containerColor = HireColor, // Background color
//                contentColor = LightGrayBackground   // Text color
            ),
            modifier = Modifier.width(180.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Create Profile",
                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Save Information: Move Forward",
                modifier = Modifier
                    .size(30.dp)
            )
        }
    }
}