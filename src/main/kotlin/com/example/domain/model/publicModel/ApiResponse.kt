package com.example.domain.model.publicModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val succeeded: Boolean,
    val data: T?,
    val message: ArrayList<String> = ArrayList(),
    val code: Int? = 0,
    val errorCode: Int? = 0
): Principal
