package com.hackathon.dinemate.questionnaire

data class Question(
    val id: String,
    val text: String,
    val options: List<Option>,
    val maxSelectable: Int = Int.MAX_VALUE
)

data class Option(
    val id: String,
    val text: String,
    val value: String
)

data class UserAnswer(
    val questionId: String,
    val selectedOptionIds: List<String>, // multiple
    val selectedValues: List<String>
)

data class QuestionnaireUiState(
    val currentQuestion: Question? = null,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 0,
    // Map<QuestionId, Set<OptionId>>
    val selections: MutableMap<String, Set<String>> = mutableMapOf(),
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val transientMessage: String? = null
)