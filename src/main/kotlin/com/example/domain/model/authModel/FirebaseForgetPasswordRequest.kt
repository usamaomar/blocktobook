package com.example.domain.model.authModel

import kotlinx.serialization.Serializable

@Serializable
data class FirebaseForgetPasswordRequest(
    val requestType: String,
    val email: String
)
