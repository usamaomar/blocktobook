package com.example.util

enum class SubscriptionType {
    Free,
    Monthly,
    SixMonth,
    Yearly
}

enum class AccessRole {
    Merchant,
    User,
    Admin
}

enum class TravelClass {
    ECONOMY,
    BUSINESS,
    PREMIUM_ECONOMY,
    FIRST_CLASS
}

enum class Status {
    PENDING, // 0
    CANCELED,// 1
    REJECTED,// 2
    APPROVED,// 3
    EDITED,// 4
    EDITED_FOR_REJECTION,// 5
}

enum class TransportationClass {
    BUS_IN_GROUP,
    PRIVATE_CAR_FUR_SEATS,
    PRIVATE_CAR_SIX_SEATS,
    PRIVATE_CAR_EIGHT_SEATS
}


enum class HotelClass {
    ONE_BEAD,
    TWO_BEAD,
    THREE_BEAD,
    FOUR_BEAD,
    FIVE_BEAD
}

enum class TopUpType {
    CART,
    SUBSCRIPTION,
    PURCHASE,
    BLOCK_TO_BOOK_FEES,
    VISA_FEES,
    EDIT_FEES,
    CANCEL_FEES,
}

enum class TransactionType {
    PLUS,
    MINUS
}

private fun String.monthField() = "\$substr: ['$this', 3, 2]" // Extract MM
private fun String.yearField() = "\$substr: ['$this', 6, 4]" // Extract yyyy


object paramNames {
    val SearchText = "SearchText"
    val PageSize = "PageSize"
    val PageNumber = "PageNumber"
    val FilterByCityId = "FilterByCityId"
    val FilterByHotelId = "FilterByHotelId"
    val FilterByDateFrom = "FilterByDateFrom"
    val FilterByDateTo = "FilterByDateTo"
    val FilterByDate = "FilterByDate"
    val FilterByVisibility = "FilterByVisibility"
    val FilterByPriceRangeFrom = "FilterByPriceRangeFrom"
    val FilterByPriceRangeTo = "FilterByPriceRangeTo"
    val FilterByHotelIds = "FilterByHotelIds"
    val FilterByAirLineIds = "FilterByAirLineIds"
    val FilterByAdultsTicketNumber = "FilterByAdultsTicketNumber"
    val FilterByChildrenTicketNumber = "FilterByChildrenTicketNumber"
    val FilterByRoomsTicketNumber = "FilterByRoomsTicketNumber"
    val FilterByIdFromAirport = "FilterByIdFromAirport"
    val FilterByIdFromCity = "FilterByIdFromCity"
    val FilterByIdToAirport = "FilterByIdToAirport"
    val FilterByIdToCity = "FilterByIdToCity"
    val DirectFlightOnly = "DirectFlightOnly"
    val languageId = "x-app-language-id"
    val status = "Status"
    val Id = "id"
    val ImageUrl = "imageUrl"
}