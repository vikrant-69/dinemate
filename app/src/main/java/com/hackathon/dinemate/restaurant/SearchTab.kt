package com.hackathon.dinemate.restaurant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
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
                viewModel.searchRestaurants()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Results Section
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
                    onRetry = { viewModel.searchRestaurants() },
                    onDismiss = { viewModel.clearError() }
                )
            }
            ui.restaurants.isNotEmpty() -> {
                RestaurantList(
                    restaurants = ui.restaurants,
                    onRestaurantClick = onRestaurantClick
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

            // Query Field (Required)
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

            // Location Field
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

            // Search Button
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

            // Location Status - Updated to show actual address
            if (hasCurrentLocation || locationText.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color(0xFF4CAF50),
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
                        color = androidx.compose.ui.graphics.Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}


// Keep the existing ErrorSection, RestaurantList, and RestaurantItem composables as they are...

@Composable
private fun ErrorSection(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
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
private fun RestaurantList(
    restaurants: List<Restaurant>,
    onRestaurantClick: (Restaurant) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(restaurants, key = { "${it.name}_${it.distance}" }) { restaurant ->
            RestaurantItem(
                restaurant = restaurant,
                onClick = { onRestaurantClick(restaurant) }
            )
        }
    }
}

@Composable
private fun RestaurantItem(
    restaurant: Restaurant,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.elevatedCardColors(containerColor = LightGrey)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = restaurant.image,
                contentDescription = "${restaurant.name} image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = restaurant.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurant.distance,
                        style = MaterialTheme.typography.bodySmall,
                        color = MediumGrey
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = androidx.compose.ui.graphics.Color(0xFFFFA000),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = restaurant.rating.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MediumGrey
                        )
                    }
                }
            }
        }
    }
}
