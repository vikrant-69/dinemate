package com.hackathon.dinemate.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.dinemate.config.AppConfig
import com.hackathon.dinemate.util.HttpUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private var firebaseId: String = ""
    private var baseURL: String = AppConfig.BASE_URL

    fun initialize(userId: String, baseUrl: String = AppConfig.BASE_URL) {
        firebaseId = userId
        baseURL = baseUrl
        fetchUserGroups()
    }

    fun setTab(tab: BottomTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
        if (tab == BottomTab.Groups && uiState.value.groups.isEmpty()) {
            fetchUserGroups()
        }
    }

    // Create group form
    fun onCreateGroupNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(createGroupName = value)
    }

    fun onCreateGroupDescChanged(value: String) {
        _uiState.value = _uiState.value.copy(createGroupDescription = value)
    }

    fun onInviteCodeChanged(value: String) {
        _uiState.value = _uiState.value.copy(inviteCode = value)
    }

    fun onMessageInputChanged(value: String) {
        _uiState.value = _uiState.value.copy(messageInput = value)
    }

    // Create group
    fun createGroup() {
        val name = uiState.value.createGroupName.trim()
        val desc = uiState.value.createGroupDescription.trim()
        if (name.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Group name is required")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null, info = null)
                val url = "${baseURL.trimEnd('/')}/api/v1/group/"

                val json = JSONObject().apply {
                    put("firebase_id", firebaseId)
                    put("name", name)
                    put("description", desc)
                }.toString()

                val response = withContext(Dispatchers.IO) {
                    HttpUtil.post(url, json)
                }

                val obj = JSONObject(response.body)
                val inviteCode = obj.optString("invite_code", "")
                val groupId = obj.optString("id", "")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    inviteCodeCreated = inviteCode,
                    info = if (inviteCode.isNotEmpty()) "Group created. Invite code: $inviteCode" else "Group created"
                )

                // Refresh groups so the newly created group appears in Groups tab immediately
                fetchUserGroups()

                // Optionally auto-select this group in Groups tab:
                if (groupId.isNotEmpty()) {
                    selectGroup(groupId)
                }
            } catch (e: Exception) {
                Log.e("HomeVM", "createGroup failed", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to create group")
            }
        }
    }

    // Join group
    fun joinGroup() {
        val code = uiState.value.inviteCode.trim()
        if (code.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Invite code is required")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null, info = null)
                val url = "${baseURL.trimEnd('/')}/api/v1/group/join"

                val json = JSONObject().apply {
                    put("firebase_id", firebaseId)
                    put("invite_code", code)
                }.toString()

                val response = withContext(Dispatchers.IO) {
                    HttpUtil.post(url, json)
                }

                val obj = JSONObject(response.body)
                val groupId = obj.optString("id", "")
                if (groupId.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        info = "Joined group successfully"
                    )
                    // Refresh list and open chat for this group
                    fetchUserGroups {
                        selectGroup(groupId)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Invalid response from server")
                }
            } catch (e: Exception) {
                Log.e("HomeVM", "joinGroup failed", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to join group")
            }
        }
    }

    fun fetchUserGroups(onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isFetchingGroups = true, error = null)
                val url = "${baseURL.trimEnd('/')}/api/v1/user/groups?firebase_id=${firebaseId}"

                val response = withContext(Dispatchers.IO) {
                    HttpUtil.get(url)
                }

                val obj = JSONObject(response.body)
                val arr = obj.optJSONArray("groups") ?: JSONArray()

                val groups = mutableListOf<GroupSummary>()
                for (i in 0 until arr.length()) {
                    val g = arr.getJSONObject(i)
                    groups.add(
                        GroupSummary(
                            id = g.optString("id"),
                            name = g.optString("name"),
                            description = g.optString("description"),
                            created_by = g.optString("created_by"),
                            invite_code = g.optString("invite_code"),
                            status = g.optString("status"),
                            member_count = g.optInt("member_count"),
                            max_members = g.optInt("max_members"),
                            last_message_at = g.optString("last_message_at"),
                            message_count = g.optInt("message_count"),
                            selected_restaurant = g.opt("selected_restaurant")?.toString(),
                            created_at = g.optString("created_at")
                        )
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isFetchingGroups = false,
                    groups = groups
                )

                onDone?.invoke()
            } catch (e: Exception) {
                Log.e("HomeVM", "fetchUserGroups failed", e)
                _uiState.value = _uiState.value.copy(isFetchingGroups = false, error = "Failed to fetch groups")
            }
        }
    }

    // Messages
    fun fetchMessages(groupId: String) {
        if (groupId.isEmpty()) return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isFetchingMessages = true, error = null)
                val url = "${baseURL.trimEnd('/')}/api/v1/group/${groupId}/messages?firebase_id=${firebaseId}"

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

                _uiState.value = _uiState.value.copy(isFetchingMessages = false, messages = messages)
            } catch (e: Exception) {
                Log.e("HomeVM", "fetchMessages failed", e)
                _uiState.value = _uiState.value.copy(isFetchingMessages = false, error = "Failed to fetch messages")
            }
        }
    }

    fun sendMessage() {
        val groupId = uiState.value.openGroupId.orEmpty()
        if (groupId.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Select a group first")
            return
        }
        val content = uiState.value.messageInput.trim()
        if (content.isEmpty()) return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSendingMessage = true, error = null)
                val url = "${baseURL.trimEnd('/')}/api/v1/group/${groupId}/messages"

                val payload = JSONObject().apply {
                    put("content", content)
                    put("message_type", "text")
                    put("restaurant_data", JSONObject.NULL)
                    put("firebase_id", firebaseId)
                }.toString()

                val response = withContext(Dispatchers.IO) {
                    HttpUtil.post(url, payload)
                }

                Log.d("HomeVM", "sendMessage response: $response")
                _uiState.value = _uiState.value.copy(messageInput = "")
                fetchMessages(groupId)
                _uiState.value = _uiState.value.copy(isSendingMessage = false)
            } catch (e: Exception) {
                Log.e("HomeVM", "sendMessage failed", e)
                _uiState.value = _uiState.value.copy(isSendingMessage = false, error = "Failed to send message")
            }
        }
    }

    fun selectGroup(groupId: String) {
        _uiState.value = _uiState.value.copy(openGroupId = groupId, currentTab = BottomTab.Groups)
        fetchMessages(groupId)
    }
}

enum class BottomTab { Home, Groups, Profile }

data class HomeUiState(
    val currentTab: BottomTab = BottomTab.Home,

    val isLoading: Boolean = false,
    val isFetchingGroups: Boolean = false,
    val isFetchingMessages: Boolean = false,
    val isSendingMessage: Boolean = false,
    val error: String? = null,
    val info: String? = null,

    // Create group
    val createGroupName: String = "",
    val createGroupDescription: String = "",
    val inviteCodeCreated: String? = null,

    // Join group
    val inviteCode: String = "",

    // Groups
    val groups: List<GroupSummary> = emptyList(),
    val openGroupId: String? = null, // selected group for chat view

    // Chat
    val messages: List<Message> = emptyList(),
    val messageInput: String = ""
)
