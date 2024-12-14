package com.example.domain.model.authModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable


@Serializable
data class CreateTokenModel(
    val tokenId: String?
) : Principal
