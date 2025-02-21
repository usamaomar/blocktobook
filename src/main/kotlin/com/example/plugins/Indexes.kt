package com.example.plugins

import com.example.data.repository.airLinesDataSource.AirLineDataSource
import com.example.data.repository.purchaseDataSource.PurchaseDataSource
import com.example.data.repository.userDataSource.UserDataSource
import com.example.domain.model.airlinesTicketModel.AirlineTicketModel
import io.ktor.server.application.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent



fun Application.Indexes() {
    val airLinesTickets: AirLineDataSource by KoinJavaComponent.inject(AirLineDataSource::class.java)
    val purchaseModel: PurchaseDataSource by KoinJavaComponent.inject(PurchaseDataSource::class.java)

    CoroutineScope(Dispatchers.IO).launch {
        airLinesTickets.createIndex()
        purchaseModel.createIndex()
    }
}
