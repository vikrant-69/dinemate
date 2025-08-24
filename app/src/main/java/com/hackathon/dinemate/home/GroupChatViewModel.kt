package com.hackathon.dinemate.home

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.dinemate.restaurant.Restaurant
import com.hackathon.dinemate.util.HttpUtil
import com.hackathon.dinemate.util.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import com.hackathon.dinemate.R

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

    fun showPreferenceDialog() {
        _uiState.value = _uiState.value.copy(showPreferenceDialog = true)
    }

    fun hidePreferenceDialog() {
        _uiState.value = _uiState.value.copy(showPreferenceDialog = false)
    }

    fun onPreferenceLocationChange(location: String) {
        _uiState.value = _uiState.value.copy(preferenceLocation = location)
    }

    fun fetchRestaurantPreferences(context: Context) {
        val location = _uiState.value.preferenceLocation.trim()

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoadingPreferences = true, error = null)

                val url = if (location.isNotEmpty()) {
                    val coordinates: Pair<Double, Double>? =
                        getLatLongFromAddress(context, location)
                    if (coordinates != null) {
                        "${baseURL.trimEnd('/')}/api/v1/restaurants/groups/$groupId/recommendations?" +
                                "latitude=${coordinates.first}&longitude=${coordinates.second}"
                    } else {
                        throw Exception("Unable to get current location")
                    }
                } else {
                    val currentLocation = LocationHelper.getCurrentLocation(context)
                    if (currentLocation != null) {
                        "${baseURL.trimEnd('/')}/api/v1/restaurants/groups/$groupId/recommendations?" +
                                "latitude=${currentLocation.latitude}&longitude=${currentLocation.longitude}"
                    } else {
                        throw Exception("Unable to get current location")
                    }
                }

                Log.d("GroupChatVM", "Fetching preferences: $url")

                val response = withContext(Dispatchers.IO) {
                    HttpUtil.get(url)
                }

                val restaurants = parseRestaurantPreferences(response.body)
                _uiState.value = _uiState.value.copy(
                    isLoadingPreferences = false,
                    restaurantPreferences = restaurants,
                    showPreferenceDialog = false,
                    preferenceLocation = ""
                )

            } catch (e: Exception) {
                Log.e("GroupChatVM", "Failed to fetch restaurant preferences", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingPreferences = false,
                    error = "Failed to fetch restaurant preferences: ${e.message}"
                )
            }
        }
    }

    private fun parseRestaurantPreferences(responseBody: String): List<Restaurant> {
        return try {
            val json = JSONObject(responseBody)
            val jsonArray = json.optJSONArray("results") ?: JSONArray()
            val restaurants = mutableListOf<Restaurant>()

            for (i in 0 until jsonArray.length()) {
                if (i > 4) {
                    break
                }
                val item = jsonArray.getJSONObject(i)
                val location = item.optJSONObject("location")
                val categoriesArray = item.optJSONArray("categories")
                val firstCategory = categoriesArray?.optJSONObject(0)
                val icon = firstCategory?.optJSONObject("icon")

                val formattedDistance =
                    item.optInt("distance", -1).let { if (it != -1) "$it m" else "" }
                val address = location?.optString("formatted_address") ?: ""
                val name = item.optString("name", "")
                val desc = firstCategory?.optString("name") ?: ""
                val rating = 0.0
                val website = item.optString("website", "")
                val phone = item.optString("tel", "")
                val image = when (i) {
                    0 -> R.drawable.restaurant_1
                    1 -> R.drawable.restaurant_2
                    2 -> R.drawable.restaurant_3
                    3 -> R.drawable.restaurant_4
                    4 -> R.drawable.restaurant_5
                    else -> R.drawable.restaurant_1
                }
                restaurants.add(
                    Restaurant(
                        name = name,
                        distance = formattedDistance,
                        description = if (desc.isNotBlank()) desc else address,
                        image = image,
                        rating = rating
                    )
                )
            }
            restaurants
        } catch (e: Exception) {
            Log.e("GroupChatVM", "Failed to parse recommendations", e)
            emptyList()
        }
    }


    fun getLatLongFromAddress(context: Context, address: String): Pair<Double, Double>? {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addressList: List<Address>? = geocoder.getFromLocationName(address, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val location = addressList[0]
                val latitude = location.latitude
                val longitude = location.longitude
                return Pair(latitude, longitude)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun fetchMessages() {
        if (groupId.isEmpty()) return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val url =
                    "${baseURL.trimEnd('/')}/api/v1/group/${groupId}/messages?firebase_id=${userId}"

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
                _uiState.value =
                    _uiState.value.copy(isLoading = false, error = "Failed to fetch messages")
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

                _uiState.value = _uiState.value.copy(input = "", isSending = false)
                fetchMessages()
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(isSending = false, error = "Failed to send message")
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
    val error: String? = null,
    val showPreferenceDialog: Boolean = false,
    val preferenceLocation: String = "",
    val isLoadingPreferences: Boolean = false,
    val restaurantPreferences: List<Restaurant> = emptyList()
)
