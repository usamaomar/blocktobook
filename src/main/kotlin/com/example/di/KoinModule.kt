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
import com.example.data.repository.uploadDataSource.UploadDataSourceImpl
import com.example.data.repository.userDataSource.UserDataSourceImpl
import com.example.data.repository.userDataSource.UserDataSource
import com.example.data.repository.walletDataSource.TransactionDataSource
import com.example.data.repository.walletDataSource.TransactionDataSourceImpl
import com.example.util.Constants.DATABASE_NAME
import com.example.util.Constants.DATABASE_TEST
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val koinModule = module {


    single {
//         if (System.getenv("APP_ENV") == "test") {
//         KMongo.createClient().coroutine.getDatabase(DATABASE_TEST) // Local test database
//        } else {
          KMongo.createClient(System.getenv("MONGODB_URI")).coroutine.getDatabase(DATABASE_NAME) // Production database
//      val database =   KMongo.createClient(System.getenv("MONGODB_URI")).coroutine.getDatabase(DATABASE_NAME) // Production database
//        }

        // Create indexes on collections
//        CoroutineScope(Dispatchers.IO).launch {
//            database.getCollection<User>("users")
//                .createIndex(Indexes.ascending("email"), IndexOptions().unique(true))
//
//            database.getCollection<CityModel>("cities")
//                .createIndex(Indexes.text("name"))
//
//            database.getCollection<HotelModel>("hotels")
//                .createIndex(Indexes.ascending("location"))
//
//            database.getCollection<PurchaseModel>("purchases")
//                .createIndex(Indexes.compoundIndex(Indexes.ascending("userId"), Indexes.descending("createdAt")))
//            val airLinesTickets = database.getCollection<AirlineTicketModel>("airLineTickets")
//            airLinesTickets.createIndex(Indexes.ascending("userId")) // Index for user-based queries
//            airLinesTickets.createIndex(Indexes.ascending("departureCityId", "arrivalCityId")) // Index for city-based queries
//            airLinesTickets.createIndex(Indexes.ascending("departureDate", "arrivalDate")) // Optimize date range filtering
//            airLinesTickets.createIndex(Indexes.ascending("pricePerSeat")) // Optimize price range filtering
//            airLinesTickets.createIndex(Indexes.ascending("isRoundTrip")) // Filter for round-trip tickets
//            airLinesTickets.createIndex(Indexes.ascending("airLineId")) // Index for airline-based queries
//            airLinesTickets.createIndex(Indexes.ascending("roundTripId")) // Filter for round-trip tickets (if needed)
//            airLinesTickets.createIndex(Indexes.ascending("departureAirportId", "arrivalAirportId")) // Airports-based queries
//            airLinesTickets.createIndex(Indexes.ascending("flightNumber")) // Flight number-based queries
//        }

//        database
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
