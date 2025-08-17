package com.hackathon.dinemate.questionnaire

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuestionnaireViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionnaireUiState())
    val uiState: StateFlow<QuestionnaireUiState> = _uiState.asStateFlow()

    private var currentUserId: String = ""

    // Example questions with maxSelectable per question
    private val questions = listOf(
        Question(
            id = "dietary_preference",
            text = "Select your dietary preference(s)",
            options = listOf(
                Option("vegetarian", "Vegetarian", "vegetarian"),
                Option("vegan", "Vegan", "vegan"),
                Option("non_vegetarian", "Non-Vegetarian", "non_vegetarian"),
                Option("pescatarian", "Pescatarian", "pescatarian"),
                Option("gluten_free", "Gluten-free", "gluten_free")
            ),
            maxSelectable = 2
        ),
        Question(
            id = "cuisine_preference",
            text = "Select cuisines you like",
            options = listOf(
                Option("italian", "Italian", "italian"),
                Option("indian", "Indian", "indian"),
                Option("thai", "Thai", "thai"),
                Option("chinese", "Chinese", "chinese"),
                Option("mexican", "Mexican", "mexican"),
                Option("japanese", "Japanese", "japanese"),
                Option("mediterranean", "Mediterranean", "mediterranean")
            ),
            maxSelectable = 3
        ),
        Question(
            id = "spice_level",
            text = "Select spice levels you enjoy",
            options = listOf(
                Option("mild", "Mild", "mild"),
                Option("medium", "Medium", "medium"),
                Option("hot", "Hot", "hot"),
                Option("extra_hot", "Extra Hot", "extra_hot")
            ),
            maxSelectable = 2
        )
    )

    fun initializeQuestionnaire(userId: String) {
        currentUserId = userId
        _uiState.value = QuestionnaireUiState(
            currentQuestion = questions.firstOrNull(),
            currentQuestionIndex = 0,
            totalQuestions = questions.size,
            selections = mutableMapOf()
        )
    }

    fun toggleOption(option: Option) {
        val q = _uiState.value.currentQuestion ?: return
        val selections = _uiState.value.selections.toMutableMap()
        val current = selections[q.id]?.toMutableSet() ?: mutableSetOf()

        if (current.contains(option.id)) {
            current.remove(option.id)
            selections[q.id] = current
            _uiState.value = _uiState.value.copy(
                selections = selections,
                transientMessage = null
            )
        } else {
            // Enforce limit
            if (current.size >= q.maxSelectable) {
                _uiState.value = _uiState.value.copy(
                    transientMessage = null
                )
            } else {
                current.add(option.id)
                selections[q.id] = current
                _uiState.value = _uiState.value.copy(
                    selections = selections,
                    transientMessage = null
                )
            }
        }
    }

    fun goToNextQuestion() {
        val currentIndex = _uiState.value.currentQuestionIndex
        val nextIndex = currentIndex + 1

        if (nextIndex < questions.size) {
            _uiState.value = _uiState.value.copy(
                currentQuestion = questions[nextIndex],
                currentQuestionIndex = nextIndex,
                transientMessage = null
            )
        } else {
            // Finalize
            _uiState.value = _uiState.value.copy(isCompleted = true)
            saveAnswersToMongoDB()
        }
    }

    fun goToPreviousQuestion() {
        val currentIndex = _uiState.value.currentQuestionIndex
        val prevIndex = currentIndex - 1
        if (prevIndex >= 0) {
            _uiState.value = _uiState.value.copy(
                currentQuestion = questions[prevIndex],
                currentQuestionIndex = prevIndex,
                transientMessage = null
            )
        }
    }

    private fun saveAnswersToMongoDB() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true)
                val answers = _uiState.value.selections.map { (questionId, selectedIds) ->
                    val q = questions.first { it.id == questionId }
                    UserAnswer(
                        questionId = questionId,
                        selectedOptionIds = selectedIds.toList(),
                        selectedValues = q.options.filter { it.id in selectedIds }.map { it.value }
                    )
                }
                // TODO: Replace with actual persistence
                // mongoDbService.saveUserPreferences(currentUserId, answers)

                _uiState.value = _uiState.value.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Failed to save preferences: ${e.message}"
                )
            }
        }
    }
}

data class QuestionnaireUiState(
    val currentQuestion: Question? = null,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val selections: MutableMap<String, Set<String>> = mutableMapOf(),
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val transientMessage: String? = null
)
