package com.example.data.repository.adminWalletDataSource

import com.example.domain.model.adminWalletAmount.AdminWalletAmount
import com.example.domain.model.adminWalletAmount.CreateAdminWalletAmount
import com.example.domain.model.adminWalletAmount.toAdminWalletAmount
import com.example.domain.model.publicModel.ApiResponse
import org.litote.kmongo.coroutine.CoroutineDatabase

class AdminWalletDataSourceImpl(database: CoroutineDatabase) : AdminWalletDataSource {
    private val errorCode: Int = 2662
    private val adminWalletAmount = database.getCollection<AdminWalletAmount>()

    override suspend fun addPurchaseToAdminWallet(createAdminWalletAmount: CreateAdminWalletAmount): ApiResponse<String?>? {
        val insertResult = adminWalletAmount.insertOne(document = createAdminWalletAmount.toAdminWalletAmount())
        val insertedId = insertResult.insertedId?.asObjectId()?.value?.toString()
       return if (insertedId  != null) {
            ApiResponse(
                data = null,
                succeeded = true,
                message = arrayListOf("Success"), errorCode = errorCode
            )
        } else {
            ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Error"), errorCode = errorCode
            )
        }
    }
}