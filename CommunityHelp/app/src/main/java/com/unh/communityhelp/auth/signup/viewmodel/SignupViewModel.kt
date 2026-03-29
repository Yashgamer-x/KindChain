package com.unh.communityhelp.auth.signup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SignupState {
    object Idle : SignupState()
    object Loading : SignupState()
    object Success : SignupState()
    data class Error(val message: String) : SignupState()
}

class SignupViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _signupState = MutableStateFlow<SignupState>(SignupState.Idle)
    val signupState: StateFlow<SignupState> = _signupState

    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _signupState.value = SignupState.Error("Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _signupState.value = SignupState.Loading

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _signupState.value = SignupState.Success
                    } else {
                        _signupState.value = SignupState.Error(
                            task.exception?.message ?: "Registration failed"
                        )
                    }
                }
        }
    }

    fun resetState() {
        _signupState.value = SignupState.Idle
    }
}