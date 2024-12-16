package com.example.data.repository.authDataSource

import com.example.domain.model.authModel.CreateEmailAdminModel
import com.example.domain.model.authModel.CreateEmailModel
import com.example.domain.model.authModel.CreateRefreshTokenModel

interface AuthDataSource {
    suspend fun googleLoginMerchant(tokenId: String?): Map<String, Any>?
    suspend fun loginByEmailMerchant(createEmailModel: CreateEmailModel): Map<String, Any>?
    suspend fun loginByToken(createEmailModel: CreateEmailModel): Map<String, Any>?
    suspend fun createEmailMerchant(createEmailModel: CreateEmailModel): Map<String, Any>?
    suspend fun createEmailAdmin(createEmailModel: CreateEmailAdminModel): Map<String, Any>?
    suspend fun refresh(tokenModel: CreateRefreshTokenModel?): Map<String, Any>?

    suspend fun getTest(): String?


}
