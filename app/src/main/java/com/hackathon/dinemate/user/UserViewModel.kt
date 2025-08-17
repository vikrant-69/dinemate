package com.hackathon.dinemate.user

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.hackathon.dinemate.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = FirebaseStorage.getInstance()
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val gson = Gson() // Instance for JSON serialization

    // --- State Flows ---
    private val _user = MutableStateFlow<User?>(null) // Holds the current user data
    @SuppressLint("RestrictedApi")
    val user: StateFlow<User?> = _user

    // Preferences - Name clearly indicates source
    private val userPrefs = application.getSharedPreferences("UserProfileCache", Context.MODE_PRIVATE)
    private val authPrefs = application.getSharedPreferences("UserAuthPrefs", Context.MODE_PRIVATE) // Separate auth details

    private val googleSignInClient: GoogleSignInClient


    private val prefs = application.getSharedPreferences("SavedJobsCache", Context.MODE_PRIVATE)

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.client_id)) // ensure this string exists in `strings.xml`
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(application, gso)

    }


    /** Clears locally cached user data */
    private fun clearUserPreferences() {
        userPrefs.edit().clear().apply()
        Log.d("UserViewModel", "Cleared user SharedPreferences cache.")
    }

    private fun saveUserToPreferences(@SuppressLint("RestrictedApi") user: User) {
        viewModelScope.launch { // Keep it off the main thread
            try {
                val userJson = gson.toJson(user)
                userPrefs.edit().putString(PREF_USER_JSON, userJson).apply()
                Log.d("UserViewModel", "User saved to SharedPreferences cache.")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error serializing user for SharedPreferences", e)
            }
        }
    }


    fun updateUserProfile(userId: String, firstName: String, lastName: String, headline: String, location: String){
        val updates = hashMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName,
            "headline" to headline,
            "location" to location
        )

        db.collection("hireinn_users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Log.d("ProfileViewModel", "Profile updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("ProfileViewModel", "Error updating profile", e)
            }
    }



    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun completeInitialUserInfo(userId: String, firstName: String, lastName: String, context: Context, navController: NavController){
        viewModelScope.launch {
            val userMap = hashMapOf(
                "userId" to userId,
                "firstName" to firstName,
                "lastName" to lastName
            )

            try {
                // Use set without merge here to ensure a clean slate for a NEW user profile doc
                db.collection(USERS_COLLECTION).document(userId).set(userMap).await()

                // Save minimal auth details locally (optional, userId is key)
                authPrefs.edit().apply {
                    putString("userId", userId)
                    putString("firstName", firstName) // Maybe not needed if profile loads fast
                    putString("lastName", lastName)  // Maybe not needed
                    apply()
                }

                // Eagerly load/cache the newly created profile
                val newUser = gson.fromJson(gson.toJson(userMap), User::class.java) // Create User from map
                _user.value = newUser
                saveUserToPreferences(newUser) // Save the initial profile locally

                // Navigate
                navController.navigate("questionnaire/$userId") { // Ensure route matches NavHost
                    popUpTo(navController.graph.startDestinationId) { inclusive = true } // Clear back stack to prevent going back to sign-up
                }

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error completing initial user info", e)
                Toast.makeText(context, "Error saving details: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    fun getUserEmail(): String? {
        return FirebaseAuth.getInstance().currentUser?.email
    }

    // User Profile Operations (Firebase Storage)
    fun saveProfilePicture(context: Context, userId: String, imageUri: Uri, onComplete: (Boolean, String) -> Unit) {
        val storageRef = storage.reference.child("dinemate_users/$userId/profile/profile_pic.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Update user document in Firestore
                    db.collection("dinemate_users").document(userId)
                        .update("profilePic", uri.toString())
                        .addOnSuccessListener {
                            onComplete(true, "Cover updated successfully")
                        }
                        .addOnFailureListener { e ->
                            onComplete(false, "Failed to update profile")
                        }
                }
            }
            .addOnFailureListener { e ->
                onComplete(false, "Failed to upload image $e")
                Log.d("ProfilePic", e.toString())
            }
    }


    fun signOut(onComplete: (() -> Unit)? = null, onError: ((Exception) -> Unit)? = null) {
        googleSignInClient.signOut().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                auth.signOut() // Firebase sign-out
                onComplete?.invoke()
            } else {
                onError?.invoke(task.exception ?: Exception("Unknown error during sign-out"))
            }
        }
    }


    companion object {
        private const val USERS_COLLECTION = "dinemate_users"
        private const val PREF_USER_JSON = "user_profile_json" // Key for storing user JSON
    }


}