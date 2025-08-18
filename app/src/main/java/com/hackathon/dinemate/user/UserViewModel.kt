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
import com.hackathon.dinemate.config.AppConfig
import com.hackathon.dinemate.util.HttpUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = FirebaseStorage.getInstance()
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val gson = Gson()
    private var baseURL: String = AppConfig.BASE_URL

    private val _user = MutableStateFlow<User?>(null)
    @SuppressLint("RestrictedApi")
    val user: StateFlow<User?> = _user

    private val userPrefs = application.getSharedPreferences("UserProfileCache", Context.MODE_PRIVATE)
    private val authPrefs = application.getSharedPreferences("UserAuthPrefs", Context.MODE_PRIVATE)

    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(application, gso)

    }

    private fun clearUserPreferences() {
        userPrefs.edit().clear().apply()
        Log.d("UserViewModel", "Cleared user SharedPreferences cache.")
    }

    private fun saveUserToPreferences(@SuppressLint("RestrictedApi") user: User) {
        viewModelScope.launch {
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

    private suspend fun registerUserWithAPI(
        email: String,
        firebaseId: String,
        username: String,
        fullName: String
    ): Boolean {
        return try {
            val url = "${baseURL.trimEnd('/')}/api/v1/auth/register"

            val json = JSONObject().apply {
                put("email", email)
                put("firebase_id", firebaseId)
                put("username", username)
                put("full_name", fullName)
                put("preferences", JSONObject())
            }.toString()

            Log.d("UserViewModel", "Registering user with API: $json")

            val response = withContext(Dispatchers.IO) {
                HttpUtil.post(url, json)
            }

            Log.d("UserViewModel", "API Registration successful: ${response.statusCode}")
            true
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error registering user with API", e)
            false
        }
    }

    fun completeInitialUserInfo(userId: String, firstName: String, lastName: String, context: Context, navController: NavController){
        viewModelScope.launch {
            val userMap = hashMapOf(
                "userId" to userId,
                "firstName" to firstName,
                "lastName" to lastName
            )

            try {
                db.collection(USERS_COLLECTION).document(userId).set(userMap).await()

                val userEmail = auth.currentUser?.email
                if (userEmail == null) {
                    Log.e("UserViewModel", "User email is null, cannot register with API")
                    Toast.makeText(context, "Error: User email not found", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val username = userEmail.substringBefore("@")
                val fullName = "$firstName $lastName"

                val apiRegistrationSuccess = registerUserWithAPI(
                    email = userEmail,
                    firebaseId = userId,
                    username = username,
                    fullName = fullName
                )

                if (!apiRegistrationSuccess) {
                    Log.w("UserViewModel", "API registration failed, but continuing with local setup")
                    Toast.makeText(context, "Profile created locally, but server registration failed", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("UserViewModel", "User successfully registered with backend API")
                    Toast.makeText(context, "Profile created successfully!", Toast.LENGTH_SHORT).show()
                }

                authPrefs.edit().apply {
                    putString("userId", userId)
                    putString("firstName", firstName)
                    putString("lastName", lastName)
                    apply()
                }

                val newUser = gson.fromJson(gson.toJson(userMap), User::class.java)
                _user.value = newUser
                saveUserToPreferences(newUser)

                navController.navigate("questionnaire/$userId") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
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

    fun saveProfilePicture(context: Context, userId: String, imageUri: Uri, onComplete: (Boolean, String) -> Unit) {
        val storageRef = storage.reference.child("dinemate_users/$userId/profile/profile_pic.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
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
                auth.signOut()
                onComplete?.invoke()
            } else {
                onError?.invoke(task.exception ?: Exception("Unknown error during sign-out"))
            }
        }
    }

    companion object {
        private const val USERS_COLLECTION = "dinemate_users"
        private const val PREF_USER_JSON = "user_profile_json"
    }
}