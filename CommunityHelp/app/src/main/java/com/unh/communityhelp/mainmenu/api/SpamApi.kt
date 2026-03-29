package com.unh.communityhelp.mainmenu.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class SpamRequest(val title: String, val description: String)
data class SpamResponse(val status: String)

interface SpamApi {
    @POST("predict")
    suspend fun checkSpam(@Body request: SpamRequest): Response<SpamResponse>
}