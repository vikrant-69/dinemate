package com.hackathon.dinemate.signin


import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.ui.text.font.FontVariation
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.hackathon.dinemate.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.log

class GoogleSignInUtils {

    companion object{
        fun performSignIn(context: Context,
                          scope: CoroutineScope,
                          launcher: ManagedActivityResultLauncher<Intent, ActivityResult>?,
                          login:(String) -> Unit)
        {
            val credentialManager = androidx.credentials.CredentialManager.create(context)
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(getCredentialOptions(context))
                .build()

            scope.launch {
                try {
                    val result = credentialManager.getCredential(context, request)
                    when(result.credential){

                        is CustomCredential ->{
                            if (result.credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                                val googleTokenId = googleIdTokenCredential.idToken
                                val authCredential = GoogleAuthProvider.getCredential(googleTokenId, null)

                                val user = Firebase.auth.signInWithCredential(authCredential).await().user

                                user?.let {
                                    if(it.isAnonymous.not()){
                                        login.invoke(it.uid)
                                    }
                                }
                            }
                        }
                        else ->{

                        }
                    }
                }catch (e: NoCredentialException){
                    launcher?.launch(getIntent())
                }catch (e: GetCredentialException){
                    println("Error Occured ${R.string.client_id}")
                    Log.e("UtilError", e.toString())

                    e.printStackTrace()
                }
            }

        }

        private fun getIntent(): Intent{
            return Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
            }
        }

        private fun getCredentialOptions(context: Context): GetGoogleIdOption {

            return GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .setServerClientId(context.getString(R.string.client_id))
                .build()
        }
    }
}