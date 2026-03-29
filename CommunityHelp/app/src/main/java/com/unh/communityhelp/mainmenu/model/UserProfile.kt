package com.unh.communityhelp.mainmenu.model

data class UserProfile(
    val fullName: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val expertiseList: List<String> = emptyList(),
    val points: Long = 0
)