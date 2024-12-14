package com.example.domain.model.authModel

import kotlinx.serialization.Serializable


@Serializable
data class FirebaseLoginResponse(
    val idToken: String,
    val localId: String,
    val refreshToken: String,
    val expiresIn: String
)
