package com.example.domain.model.authModel

import kotlinx.serialization.Serializable

@Serializable
data class FirebaseLoginRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true
)
