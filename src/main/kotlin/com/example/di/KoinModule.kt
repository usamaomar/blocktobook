package com.example.di

import com.example.data.repository.SubscriptionTypesDataSource.SubscriptionTypesDataSource
import com.example.data.repository.SubscriptionTypesDataSource.SubscriptionTypesDataSourceImpl
import com.example.data.repository.adminWalletDataSource.AdminWalletDataSource
import com.example.data.repository.adminWalletDataSource.AdminWalletDataSourceImpl
import com.example.data.repository.airLinesDataSource.AirLineDataSource
import com.example.data.repository.airLinesDataSource.AirLineDataSourceImpl
import com.example.data.repository.airLinesTicketsDataSource.AirLineTicketDataSource
import com.example.data.repository.airLinesTicketsDataSource.AirLineTicketDataSourceImpl
import com.example.data.repository.airPortsDataSource.AirPortDataSource
import com.example.data.repository.airPortsDataSource.AirPortsDataSourceImpl
import com.example.data.repository.authDataSource.AuthDataSource
import com.example.data.repository.authDataSource.AuthDataSourceImpl
import com.example.data.repository.cartDataSource.CartDataSource
import com.example.data.repository.cartDataSource.CartDataSourceImpl
import com.example.data.repository.cityDataSource.CityDataSource
import com.example.data.repository.cityDataSource.CityDataSourceImpl
import com.example.data.repository.cityDataSource.ProfileDataSource
import com.example.data.repository.hotelDataSource.HotelDataSource
import com.example.data.repository.hotelDataSource.HotelDataSourceImpl
import com.example.data.repository.hotelTicketsDataSource.HotelTicketDataSource
import com.example.data.repository.hotelTicketsDataSource.HotelTicketDataSourceImpl
import com.example.data.repository.paymentDataSource.PaymentDataSource
import com.example.data.repository.paymentDataSource.PaymentDataSourceImpl
import com.example.data.repository.profileDataSource.ProfileDataSourceImpl
import com.example.data.repository.purchaseDataSource.PurchaseDataSource
import com.example.data.repository.purchaseDataSource.PurchaseDataSourceImpl
import com.example.data.repository.searchDataSource.SearchDataSource
import com.example.data.repository.searchDataSource.SearchDataSourceImpl
import com.example.data.repository.sendGrid.SendGridDataSource
import com.example.data.repository.sendGrid.SendGridDataSourceImpl
import com.example.data.repository.sendGridKey.SendGridKeyDataSource
import com.example.data.repository.sendGridKey.SendGridKeySourceImpl
import com.example.data.repository.userDataSource.UploadDataSource
import com.example.data.repository.userDataSource.UploadDataSourceImpl
import com.example.data.repository.userDataSource.UserDataSourceImpl
import com.example.data.repository.userDataSource.UserDataSource
import com.example.data.repository.walletDataSource.TransactionDataSource
import com.example.data.repository.walletDataSource.TransactionDataSourceImpl
import com.example.util.Constants.DATABASE_NAME
import com.example.util.Constants.DATABASE_TEST
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mongodb.MongoClientSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.koin.dsl.module
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.io.InputStream

val koinModule = module {
    single {

//        val settings = MongoClientSettings.builder()
//            .applyToConnectionPoolSettings { builder ->
//                builder.maxSize(20).minSize(5)
//            }.build()

//         if (System.getenv("APP_ENV") == "test") {
//            KMongo.createClient().coroutine.getDatabase(DATABASE_TEST) // Local test database
//        } else {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val  database =  KMongo.createClient(System.getenv("MONGODB_URI")).coroutine.getDatabase(DATABASE_NAME) // Production database
                val result: Document? = database.runCommand<Document>(Document("profile", 2))

            } catch (e: Exception) {

            }
        }
//        }
    }
    single<UserDataSource> {
        UserDataSourceImpl(get())
    }
    single<CityDataSource> {
        CityDataSourceImpl(get())
    }
    single<ProfileDataSource> {
        ProfileDataSourceImpl(get())
    }
    single<AuthDataSource> {
        AuthDataSourceImpl(get())
    }
    single<AirLineDataSource> {
        AirLineDataSourceImpl(get())
    }
    single<UploadDataSource> {
        UploadDataSourceImpl()
    }
    single<AirPortDataSource> {
        AirPortsDataSourceImpl(get())
    }
    single<AirLineTicketDataSource> {
        AirLineTicketDataSourceImpl(get())
    }
    single<HotelDataSource> {
        HotelDataSourceImpl(get())
    }
    single<HotelTicketDataSource> {
        HotelTicketDataSourceImpl(get())
    }
    single<CartDataSource> {
        CartDataSourceImpl(get())
    }
    single<SubscriptionTypesDataSource> {
        SubscriptionTypesDataSourceImpl(get())
    }
    single<SearchDataSource> {
        SearchDataSourceImpl(get())
    }
    single<SendGridDataSource> {
        SendGridDataSourceImpl(get())
    }
    single<PaymentDataSource> {
        PaymentDataSourceImpl(get())
    }
    single<TransactionDataSource> {
        TransactionDataSourceImpl(get())
    }
    single<PurchaseDataSource> {
        PurchaseDataSourceImpl(get())
    }
    single<AdminWalletDataSource> {
        AdminWalletDataSourceImpl(get())
    }
    single<SendGridKeyDataSource> {
        SendGridKeySourceImpl()
    }
//    single {
//        val serviceAccount = object {}::class.java.classLoader.getResourceAsStream("ktor-sendgrid.json") ?: throw IllegalStateException("ktor-sendgrid.json not found!")
//        val mapper = jacksonObjectMapper()
//        val config: JsonNode = mapper.readTree(serviceAccount)
//        config["send_gr_key"].asText()
//    }

}