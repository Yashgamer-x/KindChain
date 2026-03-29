package com.unh.communityhelp.mainmenu.state

import com.unh.communityhelp.mainmenu.model.UserProfile

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val profile: UserProfile) : ProfileState()
    data class Error(val message: String) : ProfileState()
}