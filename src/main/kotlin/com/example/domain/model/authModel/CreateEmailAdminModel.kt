package com.example.domain.model.authModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable


@Serializable
data class CreateEmailAdminModel(
    val email: String,
    val password: String,
    val userName: String
) : Principal
