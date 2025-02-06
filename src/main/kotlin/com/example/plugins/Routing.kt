package com.example.plugins

 import com.example.routes.airLineRout
import com.example.routes.airLineTicketRout
import com.example.routes.airPortRout
import com.example.routes.authRout
import com.example.routes.cartRout
import com.example.routes.cityRout
import com.example.routes.hotelRout
import com.example.routes.hotelTicketRout
import com.example.routes.paymentRout
import com.example.routes.profileRout
import com.example.routes.purchaseRout
import com.example.routes.searchRout
import com.example.routes.subscriptionTypeRout
import com.example.routes.uploadRoute
import com.example.routes.userRoute
import com.example.routes.walletRoute
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        authRout()
        cartRout()
        profileRout()
        userRoute()
        walletRoute()
        airLineRout()
        cityRout()
        subscriptionTypeRout()
        uploadRoute()
        airPortRout()
        airLineTicketRout()
        hotelRout()
        hotelTicketRout()
        paymentRout()
        purchaseRout()
        searchRout()
    }
}
