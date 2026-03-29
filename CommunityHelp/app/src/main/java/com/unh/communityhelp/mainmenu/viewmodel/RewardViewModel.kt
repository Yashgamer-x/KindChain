package com.unh.communityhelp.mainmenu.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.unh.communityhelp.mainmenu.model.Reward
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RewardsViewModel: ViewModel() {
    private val auth by lazy { Firebase.auth }
    private val db by lazy { Firebase.firestore }

    var userPoints by mutableIntStateOf(0)
    var availableRewards by mutableStateOf<List<Reward>>(emptyList())
    var isLoading by mutableStateOf(false)

    fun fetchRewardsData() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                // Get User Points
                val userDoc = db.collection("users")
                    .document(uid)
                    .get()
                    .await()
                userPoints = userDoc.getLong("points")?.toInt() ?: 0

                // Get Rewards from Firestore
                val rewardDocs = db.collection("partners")
                    .orderBy("pointsRequired")
                    .get().await()

                availableRewards = rewardDocs.toObjects(Reward::class.java)
            } catch (e: Exception) {
                Log.e("RewardsVM", "Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun redeemReward(reward: Reward, onComplete: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                db.collection("users").document(uid)
                    .update("points", FieldValue.increment(-reward.pointsRequired.toLong()))
                    .await()

                fetchRewardsData() // Refresh points balance
                onComplete()
            } catch (e: Exception) {
                Log.e("RewardsVM", "Redeem failed: ${e.message}")
            }
        }
    }
}