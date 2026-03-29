package com.unh.communityhelp.auth.signup.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class OnboardingStep { GENERAL_INFO, QNA }

class CompleteProfileViewModel : ViewModel() {
    private val db by lazy { Firebase.firestore }
    private val auth by lazy { Firebase.auth }
    //General Info State
    var fullName by mutableStateOf("")
    var username by mutableStateOf("")
    var phoneNumber by mutableStateOf("")

    //Expertise State
    val expertiseList = mutableStateListOf<String>()
    var isSubmitting by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    val availableExpertise = listOf(
        "Plumbing", "Electrician", "Carpentry", "Gardening",
        "Painting", "HVAC", "Roofing", "Automotive", "Tutoring"
    )

    // Navigation State
    var currentStep by mutableStateOf(OnboardingStep.GENERAL_INFO)

    fun moveToNextStep() {
        currentStep = OnboardingStep.QNA
    }

    fun toggleExpertise(skill: String) {
        if (expertiseList.contains(skill)) {
            expertiseList.remove(skill)
        } else {
            expertiseList.add(skill)
        }
    }

    fun submitProfile(onComplete: () -> Unit) {
        val user = auth.currentUser
        val uid = user?.uid

        if (uid == null) {
            errorMessage = "User not authenticated"
            return
        }

        // Prepare the data map
        val userProfile = hashMapOf(
            "uid" to uid,
            "fullName" to fullName,
            "username" to username,
            "phoneNumber" to phoneNumber,
            "expertiseList" to expertiseList.toList(),
            "createdAt" to com.google.firebase.Timestamp.now(),
            "points" to 0
        )

        viewModelScope.launch {
            isSubmitting = true
            try {
                // Set the document using the UID as the ID
                db.collection("users")
                    .document(uid)
                    .set(userProfile)
                    .await()

                isSubmitting = false
                onComplete()
            } catch (e: Exception) {
                isSubmitting = false
                errorMessage = e.message ?: "Failed to save profile"
            }
        }
    }
}