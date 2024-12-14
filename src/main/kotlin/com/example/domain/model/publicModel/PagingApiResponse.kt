package com.example.domain.model.publicModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class PagingApiResponse<T>(
    val succeeded: Boolean,
    val data: T?,
    val message: ArrayList<String> = ArrayList(),
    val currentPage: Int? = 0,
    val totalPages: Int? = 0,
    val totalCount: Int? = 0,
    val pageSize: Int? = 0,
    val errorCode: Int? = 0,
    val hasPreviousPage: Boolean? = false,
    val hasNextPage: Boolean? = false,
) : Principal
