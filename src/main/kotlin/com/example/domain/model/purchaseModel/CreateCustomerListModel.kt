package com.example.domain.model.purchaseModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class CreateCustomerListModel(
    val purchasedAirlineTicketId: String? = null,
    val createCustomerListModel: List<CreateCustomerModel>? = null,
) : Principal

fun List<CreateCustomerModel>?.toCustomerModelList(
    firstName: String? = null,
    lastName: String? = null,
    middleName: String? = null,
    passportUrl: String? = null,
    visaUrl: String? = null,
    note: String? = null,
    isInfant: Boolean? = false,
): List<CustomerModel> {
    return this?.map { createCustomer ->
        CustomerModel(
            firstName = firstName ?: createCustomer.firstName,
            lastName = lastName ?: createCustomer.lastName,
            middleName = middleName ?: createCustomer.middleName,
            passportUrl = passportUrl ?: createCustomer.passportUrl,
            visaUrl = visaUrl ?: createCustomer.visaUrl,
            note = note ?: createCustomer.note,
            isInfant = isInfant ?: createCustomer.isInfant
        )
    } ?: emptyList()
}


