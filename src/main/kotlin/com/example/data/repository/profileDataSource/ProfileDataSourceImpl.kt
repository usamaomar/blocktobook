package com.example.data.repository.profileDataSource

import com.example.data.repository.SubscriptionTypesDataSource.SubscriptionTypesDataSource
import com.example.data.repository.cityDataSource.ProfileDataSource
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.profileModel.CompanyInfoModel
import com.example.domain.model.profileModel.CreateCompanyInfoModel
import com.example.domain.model.profileModel.VerifiedCompanyInfoModel
import com.example.domain.model.subscriptionModel.Subscription
import com.example.domain.model.subscriptionTypesModel.SubscriptionTypeModel
import com.example.domain.model.transactionModel.TransactionModel
import com.example.domain.model.userModel.User
import com.example.util.SubscriptionType
import com.example.util.TopUpType
import com.example.util.TransactionType
import com.example.util.addOneMoth
import com.example.util.addOneYear
import com.example.util.addSixMoth
import com.example.util.toSafeDouble
import com.mongodb.client.model.Filters
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import com.mongodb.client.model.Updates.set
import org.koin.java.KoinJavaComponent

class ProfileDataSourceImpl(database: CoroutineDatabase) : ProfileDataSource {

    private val errorCode: Int = 12
    private val users = database.getCollection<User>()
    val subscriptionTypeModel: SubscriptionTypesDataSource by KoinJavaComponent.inject(
        SubscriptionTypesDataSource::class.java
    )
    private val subscriptionTypeDatabase = database.getCollection<SubscriptionTypeModel>()

    override suspend fun updateCompanyInfo(
        userId: String?,
        companyInfoModel: CompanyInfoModel
    ): ApiResponse<CompanyInfoModel?> {
        val filter = User::id eq userId
        val companyInfo = companyInfoModel.copy(createdAt = System.currentTimeMillis())
        val update = set("companyInfo", companyInfo)
        val updateResult = users.updateOne(filter = filter, update = update).wasAcknowledged()
        val user = users.findOne(filter)
        return if (updateResult) {
            ApiResponse(data = user?.companyInfo, succeeded = true, errorCode = errorCode)
        } else {
            ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Company not found"),
                errorCode = errorCode
            )
        }
    }


    override suspend fun updateUserCompanyInfo(
        userId: String?,
        companyInfoModel: CompanyInfoModel
    ): ApiResponse<String?> {
        val filter = User::id eq userId
        val companyInfo = companyInfoModel.copy(createdAt = System.currentTimeMillis())
        val update = set("companyInfo", companyInfo)
        val updateResult = users.updateOne(filter = filter, update = update).wasAcknowledged()
        val user = users.findOne(filter)
        return if (updateResult) {
            ApiResponse(data = "Success", succeeded = true, errorCode = errorCode)
        } else {
            ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Company not found"),
                errorCode = errorCode
            )
        }
    }

    override suspend fun updateSubscription(
        userId: String,
        subscription: Int
    ): ApiResponse<User?>? {
        var subscriptionObject: Subscription? = null
        if (subscription == SubscriptionType.Free.ordinal) {
            subscriptionObject = Subscription(
                0,//SubscriptionType.Free
                System.currentTimeMillis(),
                addOneMoth(),
                System.currentTimeMillis()
            )
        } else if (subscription == SubscriptionType.Monthly.ordinal) {
            subscriptionObject = Subscription(
                1,//SubscriptionType.Monthly,
                System.currentTimeMillis(),
                addOneMoth(),
                System.currentTimeMillis()
            )
        } else if (subscription == SubscriptionType.SixMonth.ordinal) {
            subscriptionObject = Subscription(
                2,//SubscriptionType.SixMonth,
                System.currentTimeMillis(),
                addSixMoth(),
                System.currentTimeMillis()
            )
        } else if (subscription == SubscriptionType.Yearly.ordinal) {
            subscriptionObject = Subscription(
                3,//SubscriptionType.Yearly,
                System.currentTimeMillis(),
                addOneYear(),
                System.currentTimeMillis()
            )
        }
        val subscriptionType = subscriptionTypeDatabase.findOne(Filters.eq("type", subscription))
        subscriptionTypeModel.addTransaction(
            TransactionModel(
                userId = userId,
                amount = subscriptionType?.price ?: 0.0,
                blockToBookFees = 0.0,
                chargerId = userId,
                topUpType = TopUpType.SUBSCRIPTION.ordinal,
                transactionType = TransactionType.PLUS.ordinal,
                createdDate = System.currentTimeMillis(),
                subscriptionType = subscription
            )
        )
        val filter = User::id eq userId
        val update = set("subscription", subscriptionObject)
        users.updateOne(filter = filter, update = update)
        return ApiResponse(data = users.findOne(filter = User::id eq userId), succeeded = true, errorCode = errorCode)
    }

    override suspend fun adminUserApprove(
        verifiedCompanyInfoModel: VerifiedCompanyInfoModel
    ): ApiResponse<CompanyInfoModel?> {
        val filter = User::id eq verifiedCompanyInfoModel.userId
        val userResult = users.findOne(filter)
        val updatedUser = userResult?.copy(
            companyInfo = userResult.companyInfo?.copy(isCompanyInfoVerified = verifiedCompanyInfoModel.isCompanyInfoVerified)
        )
        users.replaceOne(
            filter, updatedUser ?: return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Hotel Ticket not found"),
                errorCode = errorCode
            )
        )
        val user = users.findOne(filter = User::id eq verifiedCompanyInfoModel.userId)
        return ApiResponse(data = user?.companyInfo, succeeded = true, errorCode = errorCode)
    }
}