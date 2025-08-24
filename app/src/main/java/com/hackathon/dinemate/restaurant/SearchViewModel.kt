package com.hackathon.dinemate.restaurant

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.dinemate.R
import com.hackathon.dinemate.config.AppConfig
import com.hackathon.dinemate.util.HttpUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Locale

class SearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    private var baseURL: String = AppConfig.BASE_URL

    fun initialize(baseUrl: String = AppConfig.BASE_URL) {
        baseURL = baseUrl
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun onLocationChange(location: String) {
        _uiState.value = _uiState.value.copy(locationText = location)
    }

    fun setCurrentLocation(lat: Double, lng: Double, address: String = "") {
        _uiState.value = _uiState.value.copy(
            currentLat = lat,
            currentLng = lng,
            currentAddress = address,
            hasCurrentLocation = true
        )
    }

    fun searchRestaurants(context: Context) {
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a search query")
            return
        }

        val locationText = _uiState.value.locationText.trim()
        val hasLocation = locationText.isNotEmpty()
        val hasCurrentLocation = _uiState.value.hasCurrentLocation

        if (!hasLocation && !hasCurrentLocation) {
            _uiState.value =
                _uiState.value.copy(error = "Please allow location access or enter a location")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val url = buildSearchUrl(context, query, locationText, hasCurrentLocation)
                Log.d("SearchVM", "Searching: $url")

                val response = withContext(Dispatchers.IO) {
                    HttpUtil.get(url)
                }

                val restaurants = parseRestaurantPreferences(response.body)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    restaurants = restaurants,
                    hasSearched = true
                )

            } catch (e: Exception) {
                Log.e("SearchVM", "Search failed", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to search restaurants: ${e.message}",
                    hasSearched = true
                )
            }
        }
    }

    private fun buildSearchUrl(
        context: Context,
        query: String,
        locationText: String,
        useCurrentLocation: Boolean
    ): String {
        val baseUrl = "${baseURL.trimEnd('/')}/api/v1/restaurants/search"

        return if (locationText.isNotEmpty()) {
            val coordinates: Pair<Double, Double>? =
                getLatLongFromAddress(context, locationText)
            "$baseUrl?query=$query&latitude=${coordinates?.first}&longitude=${coordinates?.second}"
        } else if (useCurrentLocation) {
            "$baseUrl?query=$query&latitude=${_uiState.value.currentLat}&longitude=${_uiState.value.currentLng}"
        } else {
            "$baseUrl?query=$query"
        }
    }

    private fun getLatLongFromAddress(context: Context, address: String): Pair<Double, Double>? {
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSearch() {
        _uiState.value = SearchUiState()
    }
}

data class SearchUiState(
    val query: String = "",
    val locationText: String = "",
    val currentLat: Double = 0.0,
    val currentLng: Double = 0.0,
    val currentAddress: String = "",
    val hasCurrentLocation: Boolean = false,
    val isLoading: Boolean = false,
    val restaurants: List<Restaurant> = emptyList(),
    val error: String? = null,
    val hasSearched: Boolean = false
)
