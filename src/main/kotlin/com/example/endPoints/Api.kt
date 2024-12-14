package com.example.endPoints

object Api {

    const val BASE = "Api"

    object AirLines : ApiPath {
        override val path: String
            get() = "$BASE/AirLines"

        object GetById : ApiPath {
            override val path: String
                get() = "${AirLines.path}/GetById"
        }
        object GetAll : ApiPath {
            override val path: String
                get() = "${AirLines.path}/GetAll"
        }

        object POST : ApiPath {
            override val path: String
                get() = "${AirLines.path}/POST"
        }
        object PUT : ApiPath {
            override val path: String
                get() = "${AirLines.path}/PUT"
        }
    }


    object Hotels : ApiPath {
        override val path: String
            get() = "$BASE/Hotels"

        object GetById : ApiPath {
            override val path: String
                get() = "${Hotels.path}/GetById"
        }
        object GetAll : ApiPath {
            override val path: String
                get() = "${Hotels.path}/GetAll"
        }

        object POST : ApiPath {
            override val path: String
                get() = "${Hotels.path}/POST"
        }
        object PUT : ApiPath {
            override val path: String
                get() = "${Hotels.path}/PUT"
        }
    }


    object AirPorts : ApiPath {
        override val path: String
            get() = "$BASE/AirPorts"

        object GetById : ApiPath {
            override val path: String
                get() = "${AirPorts.path}/GetById"
        }
        object GetAll : ApiPath {
            override val path: String
                get() = "${AirPorts.path}/GetAll"
        }

        object POST : ApiPath {
            override val path: String
                get() = "${AirPorts.path}/POST"
        }
        object PUT : ApiPath {
            override val path: String
                get() = "${AirPorts.path}/PUT"
        }
    }

    object Cities : ApiPath {
        override val path: String
            get() = "$BASE/Cities"

        object GetById : ApiPath {
            override val path: String
                get() = "${Cities.path}/GetById"
        }
        object GetAll : ApiPath {
            override val path: String
                get() = "${Cities.path}/GetAll"
        }

        object POST : ApiPath {
            override val path: String
                get() = "${Cities.path}/POST"
        }
        object PUT : ApiPath {
            override val path: String
                get() = "${Cities.path}/PUT"
        }
    }
    object SubscriptionTypes : ApiPath {
        override val path: String
            get() = "$BASE/SubscriptionTypes"

        object GetById : ApiPath {
            override val path: String
                get() = "${SubscriptionTypes.path}/GetById"
        }
        object GetAll : ApiPath {
            override val path: String
                get() = "${SubscriptionTypes.path}/GetAll"
        }

        object POST : ApiPath {
            override val path: String
                get() = "${SubscriptionTypes.path}/POST"
        }
        object PUT : ApiPath {
            override val path: String
                get() = "${SubscriptionTypes.path}/PUT"
        }
    }
    object Auth : ApiPath {
        override val path: String
            get() = "$BASE/Auth"

        object GoogleLoginMerchant : ApiPath {
            override val path: String
                get() = "${Auth.path}/GoogleLoginMerchant"
        }
        object LoginByEmailMerchant : ApiPath {
            override val path: String
                get() = "${Auth.path}/LoginByEmailMerchant"
        }

        object CreateEmailMerchant : ApiPath {
            override val path: String
                get() = "${Auth.path}/CreateEmailMerchant"
        }
        object CreateEmailAdmin : ApiPath {
            override val path: String
                get() = "${Auth.path}/CreateEmailAdmin"
        }

        object Refresh : ApiPath {
            override val path: String
                get() = "${Auth.path}/Refresh"
        }
        object ForgotPassword : ApiPath {
            override val path: String
                get() = "${Auth.path}/ForgotPassword"
        }

    }

    object Profile : ApiPath {
        override val path: String
            get() = "$BASE/Profile"

        object UpdateCompany : ApiPath {
            override val path: String
                get() = "${Profile.path}/UpdateCompany"
        }
        object UpdateUserCompany : ApiPath {
            override val path: String
                get() = "${Profile.path}/UpdateUserCompany"
        }

        object UpdateUserCompanyByUser : ApiPath {
            override val path: String
                get() = "${Profile.path}/UpdateUserCompanyByUser"
        }

        object AdminUserApprove : ApiPath {
            override val path: String
                get() = "${Profile.path}/AdminUserApprove"
        }

        object UpdateSubscription : ApiPath {
            override val path: String
                get() = "${Profile.path}/UpdateSubscription"
        }
        object UpdateUserSubscription : ApiPath {
            override val path: String
                get() = "${Profile.path}/UpdateUserSubscription"
        }

    }


    object User : ApiPath {
        override val path: String
            get() = "$BASE/User"

        object GetById : ApiPath {
            override val path: String
                get() = "${User.path}/GetById"
        }

        object Put : ApiPath {
            override val path: String
                get() = "${User.path}/Put"
        }
        object GetAll : ApiPath {
            override val path: String
                get() = "${User.path}/GetAll"
        }

    }


    object Upload : ApiPath {
        override val path: String
            get() = "$BASE/Upload"

        object Create : ApiPath {
            override val path: String
                get() = "${Upload.path}/Create"
        }

    }

    object AirLineTicket : ApiPath {
        override val path: String
            get() = "$BASE/AirLineTicket"

        object POST : ApiPath {
            override val path: String
                get() = "${AirLineTicket.path}/POST"
        }

        object Put : ApiPath {
            override val path: String
                get() = "${AirLineTicket.path}/Put"
        }
        object GetById : ApiPath {
            override val path: String
                get() = "${AirLineTicket.path}/GetById"
        }

        object GetAll : ApiPath {
            override val path: String
                get() = "${AirLineTicket.path}/GetAll"
        }

    }


    object HotelTicket : ApiPath {
        override val path: String
            get() = "$BASE/HotelTicket"

        object POST : ApiPath {
            override val path: String
                get() = "${HotelTicket.path}/POST"
        }

        object Put : ApiPath {
            override val path: String
                get() = "${HotelTicket.path}/Put"
        }

        object GetAll : ApiPath {
            override val path: String
                get() = "${HotelTicket.path}/GetAll"
        }


    }

    object Search : ApiPath {
        override val path: String
            get() = "$BASE/Search"



        object GetAllByCityNameAndHotelName : ApiPath {
            override val path: String
                get() = "${Search.path}/GetAllByCityNameAndHotelName"
        }

        object GetAllByCityNameAndAirportsName : ApiPath {
            override val path: String
                get() = "${Search.path}/GetAllByCityNameAndAirportsName"
        }
        object GetAllTicketsFiltration : ApiPath {
            override val path: String
                get() = "${Search.path}/GetAllTicketsFiltration"
        }
        object GetAllFlightTicketsFiltration : ApiPath {
            override val path: String
                get() = "${Search.path}/GetAllFlightTicketsFiltration"
        }
        object GetAllMonthTicketsFiltration : ApiPath {
            override val path: String
                get() = "${Search.path}/GetAllMonthTicketsFiltration"
        }
        object GetAllMonthFlightTicketsFiltration : ApiPath {
            override val path: String
                get() = "${Search.path}/GetAllMonthFlightTicketsFiltration"
        }


    }


    object Cart : ApiPath {
        override val path: String
            get() = "$BASE/Cart"



        object POST : ApiPath {
            override val path: String
                get() = "${Cart.path}/POST"
        }


        object GetAll : ApiPath {
            override val path: String
                get() = "${Cart.path}/GetAll"
        }

        object GetCartAmount : ApiPath {
            override val path: String
                get() = "${Cart.path}/GetCartAmount"
        }

        object DELETE : ApiPath {
            override val path: String
                get() = "${Cart.path}/DELETE"
        }



    }
    object Payment : ApiPath {
        override val path: String
            get() = "$BASE/Payment"



        object CreateCartCheckout : ApiPath {
            override val path: String
                get() = "${Payment.path}/CreateCartCheckout"
        }

        object CreateSubscriptionCheckout : ApiPath {
            override val path: String
                get() = "${Payment.path}/CreateSubscriptionCheckout"
        }


        object GetPaymentStatus : ApiPath {
            override val path: String
                get() = "${Payment.path}/GetPaymentStatus"
        }

        object GetSubscriptionPaymentStatus : ApiPath {
            override val path: String
                get() = "${Payment.path}/GetSubscriptionPaymentStatus"
        }

    }
    object Purchase : ApiPath {
        override val path: String
            get() = "$BASE/Purchase"
        object  Checkout : ApiPath {
            override val path: String
                get() = "${Purchase.path}/Checkout"
        }
        object  CheckoutSubscription : ApiPath {
            override val path: String
                get() = "${Purchase.path}/CheckoutSubscription"
        }
        object  ApproveHotelReservation : ApiPath {
            override val path: String
                get() = "${Purchase.path}/ApproveHotelReservation"
        }
        object  RejectHotelReservation : ApiPath {
            override val path: String
                get() = "${Purchase.path}/RejectHotelReservation"
        }
        object  CancelHotelReservation : ApiPath {
            override val path: String
                get() = "${Purchase.path}/CancelHotelReservation"
        }
        object  GetAllHotelReservations : ApiPath {
            override val path: String
                get() = "${Purchase.path}/GetAllHotelReservations"
        }
        object  GetAllForAirlines : ApiPath {
            override val path: String
                get() = "${Purchase.path}/GetAllForAirlines"
        }
        object  GetAllMerchantAirlineReservations : ApiPath {
            override val path: String
                get() = "${Purchase.path}/GetAllMerchantAirlineReservations"
        }
        object  GetAllMerchantHotelReservations : ApiPath {
            override val path: String
                get() = "${Purchase.path}/GetAllMerchantHotelReservations"
        }
        object  CreateOrUpdateCustomerInfo : ApiPath {
            override val path: String
                get() = "${Purchase.path}/CreateOrUpdateCustomerInfo"
        }
        object  CreateOrUpdateCustomerInfoList : ApiPath {
            override val path: String
                get() = "${Purchase.path}/CreateOrUpdateCustomerInfoList"
        }

    }

    object Wallet : ApiPath {
        override val path: String
            get() = "$BASE/Wallet"



        object TopUpAdminCart : ApiPath {
            override val path: String
                get() = "${Wallet.path}/TopUpAdminCart"
        }

        object TopUpRequestEmail : ApiPath {
            override val path: String
                get() = "${Wallet.path}/TopUpRequestEmail"
        }


        object GetAllWalletsByUserId : ApiPath {
            override val path: String
                get() = "${Wallet.path}/GetAllWalletsByUserId"
        }

        object GetWalletAmountByUserId : ApiPath {
            override val path: String
                get() = "${Wallet.path}/GetWalletAmountByUserId"
        }
        object GetWalletAmountByAdmin : ApiPath {
            override val path: String
                get() = "${Wallet.path}/GetWalletAmountByAdmin"
        }



    }

}