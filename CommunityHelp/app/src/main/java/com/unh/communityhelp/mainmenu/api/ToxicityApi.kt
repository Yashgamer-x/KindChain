package com.unh.communityhelp.mainmenu.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class ToxicityRequest(val title: String, val description: String)
data class ToxicityResponse(val status: String)

interface ToxicityApi {
    @POST("predict")
    suspend fun checkSpam(@Body request: ToxicityRequest): Response<ToxicityResponse>
}