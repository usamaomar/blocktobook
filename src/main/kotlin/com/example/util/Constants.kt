package com.example.util

object Constants {
    const val AUDIENCE = "439843256699-o2oqqk1vm64qmi06fhl0ufsrs2blnmp8.apps.googleusercontent.com"
    const val ISSUER = "https//accounts.google.com"
    const val DATABASE_TEST = "database"
    const val DATABASE_NAME = "BLOCKTOBOOK"
    private const val entityIdTest = "8ac7a4c794d062bb0194d13599b70518"
    private const val authorizationBearerTest = "Bearer OGFjN2E0Y2E4OTk0NDUyMDAxODk5NzFlOWM1YTA1MWZ8U2c4d3BiNm5IWQ=="
    const val COMPANY_INFO_NOT_FOUND = 2
    const val PAYMENT_LIVE = "https://eu-prod.oppwa.com"
    const val PAYMENT_TEST = "https://eu-test.oppwa.com"
    const val ADMIN_EMAIL = "usamakh@hayyakgroup.com"
    const val COMPANY_INFO_NOT_VERIFIED = 29
    const val SUBSCRIPTION_NOT_FOUND = 4
    const val SUBSCRIPTION_EXPIRED = 5
    const val TOKEN_IS_EXPIRED = 6
    const val USER_HAS_ACCOUNT = 99
    const val LOG_OU = 9
    const val YOU_DONT_HAVE_ACCESS = 909

    fun getEntityId(): String {
//        return entityIdTest;
        return  System.getenv("paymentEntityId")
    }

    fun getAuth(): String {
//        return authorizationBearerTest;
        return  System.getenv("paymentAuthorization")
    }

    fun getTwilioAccountSid(): String {
        return  System.getenv("twilioAccountSid")
    }

    fun getTwilioAuthToken(): String {
        return  System.getenv("twilioAuthToken")
    }

    fun getPaymentUrl(): String {
        return  PAYMENT_LIVE
//        return  PAYMENT_TEST
    }

}