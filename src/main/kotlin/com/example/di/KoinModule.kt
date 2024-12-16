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
import com.example.data.repository.userDataSource.UploadDataSource
import com.example.data.repository.userDataSource.UploadDataSourceImpl
import com.example.data.repository.userDataSource.UserDataSourceImpl
import com.example.data.repository.userDataSource.UserDataSource
import com.example.data.repository.walletDataSource.TransactionDataSource
import com.example.data.repository.walletDataSource.TransactionDataSourceImpl
import com.example.util.Constants.DATABASE_NAME
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val koinModule = module {
    single {
        KMongo.createClient().coroutine.getDatabase(DATABASE_NAME)
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

}