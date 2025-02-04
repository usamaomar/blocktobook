package com.example.domain.model.cityModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable


@Serializable
data class CreateSendGrid(
    val fromEmail : String,
    val toEmail: String,
    val text: String
) : Principal