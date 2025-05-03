package com.example.data.repository.cartDataSource

//import com.example.domain.model.airlinesTicketModel.TicketModel
import com.example.domain.model.airlinesModel.ResponseAirLineModel
import com.example.domain.model.airlinesModel.toResponseAirLineModel
import com.example.domain.model.airlinesTicketModel.AirlineTicketModel
import com.example.domain.model.airlinesTicketModel.toResponseAirlineTicketModel
import com.example.domain.model.airportsModel.ResponseAirPortModel
import com.example.domain.model.airportsModel.toResponseAirPortModel
import com.example.domain.model.cartModel.CartModel
import com.example.domain.model.cartModel.CreateCartModel
import com.example.domain.model.cartModel.ResponseCartModel
import com.example.domain.model.cartModel.toCartModel
import com.example.domain.model.cityModel.ResponseCityModel
import com.example.domain.model.cityModel.toResponseCityModel
import com.example.domain.model.hotelModel.HotelModel
import com.example.domain.model.hotelModel.ResponseHotelModel
import com.example.domain.model.hotelModel.toResponseHotelModel
import com.example.domain.model.hotelTicketModel.HotelTicketModel
import com.example.domain.model.hotelTicketModel.ResponseHotelTicketModel
import com.example.domain.model.hotelTicketModel.toResponseHotelTicketModel
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.purchaseModel.PurchaseModel
import com.example.domain.model.subscriptionTypesModel.SubscriptionTypeModel
import com.example.domain.model.userModel.User
import com.example.domain.model.walletAmountModel.WalletAmountModel
import com.example.util.toSafeString
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Updates
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.div
import org.litote.kmongo.eq
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date

class CartDataSourceImpl(database: CoroutineDatabase) : CartDataSource {
    private val carts = database.getCollection<CartModel>()
    private val users = database.getCollection<User>()
    private val hotelTickets = database.getCollection<HotelTicketModel>()
    private val airLinesTickets = database.getCollection<AirlineTicketModel>()
    private val hotels = database.getCollection<HotelModel>()
    private val purchaseModel = database.getCollection<PurchaseModel>()
    private val subscriptionTypeDatabase = database.getCollection<SubscriptionTypeModel>()

    private val errorCode: Int = 2552
    private val cartDataSource: CartDataSource by KoinJavaComponent.inject(CartDataSource::class.java)


    override suspend fun post(
        userId: String,
        cartModel: CreateCartModel,
        xAppLanguageId: Int
    ): ApiResponse<String?> {
        val userCarts = carts.deleteMany(CartModel::userId eq userId)
        if (cartModel.hotelTicketId.toSafeString() != null) {
            val filter = Filters.eq("_id", ObjectId(cartModel.hotelTicketId))
            val hotelTicketModel = hotelTickets.findOne(filter)
                ?: return ApiResponse(
                    data = "Hotel Ticket Not Found",
                    succeeded = true,
                    errorCode = errorCode
                )

            val filterHotel = Filters.eq("_id", ObjectId(hotelTicketModel.hotelId))
            val hotelModel = hotels.findOne(filterHotel)
            val responseHotelTicketModel = hotelTicketModel.toResponseHotelTicketModel(
                hotelTicketModel.id?.toHexString().toString(),
                hotel = hotelModel?.toResponseHotelModel(
                    xAppLanguageId,
                    hotelModel.id?.toHexString().toString()
                )
            )
            if (responseHotelTicketModel.userId == userId) {
                return ApiResponse(
                    data = "You cant buy your own ticket",
                    succeeded = true,
                    errorCode = errorCode
                )
            }
            val cartListItems = carts.find(Filters.eq("userId", userId)).toList()


            val validationError = isCartModelValid(
                cartListItems,
                cartModel.toCartModel(userId, responseHotelTicketModel, null, null)
            )

            if (validationError != null) {
                return ApiResponse(
                    data = "Error",
                    succeeded = false,
                    errorCode = errorCode,
                    message = arrayListOf("The selected date range conflicts with existing bookings and exceeds room availability.")
                )
            }

            val purchaseModelListItems = purchaseModel.find(
                Filters.eq(
                    "hotelTicketModel.hotelId",
                    ObjectId(cartModel.hotelTicketId)
                )
            ).toList()


            val validateCartAddition = validateCartAddition(
                purchaseModelListItems,
                cartModel.toCartModel(userId, responseHotelTicketModel, null, null)
            )

            if (validateCartAddition != null) {
                return ApiResponse(
                    data = "Error",
                    succeeded = false,
                    errorCode = errorCode,
                    message = arrayListOf("The selected date range conflicts with existing bookings and exceeds room availability.")
                )
            }
            val local: CartModel =
                cartModel.toCartModel(userId, responseHotelTicketModel, null, null)
            carts.insertOne(document = local)
            return ApiResponse(data = "Success", succeeded = true, errorCode = errorCode)
        } else if (cartModel.airLineTripId.toSafeString() != null) {
            val responseAirlineTicketModel =
                airLinesTickets.findOne(Filters.eq("_id", ObjectId(cartModel.airLineTripId)))

            if(cartModel.returnAirLineTripId.toSafeString()!=null) {
                val responseReturnAirlineTicketModel =
                    airLinesTickets.findOne(
                        Filters.eq(
                            "_id",
                            ObjectId(cartModel.returnAirLineTripId)
                        )
                    )
                val local: CartModel = cartModel.toCartModel(
                    userId, null, responseAirlineTicketModel?.toResponseAirlineTicketModel(
                        responseAirlineTicketModel.id?.toHexString() ?: "",
                        departureCity = ResponseCityModel(
                            id = responseAirlineTicketModel.departureCityId,
                            name = "",
                            countryName = "",
                            twoDigitCountryCode = "",
                            threeDigitCountryCode = ""
                        ),
                        arrivalCity = ResponseCityModel(
                            id = responseAirlineTicketModel.arrivalCityId,
                            name = "",
                            countryName = "",
                            twoDigitCountryCode = "",
                            threeDigitCountryCode = ""
                        ),
                        departureAirport = ResponseAirPortModel(
                            id = responseAirlineTicketModel.departureAirportId,
                            name = "",
                            code = ""
                        ),
                        arrivalAirport = ResponseAirPortModel(
                            id = responseAirlineTicketModel.arrivalAirportId,
                            name = "",
                            code = ""
                        ),
                        airLine = ResponseAirLineModel(
                            id = responseAirlineTicketModel.airLineId,
                            name = "",
                            logo = "",
                            code = ""
                        ), returnAirLine = null
                    ), responseReturnAirlineTicketModel?.toResponseAirlineTicketModel(
                        responseReturnAirlineTicketModel.id?.toHexString() ?: "",
                        departureCity = ResponseCityModel(
                            id = responseReturnAirlineTicketModel.departureCityId,
                            name = "",
                            countryName = "",
                            twoDigitCountryCode = "",
                            threeDigitCountryCode = ""
                        ),
                        arrivalCity = ResponseCityModel(
                            id = responseReturnAirlineTicketModel.arrivalCityId,
                            name = "",
                            countryName = "",
                            twoDigitCountryCode = "",
                            threeDigitCountryCode = ""
                        ),
                        departureAirport = ResponseAirPortModel(
                            id = responseReturnAirlineTicketModel.departureAirportId,
                            name = "",
                            code = ""
                        ),
                        arrivalAirport = ResponseAirPortModel(
                            id = responseReturnAirlineTicketModel.arrivalAirportId,
                            name = "",
                            code = ""
                        ),
                        airLine = ResponseAirLineModel(
                            id = responseReturnAirlineTicketModel.airLineId,
                            name = "",
                            logo = "",
                            code = ""
                        ), returnAirLine = null
                    )
                )
                val carts = carts.insertOne(document = local)
                return if (carts.wasAcknowledged()) {
                    ApiResponse(data = "Success", succeeded = true, errorCode = errorCode)
                } else {
                    ApiResponse(data = "Not Success", succeeded = true, errorCode = errorCode)
                }
            }else{
                val local: CartModel = cartModel.toCartModel(
                    userId, null, responseAirlineTicketModel?.toResponseAirlineTicketModel(
                        responseAirlineTicketModel.id?.toHexString() ?: "",
                        departureCity = ResponseCityModel(
                            id = responseAirlineTicketModel.departureCityId,
                            name = "",
                            countryName = "",
                            twoDigitCountryCode = "",
                            threeDigitCountryCode = ""
                        ),
                        arrivalCity = ResponseCityModel(
                            id = responseAirlineTicketModel.arrivalCityId,
                            name = "",
                            countryName = "",
                            twoDigitCountryCode = "",
                            threeDigitCountryCode = ""
                        ),
                        departureAirport = ResponseAirPortModel(
                            id = responseAirlineTicketModel.departureAirportId,
                            name = "",
                            code = ""
                        ),
                        arrivalAirport = ResponseAirPortModel(
                            id = responseAirlineTicketModel.arrivalAirportId,
                            name = "",
                            code = ""
                        ),
                        airLine = ResponseAirLineModel(
                            id = responseAirlineTicketModel.airLineId,
                            name = "",
                            logo = "",
                            code = ""
                        ), returnAirLine = null
                    ), null
                )
                val carts = carts.insertOne(document = local)
                return if (carts.wasAcknowledged()) {
                    ApiResponse(data = "Success", succeeded = true, errorCode = errorCode)
                } else {
                    ApiResponse(data = "Not Success", succeeded = true, errorCode = errorCode)
                }
            }
        } else {
            return ApiResponse(data = "Not Success", succeeded = false, errorCode = errorCode)
        }
    }

    override suspend fun getAll(
        userId: String,
        pageSize: Int,
        pageNumber: Int,
        xAppLanguageId: Int
    ): PagingApiResponse<List<ResponseCartModel>?> {
        val skip = (pageNumber - 1) * pageSize
        // Perform the query
        val totalCount = carts.countDocuments(Filters.eq("userId", userId)).toInt()
        val totalPages =
            if (totalCount % (if (pageSize == 0) 1 else pageSize) == 0) totalCount / (if (pageSize == 0) 1 else pageSize) else (totalCount / (if (pageSize == 0) 1 else pageSize)) + 1
        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages
        // Fetch the carts with pagination
        val cartList = carts.find(Filters.eq("userId", userId))
            .skip(skip)
            .limit(pageSize)
            .toList()
            .map { cartModel ->
                // Map each cartModel to ResponseCartModel
                ResponseCartModel(
                    id = cartModel.id?.toHexString() ?: "",
                    hotelTicketModel = cartModel.hotelTicketModel?.let {
                        ResponseHotelTicketModel(
                            id = it.id,
                            hotel = it.hotel?.let { hotel ->
                                ResponseHotelModel(
                                    id = hotel.id,
                                    name = hotel.name,
                                    profiles = hotel.profiles,
                                    latitude = hotel.latitude,
                                    longitude = hotel.longitude,
                                    stars = hotel.stars,
                                    city = hotel.city,
                                    logo = hotel.logo
                                )
                            },
                            pricePerNight = it.pricePerNight,
                            ticketNumber = it.ticketNumber,
                            roomId = it.roomId,
                            userId = it.userId,
                            fromDate = it.fromDate,
                            toDate = it.toDate,
                            roomClass = it.roomClass,
                            transportation = it.transportation,
                            numberOfRoomsPerNight = it.numberOfRoomsPerNight,
                            isVisible = it.isVisible,
                            numberOfChildrenAllowance = it.numberOfChildrenAllowance,
                            numberOfAdultsAllowance = it.numberOfAdultsAllowance,
                            childrenAge = it.childrenAge,
                        )
                    },
                    airLineModel = cartModel.airLineTicketModel,
                    returnAirLineTicketModel = cartModel.returnAirLineTicketModel,
                    userId = cartModel.userId,
                    checkOutDate = cartModel.checkOutDate,
                    checkInDate = cartModel.checkInDate,
                    numberOfRooms = cartModel.numberOfRooms,
                    numberOfInfants = cartModel.numberOfInfants,
                )
            }

        return PagingApiResponse(
            succeeded = true,
            data = cartList,
            currentPage = pageNumber,
            totalPages = totalPages,
            totalCount = totalCount,
            hasPreviousPage = hasPreviousPage,
            hasNextPage = hasNextPage,
            errorCode = null
        )
    }

    override suspend fun delete(userId: String, ticketId: String): ApiResponse<String?> {
//        val filter = and(
//            Filters.eq("userId", userId),
//            Filters.eq("hotelTicketModel.id", ticketId)
//        )
//
//        // Perform the deletion
//        val result = carts.deleteOne(filter)

        val userCarts = carts.deleteMany(CartModel::userId eq userId)

        // Check if a document was deleted and return the appropriate response
        return if (userCarts.deletedCount > 0) {
            ApiResponse(data = "Success", succeeded = true, errorCode = errorCode)
        } else {
            ApiResponse(data = null, succeeded = false, errorCode = errorCode)
        }
    }

    override suspend fun getAmount(userId: String): String {
        val userCarts = carts.find(CartModel::userId eq userId).toList()
        val totalPrice = userCarts.calculateTotalPrice()
        return formatAmount(totalPrice)
    }

    override suspend fun getAmountWithBlockFees(userId: String): String {
        val userCarts = carts.find(CartModel::userId eq userId).toList()
        val totalPrice = userCarts.calculateTotalPrice()
        val blockFees = (users.findOne(filter = User::id eq userId)?.companyInfo?.blockToBookFees
            ?: 0).toDouble()
        return formatAmount(totalPrice + blockFees)
    }

    override suspend fun getAmountWithCurrentWalletAmountWithBlockFees(
        userId: String,
        double: Double
    ): String {
        val userCarts = carts.find(CartModel::userId eq userId).toList()
        val totalPrice = userCarts.calculateTotalPrice()
        val finalPrice = totalPrice - double
        val blockFees = (users.findOne(filter = User::id eq userId)?.companyInfo?.blockToBookFees
            ?: 0).toDouble()
        return formatAmount(finalPrice + blockFees)
    }

    override suspend fun generateUniqueTexts(): String {
        return  generateCombinedUniqueText()
    }

    private fun generateCombinedUniqueText(lengthPerPart: Int = 2): String {
        val charset = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        val generated = mutableSetOf<String>()

        while (generated.size < 1) {
            val part = (1..lengthPerPart)
                .map { charset.random() }
                .joinToString("")
            generated.add(part)
        }

        return generated.joinToString("-") // You can change "-" to "" if no separator is needed
    }

    override suspend fun getAmountWithCurrentWalletAmountWithSubscription(
        userId: String,
        double: Double,
        subscriptionId: String
    ): String {
        val filter = Filters.eq("_id", ObjectId(subscriptionId))
        val subscriptionTypeDatabase = subscriptionTypeDatabase.findOne(filter)
        val subscriptionPrice = (subscriptionTypeDatabase?.price ?: 0.0)
        val total = subscriptionPrice - double
        return formatAmount(total)
    }

    override suspend fun getSubscriptionAmount(userId: String, subscriptionId: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun getAmountWithSubscriptionFees(
        userId: String,
        subscriptionId: String
    ): String {
        val filter = Filters.eq("_id", ObjectId(subscriptionId))
        val subscriptionTypeDatabase = subscriptionTypeDatabase.findOne(filter)
        val subscriptionPrice = (subscriptionTypeDatabase?.price ?: 0.0)
        return formatAmount(subscriptionPrice)
    }
}


fun formatAmount(amount: Double): String {
    return String.format("%.2f", amount)
}

fun List<CartModel>.calculateTotalPrice(): Double {
    return this.sumOf { cart ->
        if(cart.hotelTicketModel!=null) {
            val hotelPrice = cart.hotelTicketModel.pricePerNight ?: 0.0
            // Calculate the number of nights between check-in and check-out dates
            val fromDate = cart.checkInDate
            val toDate = cart.checkOutDate
            val nights = run {
                val checkInDate = LocalDate.ofEpochDay(fromDate / (24 * 60 * 60 * 1000))
                val checkOutDate = LocalDate.ofEpochDay(toDate / (24 * 60 * 60 * 1000))
                ChronoUnit.DAYS.between(checkInDate, checkOutDate).toInt().coerceAtLeast(1)
            }
            (hotelPrice) * nights
        }else{
            if(cart.returnAirLineTicketModel!=null){
                if(cart.returnAirLineTicketModel.roundTripId == cart.airLineTicketModel?.roundTripId){
                    ( (cart.airLineTicketModel?.pricePerSeatRoundTrip ?: 0.0) * cart.numberOfRooms ) + ( (cart.airLineTicketModel?.pricePerInfantRoundTrip ?: 0.0) * (cart.numberOfInfants ?: 0) )

//                    (cart.airLineTicketModel?.pricePerSeatRoundTrip ?:0.0) * cart.numberOfRooms
                }else{
                    ( ((cart.airLineTicketModel?.pricePerSeat ?: 0.0) + (cart.returnAirLineTicketModel.pricePerSeat)) * cart.numberOfRooms) + ( ((cart.airLineTicketModel?.pricePerInfant ?: 0.0) + (cart.returnAirLineTicketModel.pricePerInfant?:0.0)) * (cart.numberOfInfants?:0))

//                    ((cart.airLineTicketModel?.pricePerSeat ?:0.0) + (cart.returnAirLineTicketModel.pricePerSeat)) * cart.numberOfRooms
                }
                }else  {
                ( (cart.airLineTicketModel?.pricePerSeat ?: 0.0) * cart.numberOfRooms) + ( (cart.airLineTicketModel?.pricePerInfant ?: 0.0) * (cart.numberOfInfants?:0))

//                (cart.airLineTicketModel?.pricePerSeat ?:0.0) * cart.numberOfRooms
            }
        }
    }
}

fun isCartModelValid(
    existingCartModels: List<CartModel>,
    newCartModel: CartModel
): String? {
    // Helper function to normalize dates to midnight
    fun normalizeDate(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            time = Date(timestamp)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    // Normalize check-in and check-out dates
    val newCheckInDate = normalizeDate(newCartModel.checkInDate)
    val newCheckOutDate = normalizeDate(newCartModel.checkOutDate)

    // Get hotel ticket details
    val hotelTicketModel =
        newCartModel.hotelTicketModel ?: return "Hotel ticket information is missing."
    val fromDateOriginal = normalizeDate(hotelTicketModel.fromDate)
    val toDateOriginal = normalizeDate(hotelTicketModel.toDate)
    val allowedNights = newCartModel.numberOfRooms ?: 1

    // Validate date range of the new cart item
    if (newCheckInDate < fromDateOriginal || newCheckOutDate > toDateOriginal) {
        return "The selected dates are outside the available range for the hotel ticket."
    }

    // Map to count room usage per night
    val roomUsageMap = mutableMapOf<Long, Int>()

    // Helper function to count room usage
    fun countRoomUsage(checkIn: Long, checkOut: Long) {
        var date = checkIn
        while (date < checkOut) {
            roomUsageMap[date] = roomUsageMap.getOrDefault(date, 0) + newCartModel.numberOfRooms
            date += 24 * 60 * 60 * 1000 // Move to the next day
        }
    }

    // Count room usage for existing cart items
    existingCartModels.forEach { cartItem ->
        if (cartItem.hotelTicketModel?.id == hotelTicketModel.id) {
            val existingCheckIn = normalizeDate(cartItem.checkInDate)
            val existingCheckOut = normalizeDate(cartItem.checkOutDate)
            countRoomUsage(existingCheckIn, existingCheckOut)
        }
    }

//    // Count room usage for the new cart item
    countRoomUsage(newCheckInDate, newCheckOutDate)

    // Check if any date exceeds the allowed nights
    for ((date, usage) in roomUsageMap) {
        if (usage > allowedNights) {
            return "The selected dates overlap with existing bookings and exceed the allowed room limit per night."
        }
    }

    // No issues found, return null (no error)
    return null
}

fun validateCartAddition(
    existingPurchases: List<PurchaseModel>,
    newCartModel: CartModel
): String? {
    // Helper function to normalize dates to midnight (removing time components)
    fun normalizeDate(timestamp: Long?): Long? {
        if (timestamp == null) return null
        val calendar = Calendar.getInstance().apply {
            time = Date(timestamp)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    // Normalize check-in and check-out dates for the new cart model
    val newCheckInDate = normalizeDate(newCartModel.checkInDate)
    val newCheckOutDate = normalizeDate(newCartModel.checkOutDate)

    // Validate if the new cart has hotel ticket information
    val hotelTicketModel =
        newCartModel.hotelTicketModel ?: return "Hotel ticket information is missing."
    val fromDateOriginal = normalizeDate(hotelTicketModel.fromDate)
    val toDateOriginal = normalizeDate(hotelTicketModel.toDate)
    val allowedRoomsPerNight = hotelTicketModel.numberOfRoomsPerNight ?: 1

    // Check if the date range of the new cart is valid
    if (newCheckInDate == null || newCheckOutDate == null || newCheckInDate < (fromDateOriginal
            ?: 0) || newCheckOutDate > (toDateOriginal ?: 0)
    ) {
        return "The selected dates are outside the available range for the hotel ticket."
    }

    // Map to track room usage per night
    val roomUsageMap = mutableMapOf<Long, Int>()

    // Helper function to count room usage
    fun countRoomUsage(checkIn: Long?, checkOut: Long?) {
        if (checkIn == null || checkOut == null) return
        var date = checkIn
        while (date < checkOut) {
            roomUsageMap[date] = roomUsageMap.getOrDefault(date, 0) + newCartModel.numberOfRooms
            date += 24 * 60 * 60 * 1000 // Move to the next day
        }
    }

    // Count room usage for existing purchases
    existingPurchases.forEach { purchase ->
        if (purchase.hotelTicketModel?.id == hotelTicketModel.id) {
            val existingCheckIn = normalizeDate(purchase.checkInDate)
            val existingCheckOut = normalizeDate(purchase.checkOutDate)
            countRoomUsage(existingCheckIn, existingCheckOut)
        }
    }

    // Count room usage for the new cart model
    countRoomUsage(newCheckInDate, newCheckOutDate)

    // Check if any date exceeds the allowed rooms per night
    for ((date, usage) in roomUsageMap) {
        if (usage > allowedRoomsPerNight) {
            return "The selected dates overlap with existing bookings and exceed the allowed room limit per night."
        }
    }

    // No conflicts found, return null (no error)
    return null
}
