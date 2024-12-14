package com.example.data.repository.paymentDataSource


import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.transactionModel.TransactionModel
import com.example.domain.model.walletAmountModel.WalletAmountModel
import com.example.util.TopUpType
import com.example.util.TransactionType
import com.example.util.toDoubleAmount
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class PaymentDataSourceImpl(database: CoroutineDatabase) : PaymentDataSource {

    private val walletCollection = database.getCollection<WalletAmountModel>()
    private val transactionCollection = database.getCollection<TransactionModel>()

    private val errorCode: Int = 2642

    override suspend fun createCheckout(userId: String,amountToCharge: Double,blockToBookFees: Double): ApiResponse<Boolean> {
       try {
           val adminWalletAmount = walletCollection.findOne(WalletAmountModel::userId eq userId)
           if(adminWalletAmount == null){
               walletCollection.insertOne(
                   WalletAmountModel(
                       userId = userId,
                       amount = amountToCharge.toDoubleAmount()
                   ))
           }else{
               val totalAmountToCharge = adminWalletAmount.amount.plus(amountToCharge)
               walletCollection.updateOne(
                   WalletAmountModel::userId eq userId,
                   setValue(WalletAmountModel::amount, totalAmountToCharge.toDoubleAmount()
                   ))
           }
           transactionCollection.insertOne(
               TransactionModel(
                   userId = userId,
                   amount = amountToCharge.toDoubleAmount(),
                   blockToBookFees = blockToBookFees,
                   chargerId = userId,
                   topUpType = TopUpType.VISA_FEES.ordinal,
                   transactionType = TransactionType.PLUS.ordinal,
                   createdDate = System.currentTimeMillis()
               )
           )
           return ApiResponse(
               data = true,
               succeeded = true,
               message = arrayListOf("transportation not found"),
               errorCode = errorCode
           )
       }catch (ex:Exception){
           return ApiResponse(
               data = false,
               succeeded = true,
               message = arrayListOf("transportation not found"),
               errorCode = errorCode
           )
       }
    }


}