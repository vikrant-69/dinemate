package com.hackathon.dinemate.restaurant

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

    fun searchRestaurants() {
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a search query")
            return
        }

        val locationText = _uiState.value.locationText.trim()
        val hasLocation = locationText.isNotEmpty()
        val hasCurrentLocation = _uiState.value.hasCurrentLocation

        if (!hasLocation && !hasCurrentLocation) {
            _uiState.value = _uiState.value.copy(error = "Please allow location access or enter a location")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val url = buildSearchUrl(query, locationText, hasCurrentLocation)
                Log.d("SearchVM", "Searching: $url")

                val response = withContext(Dispatchers.IO) {
                    HttpUtil.get(url)
                }

                val restaurants = parseRestaurantResponse(response.body)
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

    private fun buildSearchUrl(query: String, locationText: String, useCurrentLocation: Boolean): String {
        val baseUrl = "${baseURL.trimEnd('/')}/api/v1/restaurants/search"

        return if (locationText.isNotEmpty()) {
            "$baseUrl?query=$query&location=$locationText"
        } else if (useCurrentLocation) {
            "$baseUrl?query=$query&lat=${_uiState.value.currentLat}&lng=${_uiState.value.currentLng}"
        } else {
            "$baseUrl?query=$query"
        }
    }

    private fun parseRestaurantResponse(responseBody: String): List<Restaurant> {
        return try {
            val jsonArray = JSONArray(responseBody)
            val restaurants = mutableListOf<Restaurant>()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                restaurants.add(
                    Restaurant(
                        name = item.optString("name", "Unknown"),
                        distance = item.optString("distance", "N/A"),
                        description = item.optString("description", "No description"),
                        image = item.optString("image", ""),
                        rating = item.optDouble("rating", 0.0)
                    )
                )
            }
            restaurants
        } catch (e: Exception) {
            Log.e("SearchVM", "Failed to parse restaurant response", e)
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
