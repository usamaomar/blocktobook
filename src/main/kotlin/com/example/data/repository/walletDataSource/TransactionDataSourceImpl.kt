package com.example.data.repository.walletDataSource

import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.userModel.User
import com.example.domain.model.transactionModel.ResponseTransactionModel
import com.example.domain.model.transactionModel.TransactionModel
import com.example.domain.model.walletAmountModel.WalletAmountModel
import com.mongodb.client.model.Updates
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class TransactionDataSourceImpl(database: CoroutineDatabase) : TransactionDataSource {
    private val errorCode: Int = 266
    private val transactions = database.getCollection<TransactionModel>()
    private val walletAmount = database.getCollection<WalletAmountModel>()
    private val users = database.getCollection<User>()

    override suspend fun topUpWallet(
        userId: String,
        amount: Double,
        blockToBookFees: Double,
        chargerId: String,
        topUpType: Int,
        transactionType: Int,
    ): ApiResponse<String?> {
        val walletAmounts = walletAmount.findOne(filter = WalletAmountModel::userId eq userId)
        if (walletAmounts == null) {
            walletAmount.insertOne(
                document = WalletAmountModel(
                    userId = userId,
                    amount = amount + blockToBookFees
                )
            )
        } else {
            val filter = WalletAmountModel::userId eq userId
            val update = Updates.combine(
                Updates.set(
                    "amount", (amount + blockToBookFees) + walletAmounts.amount
                )
            )
            walletAmount.updateOne(
                filter,update
            )
        }
        val insertResult = transactions.insertOne(
            document = TransactionModel(
                userId = userId,
                amount = amount,
                blockToBookFees = blockToBookFees,
                chargerId = chargerId,
                topUpType = topUpType,
                transactionType = transactionType,
                createdDate = System.currentTimeMillis()
            )
        )
        val insertedId = insertResult.insertedId?.asObjectId()?.value?.toString()
        if (insertedId != null) {
            return ApiResponse(data = "Success", succeeded = true, errorCode = errorCode)
        } else {
            return ApiResponse(data = "Fail", succeeded = false, errorCode = errorCode)
        }
    }

    override suspend fun getWalletAmountByUserId(
        userId: String
    ): ApiResponse<String?> {
        val walletAmounts = walletAmount.findOne(filter = WalletAmountModel::userId eq userId)
        if (walletAmounts != null) {
            return ApiResponse(data = walletAmounts.amount.toString(), succeeded = true, errorCode = errorCode)
        } else {
            return ApiResponse(data = "0", succeeded = false, errorCode = errorCode)
        }
    }


    override suspend fun getAllWalletsByUserId(
        userId: String,  // This will be used as userId
        pageSize: Int,
        pageNumber: Int
    ): PagingApiResponse<List<ResponseTransactionModel>?>? {
        val skip = (pageNumber - 1) * pageSize

        // Update the query to filter by userId using searchText
        val query = TransactionModel::userId eq userId

        // Perform the query
        val totalCount = transactions.countDocuments(query).toInt()
        val totalPages =
            if (totalCount % (if (pageSize == 0) 1 else pageSize) == 0) totalCount / (if (pageSize == 0) 1 else pageSize)
            else (totalCount / (if (pageSize == 0) 1 else pageSize)) + 1

        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages

        return PagingApiResponse(
            succeeded = true,
            data = transactions.find(query)
                .skip(skip)
                .limit(pageSize)
                .toList().map { walletModel ->
                    ResponseTransactionModel(
                        id = walletModel.id?.toHexString() ?: "",
                        userId = walletModel.userId,
                        amount = walletModel.amount,
                        chargerName = users.findOne(filter = User::id eq walletModel.chargerId)?.name
                            ?: "",
                        blockToBookFees = walletModel.blockToBookFees,
                        chargerId = walletModel.chargerId,
                        topUpType = walletModel.topUpType,
                        transactionType = walletModel.transactionType,
                        createdDate = walletModel.createdDate
                    )
                },
            currentPage = pageNumber,
            totalPages = totalPages,
            totalCount = totalCount,
            hasPreviousPage = hasPreviousPage,
            hasNextPage = hasNextPage,
            errorCode = null
        )
    }

}
