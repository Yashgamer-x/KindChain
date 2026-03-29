package com.unh.communityhelp.mainmenu.model

data class Reward(
    val id: String = "",
    val businessName: String = "",
    val description: String = "",
    val pointsRequired: Int = 500,
    val category: String = "Service",
    val mapUrl: String = "",
    val address: String = "",
    val imageUrl: String = ""
)