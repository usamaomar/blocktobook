package com.example.data.repository.SubscriptionTypesDataSource

import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.subscriptionTypesModel.CreateSubscriptionTypeModel
import com.example.domain.model.subscriptionTypesModel.ResponsePostSubscriptionTypeModel
import com.example.domain.model.subscriptionTypesModel.ResponseSubscriptionTypeModel
import com.example.domain.model.subscriptionTypesModel.SubscriptionTypeModel
import com.example.domain.model.subscriptionTypesModel.SubscriptionTypeProfileModel
import com.example.domain.model.subscriptionTypesModel.UpdateSubscriptionTypeModel
import com.example.domain.model.subscriptionTypesModel.toResponsePostSubscriptionTypeModel
import com.example.domain.model.subscriptionTypesModel.toResponseSubscriptionTypeModel
import com.example.domain.model.subscriptionTypesModel.toSubscriptionTypeModel
import com.example.domain.model.transactionModel.TransactionModel
import com.example.domain.model.userModel.User
import com.example.util.AccessRole
import com.example.util.SubscriptionType
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.bson.BsonDocument
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.elemMatch
import org.litote.kmongo.eq
import org.litote.kmongo.ne
import org.litote.kmongo.regex

class SubscriptionTypesDataSourceImpl(database: CoroutineDatabase) : SubscriptionTypesDataSource {

    private val subscriptionTypeDatabase = database.getCollection<SubscriptionTypeModel>()
    private val transactionCollection = database.getCollection<TransactionModel>()
    private val users = database.getCollection<User>()

    private val errorCode: Int = 2009
    override suspend fun getById(
        id: String
    ): ApiResponse<SubscriptionTypeModel?> {
        val filter = Filters.eq("_id", ObjectId(id))
        return ApiResponse(
            data = subscriptionTypeDatabase.findOne(filter),
            succeeded = false,
            message = arrayListOf("Not Correct Subscription Type to update"),
            errorCode = errorCode
        )
    }

    override suspend fun post(createSubscriptionTypeModel: CreateSubscriptionTypeModel): ApiResponse<ResponsePostSubscriptionTypeModel?> {
        if (createSubscriptionTypeModel.type < 0 || createSubscriptionTypeModel.type > 3) {
            return ApiResponse(
                data = null,
                succeeded = false,
                errorCode = errorCode,
                message = arrayListOf("Subscription type ENUM ERROR")
            )
        }

        val subscriptionType =
            subscriptionTypeDatabase.findOne(Filters.eq("type", createSubscriptionTypeModel.type))
        if (subscriptionType != null) {
            return ApiResponse(
                data = null,
                succeeded = false,
                errorCode = errorCode,
                message = arrayListOf("Subscription type already exists")
            )
        }
        val local: SubscriptionTypeModel = createSubscriptionTypeModel.toSubscriptionTypeModel()
        val insertResult = subscriptionTypeDatabase.insertOne(document = local)
        val insertedId = insertResult.insertedId?.asObjectId()?.value?.toString()
        val insertedTicket =
            subscriptionTypeDatabase.findOne(Filters.eq("_id", ObjectId(insertedId)))
        val responseModel =
            insertedTicket?.toResponsePostSubscriptionTypeModel(insertedId.toString())
        return ApiResponse(data = responseModel, succeeded = true, errorCode = errorCode)
    }

    override suspend fun put(updateSubscriptionTypeModel: UpdateSubscriptionTypeModel): ApiResponse<ResponsePostSubscriptionTypeModel?>? {
        val filter = Filters.eq("_id", ObjectId(updateSubscriptionTypeModel.id))
        subscriptionTypeDatabase.findOne(filter)
            ?: return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Not Correct Subscription Type to update"),
                errorCode = errorCode
            )
        if (updateSubscriptionTypeModel.type < 0 || updateSubscriptionTypeModel.type > 3) {
            return ApiResponse(
                data = null,
                succeeded = false,
                errorCode = errorCode,
                message = arrayListOf("Subscription type ENUM ERROR")
            )
        }
        val update = Updates.combine(
            Updates.set("profiles", updateSubscriptionTypeModel.profiles),
            Updates.set("price", updateSubscriptionTypeModel.price),
            Updates.set("type", updateSubscriptionTypeModel.type)
        )
        val updateResult = subscriptionTypeDatabase.updateOne(filter, update)
        return if (updateResult.matchedCount > 0) {
            val updatedCity = subscriptionTypeDatabase.findOne(filter)
            ApiResponse(
                data = updatedCity?.toResponsePostSubscriptionTypeModel(
                    updateSubscriptionTypeModel.id
                ), succeeded = true, errorCode = errorCode
            )
        } else {
            ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("AirLineTicket not found"),
                errorCode = errorCode
            )
        }

    }

    override suspend fun addTransaction(transactionModel: TransactionModel): ApiResponse<String?>? {
        transactionCollection.insertOne(transactionModel)
        return ApiResponse(
            data = "Success",
            succeeded = true,
            message = arrayListOf("Not Correct Subscription Type to update"),
            errorCode = errorCode
        )
    }

    override suspend fun getAll(
        userId: String,
        searchText: String,
        pageSize: Int,
        pageNumber: Int,
        xAppLanguageId: Int
    ): PagingApiResponse<List<ResponseSubscriptionTypeModel>?> {
        val accessRole = users.findOne(User::id eq userId)?.accessRole?.ordinal
        var transaction: TransactionModel? = null
        if (accessRole != AccessRole.Admin.ordinal) {
            val query = and(
                TransactionModel::userId eq userId,
                TransactionModel::subscriptionType eq SubscriptionType.Free.ordinal
            )
         transaction = transactionCollection.findOne(query)
        }
        val skip = (pageNumber - 1) * pageSize
        val query = if (transaction == null) {
            and(
                SubscriptionTypeModel::profiles.elemMatch(
                    SubscriptionTypeProfileModel::name regex searchText
                ),
                SubscriptionTypeModel::profiles.elemMatch(
                    SubscriptionTypeProfileModel::languageId eq xAppLanguageId
                )
            )
        } else {
            and(
                SubscriptionTypeModel::profiles.elemMatch(
                    SubscriptionTypeProfileModel::name regex searchText
                ),
                SubscriptionTypeModel::profiles.elemMatch(
                    SubscriptionTypeProfileModel::languageId eq xAppLanguageId
                ),
                SubscriptionTypeModel::type ne SubscriptionType.Free.ordinal
            )
        }
        // Perform the query
        val totalCount = subscriptionTypeDatabase.countDocuments(query).toInt()
        val totalPages =
            if (totalCount % (if (pageSize == 0) 1 else pageSize) == 0) totalCount / (if (pageSize == 0) 1 else pageSize) else (totalCount / (if (pageSize == 0) 1 else pageSize)) + 1
        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages
        return PagingApiResponse(
            succeeded = true,
            data = subscriptionTypeDatabase.find(query)
                .skip(skip)
                .limit(pageSize)
                .toList().map { cityModel ->
                    cityModel.toResponseSubscriptionTypeModel(cityModel.id?.toHexString(), 1)
                },
            currentPage = pageNumber,
            totalPages = totalPages,
            totalCount = totalCount,
            hasPreviousPage = hasPreviousPage,
            hasNextPage = hasNextPage,
            errorCode = errorCode
        )
    }
}