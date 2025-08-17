package com.hackathon.dinemate.questionnaire

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hackathon.dinemate.ui.theme.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuestionnaireScreen(
    userId: String,
    onComplete: () -> Unit,
    viewModel: QuestionnaireViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.initializeQuestionnaire(userId)
    }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) onComplete()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preferences Setup", color = Charcoal) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightGrey)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ProgressSection(
                currentStep = uiState.currentQuestionIndex + 1,
                totalSteps = uiState.totalQuestions
            )

            Spacer(modifier = Modifier.height(24.dp))

            uiState.currentQuestion?.let { question ->
                QuestionContent(
                    question = question,
                    selectedIds = uiState.selections[question.id] ?: emptySet(),
                    onToggle = viewModel::toggleOption
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Max selectable hint and transient message
//            uiState.currentQuestion?.let { q ->
//                Text(
//                    text = "Select up to ${q.maxSelectable} option(s).",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MediumGrey
//                )
//            }

            uiState.transientMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                AssistChipMessage(text = msg)
            }

            Spacer(modifier = Modifier.height(8.dp))

            NavigationButtons(
                canGoBack = uiState.currentQuestionIndex > 0,
                canGoForward = uiState.currentQuestion?.let { q ->
                    (uiState.selections[q.id]?.isNotEmpty() == true)
                } == true,
                isLastQuestion = uiState.currentQuestionIndex == uiState.totalQuestions - 1,
                onBackClick = viewModel::goToPreviousQuestion,
                onNextClick = viewModel::goToNextQuestion
            )
        }
    }

    if (uiState.isSaving) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Saving Preferences") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Please wait...")
                }
            },
            confirmButton = { }
        )
    }

    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { /* Dismiss error - ideally clear error in ViewModel */ }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun ProgressSection(
    currentStep: Int,
    totalSteps: Int
) {
    Column {
        Text(
            text = "Step $currentStep of $totalSteps",
            style = MaterialTheme.typography.bodyMedium,
            color = MediumGrey
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps.toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = DarkGrey
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionContent(
    question: Question,
    selectedIds: Set<String>,
    onToggle: (Option) -> Unit
) {
    Column {
        Text(
            text = question.text,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = Charcoal
        )

        Spacer(modifier = Modifier.height(24.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            question.options.forEach { option ->
                val isSelected = selectedIds.contains(option.id)
                SelectableChip(
                    label = option.text,
                    selected = isSelected,
                    onClick = { onToggle(option) }
                )
            }
        }
    }
}

@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (selected) DarkGrey else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) DarkGrey else MediumGrey.copy(alpha = 0.4f)),
        tonalElevation = if (selected) 6.dp else 0.dp,
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Text(
            text = label,
            color = if (selected) White else Black,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(horizontal = 40.dp, vertical = 25.dp)
        )
    }
}

@Composable
private fun AssistChipMessage(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = LightGrey,
        border = BorderStroke(1.dp, MediumGrey.copy(alpha = 0.25f))
    ) {
        Text(
            text = text,
            color = Charcoal,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun NavigationButtons(
    canGoBack: Boolean,
    canGoForward: Boolean,
    isLastQuestion: Boolean,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (canGoBack) {
            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Charcoal)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Previous")
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = onNextClick,
            enabled = canGoForward,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Charcoal,
                contentColor = White
            )
        ) {
            Text(if (isLastQuestion) "Complete" else "Next")
            if (!isLastQuestion) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next"
                )
            }
        }
    }
}
