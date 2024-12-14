package com.example.data.repository.SubscriptionTypesDataSource

import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.subscriptionTypesModel.CreateSubscriptionTypeModel
import com.example.domain.model.subscriptionTypesModel.ResponsePostSubscriptionTypeModel
import com.example.domain.model.subscriptionTypesModel.ResponseSubscriptionTypeModel
import com.example.domain.model.subscriptionTypesModel.SubscriptionTypeModel
import com.example.domain.model.subscriptionTypesModel.UpdateSubscriptionTypeModel
import com.example.domain.model.transactionModel.TransactionModel

interface SubscriptionTypesDataSource {

    suspend fun getById(id: String): ApiResponse<SubscriptionTypeModel?>?
    suspend fun addTransaction(transactionModel: TransactionModel): ApiResponse<String?>?
    suspend fun post(createSubscriptionTypeModel: CreateSubscriptionTypeModel): ApiResponse<ResponsePostSubscriptionTypeModel?>?
    suspend fun put(updateSubscriptionTypeModel: UpdateSubscriptionTypeModel): ApiResponse<ResponsePostSubscriptionTypeModel?>?
    suspend fun getAll(userId: String,searchText: String, pageSize: Int, pageNumber: Int, xAppLanguageId: Int): PagingApiResponse<List<ResponseSubscriptionTypeModel>?>?
}