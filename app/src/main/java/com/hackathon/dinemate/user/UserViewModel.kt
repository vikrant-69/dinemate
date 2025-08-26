package com.hackathon.dinemate.user

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import java.time.LocalTime
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.JsonObject
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.hackathon.dinemate.R
import com.hackathon.dinemate.config.AppConfig
import com.hackathon.dinemate.util.HttpUtil
import com.hackathon.dinemate.util.RequestType
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
    val user: StateFlow<User?> = _user

    private val userPrefs =
        application.getSharedPreferences("UserProfileCache", Context.MODE_PRIVATE)
    private val authPrefs = application.getSharedPreferences("UserAuthPrefs", Context.MODE_PRIVATE)

    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(application, gso)
        loadUserFromPreferences()
    }

    private fun loadUserFromPreferences() {
        val userJson = userPrefs.getString(PREF_USER_JSON, null)
        if (userJson != null) {
            try {
                val cachedUser = gson.fromJson(userJson, User::class.java)
                _user.value = cachedUser
                Log.d(
                    "UserViewModel",
                    "Loaded user from SharedPreferences cache: ${_user.value?.userId}"
                )
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error deserializing user from SharedPreferences", e)
            }
        }
    }

    // Add this new function to reload user data from Firestore
    fun reloadUserFromFirestore(userId: String) {
        viewModelScope.launch {
            try {
                val document = db.collection(USERS_COLLECTION).document(userId).get().await()
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        _user.value = it
                        saveUserToPreferences(it)
                        Log.d("UserViewModel", "Reloaded user from Firestore: ${it.userId}")
                    }
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error reloading user from Firestore", e)
            }
        }
    }

    // Add this function to load user after successful authentication
    fun loadUserAfterAuth() {
        val currentUser = auth.currentUser
        currentUser?.let { firebaseUser ->
            // First try to load from cache
            loadUserFromPreferences()

            // If cache is empty or user is still null, load from Firestore
            if (_user.value == null) {
                reloadUserFromFirestore(firebaseUser.uid)
            }
        }
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

    @SuppressLint("RestrictedApi")
    fun saveUserProfile(user: User, context: Context, navController: NavController) {
        viewModelScope.launch {
            try {
                db.collection(USERS_COLLECTION).document(user.userId)
                    .set(user, SetOptions.merge()).await()

                Log.d("USER INFO", user.toString())
                _user.value = user

                val resultPair = registerUserWithAPI(
                    email = user.userName +  "@gmail.com",
                    firebaseId = user.userId,
                    username = user.userName,
                    fullName = user.name
                )
                saveUserToPreferences(user) // Save to cache here

                Toast.makeText(context, "Signin Completed", Toast.LENGTH_SHORT).show()

                if (resultPair.second){
                    navController.navigate("questionnaire/${user.userId}")
                }else{
                    navController.navigate("homeScreen/${user.userId}")
                }
                Log.d("UserViewModel", "User profile saved to Firestore.")

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error saving user profile to Firestore", e)
                Toast.makeText(context, "Error in Google Signin. Check Later", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    suspend fun registerUserWithAPI(
        email: String,
        firebaseId: String,
        username: String,
        fullName: String
    ): Pair<Boolean, Boolean> {
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

            val gson = Gson()
            val jsonObject = gson.fromJson(response.body, JsonObject::class.java)

            val preferences = jsonObject.getAsJsonObject("preferences")
            val isPreferencesEmpty = preferences?.entrySet()?.isEmpty() ?: true
            Log.d("PREFERENCES", preferences.toString())
            if (isPreferencesEmpty) {
                // preferences is empty
                Pair(true, true)
            } else {
                // preferences is not empty
                Pair(true, false)
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error registering user with API", e)
            Pair(false, false)
        }
    }

    suspend fun userLogin(
        email: String,
        firebaseId: String
    ): Boolean{
        return try {
            val url = "${baseURL.trimEnd('/')}/api/v1/auth/login"

            val json = JSONObject().apply {
                put("email", email)
                put("firebase_id", firebaseId)
            }.toString()

            Log.d("UserViewModel", "Logging in user with API: $json")

            val response = withContext(Dispatchers.IO) {
                HttpUtil.post(url, json)
            }

            val gson = Gson()
            val jsonObject = gson.fromJson(response.body, JsonObject::class.java)

            val preferences = jsonObject.getAsJsonObject("preferences")
            val isPreferencesEmpty = preferences?.entrySet()?.isEmpty() ?: true
            Log.d("PREFERENCES", preferences.toString())

            // After successful API login, reload user data
            reloadUserFromFirestore(firebaseId)

            if (isPreferencesEmpty) {
                // preferences is empty
                true
            } else {
                // preferences is not empty
                false
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error logging in user with API", e)
            false
        }
    }

    fun getUserEmail(): String? {
        return FirebaseAuth.getInstance().currentUser?.email
    }

    fun saveProfilePicture(
        context: Context,
        userId: String,
        imageUri: Uri,
        onComplete: (Boolean, String) -> Unit
    ) {
        val storageRef = storage.reference.child("dinemate_users/$userId/profile/profile_pic.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    db.collection("dinemate_users").document(userId)
                        .update("profilePic", uri.toString())
                        .addOnSuccessListener {
                            // Update local user state as well
                            _user.value?.let { currentUser ->
                                val updatedUser = currentUser.copy(profilePic = uri.toString())
                                _user.value = updatedUser
                                saveUserToPreferences(updatedUser)
                            }
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
                clearUserPreferences()
                _user.value = null
                onComplete?.invoke()
                Log.d("UserViewModel", "User signed out successfully")
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