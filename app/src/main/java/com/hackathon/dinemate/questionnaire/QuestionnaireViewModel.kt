package com.hackathon.dinemate.questionnaire

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.hackathon.dinemate.util.HttpUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class QuestionnaireViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionnaireUiState())
    val uiState: StateFlow<QuestionnaireUiState> = _uiState.asStateFlow()

    private var currentUserId: String = ""
    private var baseURL: String = ""

    private val db = Firebase.firestore

    private val questions = listOf(
        Question(
            id = "dietary_restrictions",
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
            id = "preferred_cuisines",
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
            id = "spice_tolerance",
            text = "Select spice levels you enjoy",
            options = listOf(
                Option("mild", "Mild", "mild"),
                Option("medium", "Medium", "medium"),
                Option("hot", "Hot", "hot"),
                Option("extra_hot", "Extra Hot", "extra_hot")
            ),
            maxSelectable = 1
        ),
        Question(
            id = "budget_preference",
            text = "Select your budget",
            options = listOf(
                Option("less_than_500", "< 500", "less_than_500"),
                Option("less_than_1000", "< 1000", "less_than_1000"),
                Option("less_than_2000", "< 2000", "less_than_2000"),
                Option("less_than_5000", "< 5000", "less_than_5000"),
                Option("no_budget", "No Budget", "no_budget")
            ),
            maxSelectable = 1
        ),
        Question(
            id = "dining_style",
            text = "Select your dining style",
            options = listOf(
                Option("fine_dining", "Fine Dining", "fine_dining"),
                Option("casual_dining", "Casual Dining", "casual_dining"),
                Option("fast_food", "Fast Food", "fast_food"),
                Option("cafes", "Cafes", "cafes"),
                Option("buffet", "Buffet", "buffet")
            ),
            maxSelectable = 2
        )
    )

    fun initializeQuestionnaire(userId: String, baseUrl: String = "") {
        currentUserId = userId
        baseURL = baseUrl
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
            saveAnswersToServer()
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

    private fun buildAnswersFromSelections(): List<UserAnswer> {
        val selections = _uiState.value.selections // Map<questionId, Set<optionId>>
        return selections.map { (questionId, selectedIds) ->
            val q = questions.first { it.id == questionId }
            val selectedOptions = q.options.filter { it.id in selectedIds }
            UserAnswer(
                questionId = questionId,
                selectedOptionIds = selectedOptions.map { it.id },
                selectedValues = selectedOptions.map { it.value }
            )
        }
    }

    private fun saveAnswersToServer() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                // Step 1: Send preferences to your backend API
                val answers = buildAnswersFromSelections()
                val jsonBody = buildPostBody(currentUserId, answers)
                val url = "${baseURL.trimEnd('/')}/api/v1/user/update_preferences"
                val response = withContext(Dispatchers.IO) { HttpUtil.post(url, jsonBody) }

                Log.d("Questionnaire", "API response statusCode: ${response.statusCode}")

                if (response.statusCode == 200) { // Check for successful API response
                    // Step 2: Save the same preferences to the User object in Firestore
                    val preferencesList = getAllSelectedPreferences()
                    db.collection("dinemate_users").document(currentUserId)
                        .update("preferences", preferencesList)
                        .await() // Wait for the update to complete

                    Log.d("Questionnaire", "Firestore preferences updated successfully.")

                    // Step 3: Mark as completed only after all saving is done
                    _uiState.value = _uiState.value.copy(isSaving = false, isCompleted = true)

                } else {
                    throw Exception("API call failed with status code ${response.statusCode}")
                }

            } catch (e: Exception) {
                Log.e("Questionnaire", "Failed to save preferences", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Failed to save preferences: ${e.message}"
                )
            }
        }
    }

    private fun buildPostBody(firebaseId: String, answers: List<UserAnswer>): String {
        val root = JSONObject().apply {
            put("firebase_id", firebaseId)
        }

        val preferences = JSONObject()
        val answerMap = answers.associateBy { it.questionId }

        answerMap["dietary_restrictions"]?.let { ans ->
            preferences.put("dietary_restrictions", JSONArray(ans.selectedValues))
        }

        answerMap["preferred_cuisines"]?.let { ans ->
            preferences.put("preferred_cuisines", JSONArray(ans.selectedValues))
        }

        answerMap["spice_tolerance"]?.let { ans ->
            val value = ans.selectedValues.firstOrNull() ?: ""
            preferences.put("spice_tolerance", value)
        }

         answerMap["budget_preference"]?.let { ans ->
             val value = ans.selectedValues.firstOrNull() ?: ""
             preferences.put("budget_preference", value)
         }

        answerMap["dining_style"]?.let { ans ->
            preferences.put("dining_style", JSONArray(ans.selectedValues))
        }

        root.put("preferences", preferences)
        return root.toString()
    }

    private fun getAllSelectedPreferences(): List<String> {
        val selections = _uiState.value.selections
        val allPreferences = mutableListOf<String>()
        selections.forEach { (questionId, selectedIds) ->
            val question = questions.find { it.id == questionId }
            question?.options?.forEach { option ->
                if (selectedIds.contains(option.id)) {
                    allPreferences.add(option.text) // Storing the display text e.g., "Vegetarian"
                }
            }
        }
        return allPreferences
    }
}