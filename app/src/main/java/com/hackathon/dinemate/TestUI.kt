//package com.hackathon.dinemate
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.ExperimentalLayoutApi
//import androidx.compose.foundation.layout.FlowRow
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.navigationBarsPadding
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material.icons.filled.Restaurant
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Divider
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.FilterChip
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.SnackbarHost
//import androidx.compose.material3.SnackbarHostState
//import androidx.compose.material3.Switch
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalSoftwareKeyboardController
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.KeyboardCapitalization
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import kotlinx.coroutines.launch
//
///** Profile domain model */
//data class UserProfile(
//    val firstName: String = "",
//    val secondName: String = "",
//    val email: String = "",
//    val phone: String = "",
//    val dietaryPreference: DietaryPreference = DietaryPreference.None,
//    val favoriteCuisines: Set<String> = emptySet(),
//    val notifyOrders: Boolean = true,
//    val notifyPromos: Boolean = false
//)
//
//enum class DietaryPreference { None, Vegetarian, Vegan, NonVegetarian, Eggetarian }
//
//class ProfileActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            DiningAppTheme {
//                UserProfileRoute(
//                    initial = UserProfile(
//                        firstName = "Aarav",
//                        secondName = "Shah",
//                        email = "aarav@example.com",
//                        phone = "9876543210",
//                        dietaryPreference = DietaryPreference.Vegetarian,
//                        favoriteCuisines = setOf("Indian", "Italian"),
//                        notifyOrders = true,
//                        notifyPromos = true
//                    ),
//                    onSave = { updated ->
//                        // TODO: Persist to Firebase/Room and navigate back
//                        println("Saved profile: ${'$'}updated")
//                    },
//                    onLogout = { println("Logout clicked") }
//                )
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun UserProfileRoute(
//    initial: UserProfile,
//    onSave: (UserProfile) -> Unit,
//    onLogout: () -> Unit = {}
//) {
//    val snackbar = remember { SnackbarHostState() }
//    val scope = rememberCoroutineScope()
//
//    var form by rememberSaveable(stateSaver = UserProfileSaver) { mutableStateOf(initial) }
//    val isValid = remember(form) {
//        validateName(form.firstName) == null &&
//                validateName(form.secondName) == null &&
//                validateEmail(form.email) == null &&
//                validatePhone(form.phone) == null
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Column {
//                        Text("Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
//                        Text("Manage your dining identity", style = MaterialTheme.typography.bodySmall)
//                    }
//                },
//                navigationIcon = {
//                    Icon(Icons.Default.Restaurant, contentDescription = null)
//                }
//            )
//        },
//        snackbarHost = { SnackbarHost(snackbar) },
//        bottomBar = {
//            Card(
//                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
//            ) {
//                Column(Modifier.fillMaxWidth().padding(16.dp)) {
//                    Button(
//                        onClick = {
//                            onSave(form)
//                            scope.launch { snackbar.showSnackbar("Profile saved") }
//                        },
//                        enabled = isValid,
//                        modifier = Modifier.fillMaxWidth()
//                    ) { Text("Save changes") }
//                    TextButton(onClick = onLogout, modifier = Modifier.align(Alignment.CenterHorizontally)) {
//                        Text("Log out")
//                    }
//                }
//            }
//        }
//    ) { padding ->
//        UserProfileScreen(
//            modifier = Modifier
//                .padding(padding)
//                .navigationBarsPadding()
//                .fillMaxSize(),
//            value = form,
//            onChange = { form = it }
//        )
//    }
//}
//
//@OptIn(ExperimentalLayoutApi::class)
//@Composable
//fun UserProfileScreen(
//    modifier: Modifier = Modifier,
//    value: UserProfile,
//    onChange: (UserProfile) -> Unit
//) {
//    val keyboard = LocalSoftwareKeyboardController.current
//    val scroll = rememberScrollState()
//
//    val cuisines = remember {
//        listOf(
//            "Indian", "Chinese", "Italian", "Mexican", "Thai", "Japanese",
//            "Mediterranean", "American", "South Indian", "Punjabi", "Bengali"
//        )
//    }
//
//    Column(
//        modifier = modifier
//            .padding(horizontal = 20.dp)
//            .verticalScroll(scroll),
//        verticalArrangement = Arrangement.spacedBy(20.dp)
//    ) {
//        Spacer(Modifier.height(12.dp))
//        ProfileHeader(
//            firstName = value.firstName,
//            secondName = value.secondName,
//            onEdit = { /* Hook up image picker if needed */ }
//        )
//
//        Section(title = "Personal info") {
//            LabeledTextField(
//                label = "First name",
//                value = value.firstName,
//                onValueChange = { onChange(value.copy(firstName = sanitizeName(it))) },
//                error = validateName(value.firstName),
//                ime = ImeAction.Next
//            )
//            LabeledTextField(
//                label = "Second name",
//                value = value.secondName,
//                onValueChange = { onChange(value.copy(secondName = sanitizeName(it))) },
//                error = validateName(value.secondName),
//                ime = ImeAction.Next
//            )
//            LabeledTextField(
//                label = "Email",
//                value = value.email,
//                onValueChange = { onChange(value.copy(email = it.trim())) },
//                error = validateEmail(value.email),
//                keyboardType = KeyboardType.Email,
//                ime = ImeAction.Next
//            )
//            LabeledTextField(
//                label = "Phone",
//                value = value.phone,
//                onValueChange = { onChange(value.copy(phone = it.filter { c -> c.isDigit() }.take(15))) },
//                error = validatePhone(value.phone),
//                keyboardType = KeyboardType.Phone,
//                ime = ImeAction.Done,
//                onDone = { keyboard?.hide() }
//            )
//        }
//
//        Section(title = "Preferences") {
//            Text("Dietary preference", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
//            Spacer(Modifier.height(8.dp))
//            RadioRow(
//                selected = value.dietaryPreference,
//                options = listOf(
//                    DietaryPreference.None to "None",
//                    DietaryPreference.Vegetarian to "Vegetarian",
//                    DietaryPreference.Vegan to "Vegan",
//                    DietaryPreference.Eggetarian to "Eggetarian",
//                    DietaryPreference.NonVegetarian to "Non‑vegetarian"
//                ),
//                onSelect = { onChange(value.copy(dietaryPreference = it)) }
//            )
//
//            Spacer(Modifier.height(12.dp))
//            Text("Favourite cuisines", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
//            Spacer(Modifier.height(6.dp))
//            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                cuisines.forEach { c ->
//                    val selected = c in value.favoriteCuisines
//                    FilterChip(
//                        selected = selected,
//                        onClick = {
//                            val next = value.favoriteCuisines.toMutableSet().apply {
//                                if (selected) remove(c) else add(c)
//                            }
//                            onChange(value.copy(favoriteCuisines = next))
//                        },
//                        label = { Text(c) }
//                    )
//                }
//            }
//        }
//
//        Section(title = "Notifications") {
//            SwitchRow(
//                title = "Order updates",
//                subtitle = "Track confirmations and delivery status",
//                checked = value.notifyOrders,
//                onCheckedChange = { onChange(value.copy(notifyOrders = it)) }
//            )
//            SwitchRow(
//                title = "Offers & promos",
//                subtitle = "Personalised deals and dining tips",
//                checked = value.notifyPromos,
//                onCheckedChange = { onChange(value.copy(notifyPromos = it)) }
//            )
//        }
//
//        Spacer(Modifier.height(90.dp)) // bottom bar space
//    }
//}
//
//@Composable
//private fun Section(title: String, content: @Composable () -> Unit) {
//    Column(Modifier.fillMaxWidth()) {
//        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
//        Spacer(Modifier.height(8.dp))
//        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
//            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                content()
//            }
//        }
//    }
//}
//
//@Composable
//private fun LabeledTextField(
//    label: String,
//    value: String,
//    onValueChange: (String) -> Unit,
//    error: String? = null,
//    keyboardType: KeyboardType = KeyboardType.Text,
//    ime: ImeAction = ImeAction.Next,
//    onDone: (() -> Unit)? = null
//) {
//    OutlinedTextField(
//        value = value,
//        onValueChange = onValueChange,
//        label = { Text(label) },
//        isError = error != null,
//        supportingText = { error?.let { Text(it) } },
//        singleLine = true,
//        keyboardOptions = KeyboardOptions(
//            capitalization = if (keyboardType == KeyboardType.Text) KeyboardCapitalization.Words else KeyboardCapitalization.None,
//            keyboardType = keyboardType,
//            imeAction = ime
//        ),
//        keyboardActions = KeyboardActions(
//            onDone = { onDone?.invoke() }
//        ),
//        modifier = Modifier.fillMaxWidth()
//    )
//}
//
//@Composable
//private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
//    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//        Column(Modifier.weight(1f)) {
//            Text(title, style = MaterialTheme.typography.bodyLarge)
//            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
//        }
//        Switch(checked = checked, onCheckedChange = onCheckedChange)
//    }
//}
//
//@Composable
//private fun RadioRow(
//    selected: DietaryPreference,
//    options: List<Pair<DietaryPreference, String>>,
//    onSelect: (DietaryPreference) -> Unit
//) {
//    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
//        options.forEach { (value, label) ->
//            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
//                androidx.compose.material3.RadioButton(
//                    selected = selected == value,
//                    onClick = { onSelect(value) }
//                )
//                Text(label)
//            }
//        }
//    }
//}
//
//@Composable
//private fun ProfileHeader(firstName: String, secondName: String, onEdit: () -> Unit) {
//    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//        val initials = initialsForName(firstName, secondName)
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            Column(
//                modifier = Modifier
//                    .size(80.dp)
//                    .clip(CircleShape)
//                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                Text(
//                    initials,
//                    style = MaterialTheme.typography.headlineSmall,
//                    fontWeight = FontWeight.Bold,
//                    textAlign = TextAlign.Center,
//                    color = MaterialTheme.colorScheme.primary
//                )
//            }
//            TextButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Change") }
//        }
//        Column(Modifier.weight(1f)) {
//            Text("${'$'}firstName ${'$'}secondName", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
//            Text("Make dining yours", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
//        }
//    }
//}
//
///*********** Helpers & Validation ***********/
//private fun initialsForName(first: String, second: String): String {
//    val f = first.trim().firstOrNull()?.uppercase() ?: "?"
//    val s = second.trim().firstOrNull()?.uppercase() ?: ""
//    return "$f$s"
//}
//
//private fun validateName(value: String): String? {
//    val v = value.trim()
//    if (v.isEmpty()) return "Required"
//    val pattern = Regex("^[\u00C0-\u017Fa-zA-Z .'-]{1,40}$")
//    return if (!pattern.matches(v)) "Only letters, spaces, .' - (max 40)" else null
//}
//
//private fun validateEmail(value: String): String? {
//    if (value.isBlank()) return null // optional
//    // Simple RFC-like email pattern
//    val pattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
//    return if (!pattern.matches(value)) "Invalid email" else null
//}
//
//private fun validatePhone(value: String): String? {
//    if (value.isBlank()) return null // optional
//    val digits = value.filter { it.isDigit() }
//    return if (digits.length in 7..15) null else "Invalid phone"
//}
//
//private fun sanitizeName(input: String): String = input.replace(Regex("\\s+"), " ").trimStart()
//
///*********** Saver for UserProfile (so rememberSaveable works) ***********/
//private val UserProfileSaver = androidx.compose.runtime.saveable.Saver<UserProfile, List<Any>>(
//    save = {
//        listOf(
//            it.firstName, it.secondName, it.email, it.phone,
//            it.dietaryPreference.name, it.favoriteCuisines.toList(), it.notifyOrders, it.notifyPromos
//        )
//    },
//    restore = {
//        @Suppress("UNCHECKED_CAST")
//        UserProfile(
//            firstName = it[0] as String,
//            secondName = it[1] as String,
//            email = it[2] as String,
//            phone = it[3] as String,
//            dietaryPreference = DietaryPreference.valueOf(it[4] as String),
//            favoriteCuisines = (it[5] as List<String>).toSet(),
//            notifyOrders = it[6] as Boolean,
//            notifyPromos = it[7] as Boolean
//        )
//    }
//)
//
///**************** THEME & PREVIEW ****************/
//@Composable
//fun DiningAppTheme(content: @Composable () -> Unit) {
//    MaterialTheme(
//        colorScheme = androidx.compose.material3.lightColorScheme(),
//        typography = androidx.compose.material3.Typography(),
//        content = content
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//private fun ProfilePreview() {
//    DiningAppTheme {
//        UserProfileRoute(
//            initial = UserProfile(
//                firstName = "Aarav",
//                secondName = "Shah",
//                email = "aarav@example.com",
//                phone = "9876543210",
//                dietaryPreference = DietaryPreference.Vegetarian,
//                favoriteCuisines = setOf("Indian", "Italian"),
//                notifyOrders = true,
//                notifyPromos = false
//            ),
//            onSave = {},
//            onLogout = {}
//        )
//    }
//}
