package com.example.domain.model.userModel

import io.ktor.server.auth.Principal

data class UserSession(
    val id: String,
    val name : String
): Principal
