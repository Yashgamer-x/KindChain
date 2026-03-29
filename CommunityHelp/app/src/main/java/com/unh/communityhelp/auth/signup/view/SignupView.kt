package com.unh.communityhelp.auth.signup.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unh.communityhelp.auth.scaffold.AuthScaffold
import com.unh.communityhelp.auth.signup.viewmodel.SignupState
import com.unh.communityhelp.auth.signup.viewmodel.SignupViewModel
import com.unh.communityhelp.ui.theme.CommunityHelpTheme

@Composable
fun SignupView(
    modifier: Modifier = Modifier,
    viewModel: SignupViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    val signupState by viewModel.signupState.collectAsState()

    SignupContent(
        modifier = modifier,
        signupState = signupState,
        onSignUp = { email, password ->
            viewModel.signUp(email, password)
        },
        onNavigateToLogin = onNavigateToLogin,
        onSignUpSuccess = onSignUpSuccess
    )
}

@Composable
fun SignupContent(
    modifier: Modifier = Modifier,
    signupState: SignupState,
    onSignUp: (String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val passwordFocusRequester = remember { FocusRequester() }

    // Trigger navigation on success
    LaunchedEffect(signupState) {
        if (signupState is SignupState.Success) {
            onSignUpSuccess()
        }
    }

    AuthScaffold(title = "Join Community Help") { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Create your account", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))


            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = signupState !is SignupState.Loading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = signupState !is SignupState.Loading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
            )

            // Show Error Message if it exists
            if (signupState is SignupState.Error) {
                Text(
                    text = signupState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onSignUp(email, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = signupState !is SignupState.Loading &&
                        email.isNotBlank() && password.isNotBlank()
            ) {
                if (signupState is SignupState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Sign Up")
                }
            }

            TextButton(onClick = onNavigateToLogin, enabled = signupState !is SignupState.Loading) {
                Text("Already have an account? Log In")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignupPreview() {
    CommunityHelpTheme {
        SignupContent(
            signupState = SignupState.Idle,
            onSignUp = { _, _-> },
            onNavigateToLogin = {},
            onSignUpSuccess = {}
        )
    }
}