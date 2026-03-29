package com.unh.communityhelp.mainmenu.viewmodel

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.unh.communityhelp.auth.signup.category.Expertise
import com.unh.communityhelp.mainmenu.api.SpamApi
import com.unh.communityhelp.mainmenu.api.SpamRequest
import com.unh.communityhelp.mainmenu.api.ToxicityApi
import com.unh.communityhelp.mainmenu.api.ToxicityRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class CreateHelpViewModel : ViewModel() {
    // Form States
    var capturedImage by mutableStateOf<Bitmap?>(null)
    var locationAddress by mutableStateOf("")
    var cityName by mutableStateOf("")
    var selectedCategory by mutableStateOf(Expertise.ELDERLY)

    // Status States
    var isFetchingLocation by mutableStateOf(false)
    var isSubmitting by mutableStateOf(false)
    var statusMessage by mutableStateOf("")

    private val spamApi = Retrofit.Builder()
        .baseUrl("https://spam-detection-api-lyart.vercel.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SpamApi::class.java)

    private val toxicityApi = Retrofit.Builder()
        .baseUrl("https://toxicity-api-project.vercel.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ToxicityApi::class.java)

    fun createHelpRequest(
        image: Bitmap?,
        title: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isSubmitting = true
            statusMessage = "Analyzing content..."

            try {
                val response = spamApi.checkSpam(SpamRequest(title, description))

                if (response.isSuccessful && response.body()?.status?.lowercase() == "good") {
                    statusMessage = "Checking safety!! PLEASE WAIT...."

                    val toxicityResponse = toxicityApi.checkSpam(ToxicityRequest(title, description))

                    if(toxicityResponse.isSuccessful && toxicityResponse.body()?.status?.lowercase() == "good"){
                        statusMessage = "Posting to Community..."
                        saveToFirestore(image, title, description, onSuccess)
                    } else {
                        statusMessage = "Post rejected: Content flagged as toxic."
                        isSubmitting = false
                    }

                } else {
                    statusMessage = "Post rejected: Content flagged as spam."
                    isSubmitting = false
                }
            } catch (e: Exception) {
                statusMessage = "Connection error. Please try again."
                isSubmitting = false
            }
        }
    }

    private suspend fun saveToFirestore(
        bitmap: Bitmap?,
        title: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        val db = Firebase.firestore
        val userId = Firebase.auth.currentUser?.uid ?: return // Safety check: need a real UID

        val imageString = bitmap?.let {
            val outputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        } ?: ""

        val requestData = hashMapOf(
            "authorId" to userId,
            "title" to title,
            "description" to description,
            "location" to locationAddress,
            "image" to imageString,
            "category" to selectedCategory.name,
            "status" to "open",
            "createdAt" to FieldValue.serverTimestamp()
        )

        try {
            // Create the Help Request document and get its reference
            val newTaskRef = db.collection("geolocation")
                .document(cityName)
                .collection("categories")
                .document(selectedCategory.name)
                .collection("help_requests")
                .add(requestData)
                .await() // .add() returns a DocumentReference after completion

            // Add this specific reference to the user's 'createdTasks' array
            db.collection("users")
                .document(userId)
                .update("createdTasks", FieldValue.arrayUnion(newTaskRef))
                .await()

            isSubmitting = false
            statusMessage = "Post successful!"
            onSuccess()
        } catch (e: Exception) {
            statusMessage = "Firestore Error: ${e.message}"
            isSubmitting = false
        }
    }

    fun resetForm() {
        capturedImage = null
        statusMessage = ""
    }

    fun clearImage() {
        capturedImage = null
    }
}