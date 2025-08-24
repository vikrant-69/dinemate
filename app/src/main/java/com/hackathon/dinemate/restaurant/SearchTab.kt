package com.hackathon.dinemate.restaurant

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hackathon.dinemate.R
import com.hackathon.dinemate.config.AppConfig
import com.hackathon.dinemate.ui.theme.Black
import com.hackathon.dinemate.ui.theme.Charcoal
import com.hackathon.dinemate.ui.theme.LightGrey
import com.hackathon.dinemate.ui.theme.MediumGrey
import com.hackathon.dinemate.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTab(
    padding: PaddingValues,
    baseURL: String = AppConfig.BASE_URL,
    viewModel: SearchViewModel = viewModel(),
    onRestaurantClick: (Restaurant) -> Unit = {}
) {
    val ui by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    LaunchedEffect(baseURL) {
        viewModel.initialize(baseURL)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        // Search Form
        SearchForm(
            query = ui.query,
            locationText = ui.locationText,
            hasCurrentLocation = ui.hasCurrentLocation,
            isLoading = ui.isLoading,
            onQueryChange = viewModel::onQueryChange,
            onLocationChange = viewModel::onLocationChange,
            onSearch = {
                keyboardController?.hide()
                viewModel.searchRestaurants(context)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            ui.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Searching restaurants...", color = MediumGrey)
                    }
                }
            }

            ui.error != null -> {
                ErrorSection(
                    error = ui.error!!,
                    onRetry = { viewModel.searchRestaurants(context) },
                    onDismiss = { viewModel.clearError() }
                )
            }

            ui.restaurants.isNotEmpty() -> {
                RestaurantRecommendations(
                    restaurants = ui.restaurants,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            ui.hasSearched -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No restaurants found for \"${ui.query}\"",
                        color = MediumGrey,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🍽️",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Search for restaurants near you",
                            color = MediumGrey,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchForm(
    query: String,
    locationText: String,
    hasCurrentLocation: Boolean,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onSearch: () -> Unit,
    viewModel: SearchViewModel = viewModel(),
) {
    val ui by viewModel.uiState.collectAsState()

    Card(
        colors = CardDefaults.cardColors(containerColor = LightGrey),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Find Restaurants",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Charcoal
            )

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search Query *") },
                placeholder = { Text("Restaurant name, cuisine, etc.") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { onSearch() }
                ),
                enabled = !isLoading,
                supportingText = {
                    Text(
                        text = "Search by restaurant name, cuisine type, or food category",
                        style = MaterialTheme.typography.bodySmall,
                        color = MediumGrey
                    )
                }
            )

            OutlinedTextField(
                value = locationText,
                onValueChange = onLocationChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Location (optional)") },
                placeholder = {
                    Text(
                        if (hasCurrentLocation && ui.currentAddress.isNotEmpty())
                            ui.currentAddress
                        else if (hasCurrentLocation)
                            "Using current location"
                        else
                            "Enter city or address"
                    )
                },
                leadingIcon = {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Location")
                },
                singleLine = true,
                enabled = !isLoading
            )

            Button(
                onClick = onSearch,
                enabled = query.isNotBlank() && !isLoading && (hasCurrentLocation || locationText.isNotBlank()),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Charcoal,
                    contentColor = White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Searching...")
                } else {
                    Icon(Icons.Filled.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search Restaurants")
                }
            }

            if (hasCurrentLocation || locationText.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (locationText.isNotEmpty()) {
                            "Location: $locationText"
                        } else if (ui.currentAddress.isNotEmpty()) {
                            "Location: ${ui.currentAddress}"
                        } else {
                            "Using current location"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}


@Composable
private fun ErrorSection(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text("Dismiss")
            }
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Charcoal,
                    contentColor = White
                )
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun RestaurantRecommendations(
    restaurants: List<Restaurant>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = LightGrey
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Top Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    restaurants.forEach { restaurant ->
                        RestaurantRecommendationItem(restaurant = restaurant)
                        if (restaurant != restaurants.last()) {
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MediumGrey.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantRecommendationItem(restaurant: Restaurant) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Show in webview or open maps if needed */ }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = restaurant.image),
            contentDescription = "Rating star",
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Charcoal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            restaurant.description.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_star),
                    contentDescription = "Rating star",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${restaurant.rating}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Text(
                    text = " • ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = restaurant.distance,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}