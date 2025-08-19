package com.hackathon.dinemate.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.hackathon.dinemate.util.HttpUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class GroupChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GroupChatUiState())
    val uiState: StateFlow<GroupChatUiState> = _uiState

    private var userId: String = ""
    private var baseURL: String = ""
    private var groupId: String = ""

    fun initialize(userId: String, baseURL: String, groupId: String) {
        this.userId = userId
        this.baseURL = baseURL
        this.groupId = groupId
    }

    fun onInputChange(value: String) {
        _uiState.value = _uiState.value.copy(input = value)
    }

    fun fetchMessages() {
        if (groupId.isEmpty()) return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val url = "${baseURL.trimEnd('/')}/api/v1/group/${groupId}/messages?firebase_id=${userId}"
                Log.d("TAG", url)

                val response = withContext(Dispatchers.IO) {
                    HttpUtil.get(url)
                }

                val arr = JSONArray(response.body)
                val messages = mutableListOf<Message>()
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    messages.add(
                        Message(
                            id = item.optString("id"),
                            group_id = item.optString("group_id"),
                            user_id = item.optString("user_id"),
                            user_name = item.optString("user_name"),
                            message_type = item.optString("message_type"),
                            content = item.optString("content"),
                            restaurant_data = item.opt("restaurant_data")?.toString(),
                            created_at = item.optString("created_at")
                        )
                    )
                }

                _uiState.value = _uiState.value.copy(isLoading = false, messages = messages)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to fetch messages")
            }
        }
    }

    fun sendMessage() {
        val text = _uiState.value.input.trim()
        if (text.isEmpty() || _uiState.value.isSending || groupId.isEmpty()) return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSending = true, error = null)
                val url = "${baseURL.trimEnd('/')}/api/v1/group/${groupId}/messages"
                val payload = JSONObject().apply {
                    put("content", text)
                    put("message_type", "text")
                    put("restaurant_data", JSONObject.NULL)
                    put("firebase_id", userId)
                }.toString()

                withContext(Dispatchers.IO) {
                    HttpUtil.post(url, payload)
                }

                // Clear input and refresh messages
                _uiState.value = _uiState.value.copy(input = "", isSending = false)
                fetchMessages()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSending = false, error = "Failed to send message")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class GroupChatUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val messages: List<Message> = emptyList(),
    val input: String = "",
    val error: String? = null
)