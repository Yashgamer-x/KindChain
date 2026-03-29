package com.unh.communityhelp.mainmenu.model

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude

data class HelpRequest(
    // 1. Move selfRef to the constructor so .copy() works properly
    @get:Exclude val id: String = "",
    @get:Exclude val selfRef: DocumentReference? = null,

    val authorId: String = "",
    val authorName: String = "Loading...",
    val helperId: String? = null,
    val title: String = "",
    val description: String = "",
    val image: String = "",
    val location: String = "",
    val category: String = "",
    val status: String = "open",
    val createdAt: Timestamp? = null
)

fun HelpRequest.decodeImage(): ImageBitmap? {
    if (image.isBlank()) return null
    return try {
        val decodedBytes = Base64.decode(image, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}