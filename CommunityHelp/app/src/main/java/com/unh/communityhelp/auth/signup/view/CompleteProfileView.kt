package com.unh.communityhelp.auth.signup.view

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unh.communityhelp.auth.scaffold.AuthScaffold
import com.unh.communityhelp.auth.signup.category.Expertise
import com.unh.communityhelp.auth.signup.viewmodel.CompleteProfileViewModel
import com.unh.communityhelp.auth.signup.viewmodel.OnboardingStep
import com.unh.communityhelp.ui.theme.CommunityHelpTheme

@Composable
fun CompleteProfileView(
    viewModel: CompleteProfileViewModel = viewModel(),
    onProfileComplete: () -> Unit
) {
    AuthScaffold(
        title = if (viewModel.currentStep == OnboardingStep.GENERAL_INFO) "Profile Setup" else "Expertise"
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (viewModel.currentStep) {
                OnboardingStep.GENERAL_INFO -> {
                    GeneralInfoPart(
                        viewModel = viewModel,
                        onNext = { viewModel.moveToNextStep() }
                    )
                }
                OnboardingStep.QNA -> {
                    ExpertiseQNAPart(
                        viewModel = viewModel,
                        onFinish = { viewModel.submitProfile(onProfileComplete) }
                    )
                }
            }
        }
    }
}

@Composable
fun GeneralInfoPart(viewModel: CompleteProfileViewModel, onNext: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Basic Information",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.fullName,
            onValueChange = { viewModel.fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.username,
            onValueChange = { viewModel.username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.phoneNumber,
            onValueChange = { viewModel.phoneNumber = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.username.isNotBlank() && viewModel.fullName.isNotBlank()
        ) {
            Text("Next: Select Expertise")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpertiseQNAPart(viewModel: CompleteProfileViewModel, onFinish: () -> Unit) {
    Column {
        Text(
            text = "What is your expertise?",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Select all that apply to you.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Suggestions FlowRow
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Expertise.entries.forEach { skill ->
                val isSelected = viewModel.expertiseList.contains(skill.name)
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.toggleExpertise(skill.name) },
                    label = { Text(skill.name) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Your Selection (${viewModel.expertiseList.size})",
            style = MaterialTheme.typography.titleMedium
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.expertiseList.forEach { skill ->
                InputChip(
                    selected = true,
                    onClick = { viewModel.toggleExpertise(skill) },
                    label = { Text(skill) },
                    trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.expertiseList.isNotEmpty() && !viewModel.isSubmitting
        ) {
            if (viewModel.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Finish Registration")
            }
        }
    }
}

// --- PREVIEWS ---

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun PreviewStep1() {
    CommunityHelpTheme {
        val vm = CompleteProfileViewModel()
        AuthScaffold(title = "Profile Setup") { p ->
            Box(Modifier.padding(p).padding(24.dp)) {
                GeneralInfoPart(vm, {})
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun PreviewStep2() {
    CommunityHelpTheme {
        val vm = CompleteProfileViewModel().apply {
            expertiseList.add("PLUMBING")
            currentStep = OnboardingStep.QNA
        }
        AuthScaffold(title = "Expertise") { p ->
            Box(Modifier.padding(p).padding(24.dp)) {
                ExpertiseQNAPart(vm, {})
            }
        }
    }
}