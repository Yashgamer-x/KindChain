package com.unh.communityhelp.mainmenu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.unh.communityhelp.mainmenu.model.UserProfile
import com.unh.communityhelp.mainmenu.state.ProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val auth by lazy { Firebase.auth }
    private val db by lazy { Firebase.firestore }

    private val _uiState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val uiState: StateFlow<ProfileState> = _uiState

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    val profile = UserProfile(
                        fullName = doc.getString("fullName") ?: "",
                        username = doc.getString("username") ?: "",
                        phoneNumber = doc.getString("phoneNumber") ?: "",
                        expertiseList = doc.get("expertiseList") as? List<String> ?: emptyList(),
                        points = doc.getLong("points") ?: 0L
                    )
                    _uiState.value = ProfileState.Success(profile)
                }
            } catch (e: Exception) {
                _uiState.value = ProfileState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        auth.signOut()
        onLogoutComplete()
    }
}