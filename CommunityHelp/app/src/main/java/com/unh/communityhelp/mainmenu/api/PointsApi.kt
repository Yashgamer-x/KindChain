package com.unh.communityhelp.mainmenu.api

import com.google.firebase.Timestamp
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class PointsApiRequest(
    @SerializedName("location") val location: String,
    @SerializedName("stars") val stars: Int,
    @SerializedName("task_description") val description: String,
    @SerializedName("task_title") val title: String,
    @SerializedName("timestamp") val timestamp: String
)

fun Timestamp.toIso8601String(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(this.toDate())
}
data class PointsResponse(val totalPoints: Long)

interface PointsApi {
    @POST("estimate-points")
    suspend fun calculatePoints(@Body request: PointsApiRequest): Response<PointsResponse>
}