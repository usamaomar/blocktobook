package com.example.domain.model.authModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable


@Serializable
data class CreateEmailModel(
    val email: String,
    val name: String,
    val uid: String
) : Principal
