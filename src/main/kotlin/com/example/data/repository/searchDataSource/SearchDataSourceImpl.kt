package com.example.data.repository.searchDataSource

import com.example.domain.model.airlinesModel.AirLineModel
import com.example.domain.model.airlinesModel.ResponseAirLineModel
import com.example.domain.model.airlinesModel.toResponseAirLineModel
import com.example.domain.model.airlinesTicketModel.AirlineTicketModel
import com.example.domain.model.airlinesTicketModel.ResponseAirlineTicketModel
import com.example.domain.model.airlinesTicketModel.toResponseAirlineTicketModel
import com.example.domain.model.airportsModel.AirPortModel
import com.example.domain.model.airportsModel.ResponseAirPortModel
import com.example.domain.model.airportsModel.toResponseAirPortModel
import com.example.domain.model.cityModel.CityModel
import com.example.domain.model.cityModel.CityProfileModel
import com.example.domain.model.cityModel.ResponseCityModel
import com.example.domain.model.cityModel.toResponseCityModel
import com.example.domain.model.hotelModel.HotelModel
import com.example.domain.model.hotelModel.HotelProfileModel
import com.example.domain.model.hotelModel.ResponseHotelModel
import com.example.domain.model.hotelModel.toResponseHotelModel
import com.example.domain.model.hotelTicketModel.HotelTicketModel
import com.example.domain.model.hotelTicketModel.ResponseHotelTicketModel
import com.example.domain.model.hotelTicketModel.RoomAvailability
import com.example.domain.model.hotelTicketModel.toResponseHotelTicketWithHotelModel
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.purchaseModel.PurchaseModel
import com.example.util.toFormattedDashDateString
import com.example.util.toFormattedDashMonthDateString
import com.example.util.toFormattedDashYearDateString
import com.example.util.toFormattedDateString
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.gt
import com.mongodb.client.model.Filters.gte
import com.mongodb.client.model.Filters.`in`
import com.mongodb.client.model.Filters.lt
import com.mongodb.client.model.Filters.lte
import com.mongodb.client.model.Filters.not
import com.mongodb.client.model.Filters.regex
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Sorts.ascending
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.BsonDocument
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.div
import org.litote.kmongo.elemMatch
import org.litote.kmongo.eq
import org.litote.kmongo.or
import org.litote.kmongo.regex
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SearchDataSourceImpl(database: CoroutineDatabase) : SearchDataSource {

    private val hotels = database.getCollection<HotelModel>()
    private val cities = database.getCollection<CityModel>()
    private val hotelTickets = database.getCollection<HotelTicketModel>()
    private val airports = database.getCollection<AirPortModel>()
    private val purchaseModel = database.getCollection<PurchaseModel>()
    private val airLinesTickets = database.getCollection<AirlineTicketModel>()

    private val citiesdatabase = database.getCollection<CityModel>()
    private val airPortsdatabase = database.getCollection<AirPortModel>()
    private val airLinesdatabase = database.getCollection<AirLineModel>()

    private val errorCode: Int = 209

    override suspend fun getAllTicketsFiltration(
        userId: String,
        pageSize: Int,
        pageNumber: Int,
        filterByAdultsTicketNumber: Int?,
        filterByChildrenTicketNumber: Int?,
        filterByRoomsTicketNumber: Int?,
        xAppLanguageId: Int,
        filterByDateFrom: Long?,
        filterByDateTo: Long?,
        filterByHotelId: String?,
        filterByCityId: String?
    ): PagingApiResponse<List<ResponseHotelTicketModel>?> {
        val skip = (pageNumber - 1) * pageSize
        val queryForItemFilter = mutableListOf<Bson>()

        // Filter by hotelId
        if (!filterByHotelId.isNullOrEmpty()) {
            queryForItemFilter.add(eq("hotelId", filterByHotelId))
        }

        // Filter by cityId
        if (!filterByCityId.isNullOrEmpty()) {
            val hotelsInCity = hotels.find(eq("cityId", filterByCityId)).toList()
            val hotelIdsInCity = hotelsInCity.map { it.id?.toHexString() ?: "" }
            queryForItemFilter.add(`in`("hotelId", hotelIdsInCity))
        }

        // Filter by number of adults, children, and rooms if not null
        if (filterByAdultsTicketNumber != null) {
            queryForItemFilter.add(gte("numberOfAdultsAllowance", filterByAdultsTicketNumber))
        }
//        if (filterByChildrenTicketNumber != null) {
//            queryForItemFilter.add(eq("numberOfChildrenAllowance", filterByChildrenTicketNumber))
//        }

        if (filterByRoomsTicketNumber != null) {
            queryForItemFilter.add(gte("numberOfRoomsPerNight", filterByRoomsTicketNumber))
        }
        // Final query
        val finalQuery = and(queryForItemFilter)

        // Get total count and pagination
        val totalCount = hotelTickets.countDocuments(finalQuery).toInt()
        val totalPages = if (totalCount % (if (pageSize == 0) 1 else pageSize) == 0)
            totalCount / (if (pageSize == 0) 1 else pageSize)
        else
            (totalCount / (if (pageSize == 0) 1 else pageSize)) + 1

        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages

        // Fetch data
        val tickets = hotelTickets.find(finalQuery)
            .sort(ascending("pricePerNight"))  // Order by price (ascending)
            .skip(skip)
            .limit(pageSize)
            .toList()
        // After fetching the data, apply the date filter (if not already applied in the query)
        val filteredTickets = tickets.filter { ticket ->
            val fromDate = getDateAsTimestamp(ticket.fromDate)
            val toDate = getDateAsTimestamp(ticket.toDate)

            // Apply the date range filter after fetching the data (if needed)
            val isWithinDateRange = when {
                filterByDateFrom != null && filterByDateTo != null ->
                    getDateAsTimestamp(fromDate) <= getDateAsTimestamp(filterByDateFrom.toLong()) && getDateAsTimestamp(
                        toDate
                    ) >= getDateAsTimestamp(filterByDateTo.toLong())

                filterByDateFrom != null -> getDateAsTimestamp(fromDate) >= getDateAsTimestamp(
                    filterByDateFrom.toLong()
                )

                filterByDateTo != null -> getDateAsTimestamp(toDate) <= getDateAsTimestamp(
                    filterByDateTo.toLong()
                )

                else -> true
            }

            isWithinDateRange
        }

        // Return data with the pagination details
        return PagingApiResponse(
            succeeded = true,
            data = filteredTickets.map { hotelTicketModel ->
                // Join with Hotel and City Models
                val hotelModel = hotels.findOne(eq("_id", ObjectId(hotelTicketModel.hotelId)))
                val cityModel = cities.findOne(eq("_id", ObjectId(hotelModel?.cityId)))
                val responseCityModel =
                    cityModel?.toResponseCityModel(xAppLanguageId, hotelModel?.cityId ?: "")
                val responseHotelModel = hotelModel?.toResponseHotelModel(
                    xAppLanguageId,
                    hotelTicketModel.hotelId,
                    responseCityModel
                )
                hotelTicketModel.toResponseHotelTicketWithHotelModel(
                    hotelTicketModel.id?.toHexString() ?: "",
                    responseHotelModel
                )
            },
            currentPage = pageNumber,
            totalPages = totalPages,
            totalCount = totalCount,
            hasPreviousPage = hasPreviousPage,
            hasNextPage = hasNextPage,
            errorCode = null
        )
    }


    override suspend fun getAllFlightTicketsFiltration(
        userId: String,
        pageSize: Int,
        pageNumber: Int,
        filterByAdultsTicketNumber: Int?,
        filterByChildrenTicketNumber: Int?,
        xAppLanguageId: Int,
        filterByDateFrom: Long?,
        filterByDateTo: Long?,
        filterByIdFromAirport: String?,
        filterByIdFromCity: String?,
        filterByIdToAirport: String?,
        filterByIdToCity: String?,
        directFlightOnly: Boolean?,
    ): PagingApiResponse<List<ResponseAirlineTicketModel>?> {
        val skip = (pageNumber - 1) * pageSize
        val queryForItemFilter = mutableListOf<Bson>()

        queryForItemFilter.add(eq("isVisible", true))

        if (filterByAdultsTicketNumber != null) {
            queryForItemFilter.add(gte("numberOfSeats", filterByAdultsTicketNumber))
        }

        if (directFlightOnly == true) {

            // Filter by hotelId
            if (!filterByIdFromAirport.isNullOrEmpty()) {
                queryForItemFilter.add(eq("departureAirportId", filterByIdFromAirport))
            }

            if (!filterByIdFromCity.isNullOrEmpty()) {
                queryForItemFilter.add(eq("departureCityId", filterByIdFromCity))
            }

            if (filterByDateFrom != null) {
                queryForItemFilter.add(
                    eq(
                        "departureDate",
                        filterByDateFrom.toFormattedDashDateString()
                    )
                )
            }

        } else {
            // Filter by hotelId
            if (!filterByIdFromAirport.isNullOrEmpty() || !filterByIdToAirport.isNullOrEmpty()) {
                val orConditions = mutableListOf<Bson>()
                orConditions.add(
                    and(
                        eq("departureAirportId", filterByIdFromAirport),
                        eq("arrivalAirportId", filterByIdToAirport)
                    )
                )

                orConditions.add(
                    and(
                        eq("departureAirportId", filterByIdToAirport),
                        eq("arrivalAirportId", filterByIdFromAirport)
                    )
                )
                queryForItemFilter.add(or(orConditions))
            }

            if (!filterByIdFromCity.isNullOrEmpty() || !filterByIdToCity.isNullOrEmpty()) {
                val orConditions = mutableListOf<Bson>()
                orConditions.add(
                    and(
                        eq("departureCityId", filterByIdFromCity),
                        eq("arrivalCityId", filterByIdToCity)
                    )
                )

                orConditions.add(
                    and(
                        eq("departureCityId", filterByIdToCity),
                        eq("arrivalCityId", filterByIdFromCity)
                    )
                )
                queryForItemFilter.add(or(orConditions))
            }

            if (filterByDateTo != null || filterByDateFrom != null) {
                val orConditions = mutableListOf<Bson>()

                if (filterByDateTo != null) {
                    orConditions.add(
                        eq(
                            "departureDate",
                            filterByDateTo.toFormattedDashDateString()
                        )
                    )
                    println("filterByDateTo = ${filterByDateTo.toFormattedDateString()}")

                }

                if (filterByDateFrom != null) {
                    orConditions.add(
                        eq(
                            "departureDate",
                            filterByDateFrom.toFormattedDashDateString()
                        )
                    )
                    println("filterByDateFrom = ${filterByDateFrom.toFormattedDateString()}")
                }

                queryForItemFilter.add(or(orConditions))
            }
        }

        val finalQuery =
            if (queryForItemFilter.isNotEmpty()) and(queryForItemFilter) else Filters.empty()
        val sortCriteria = ascending("departureDate", "pricePerSeat")

        val totalCount = airLinesTickets.countDocuments(finalQuery).toInt()
        val totalPages =
            if (totalCount % (if (pageSize == 0) 1 else pageSize) == 0) totalCount / (if (pageSize == 0) 1 else pageSize) else (totalCount / (if (pageSize == 0) 1 else pageSize)) + 1
        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages

        val listOfAirlinesTickets = airLinesTickets.find(finalQuery)
            .sort(sortCriteria)
            .skip(skip)
            .limit(pageSize)
            .toList()

        val filteredTickets = listOfAirlinesTickets.filter { ticket ->
            // Query the database for a matching purchase
            val matchingPurchase = purchaseModel.findOne(
                eq("airLineModel.id", ticket.id?.toHexString()) // Ensure this matches your schema
            )

            val purchasedSeats = matchingPurchase?.numberOfRooms ?: 0
            val availableSeats = (ticket.numberOfSeats ?: 0) - purchasedSeats

            // Return true if there are enough available seats
            availableSeats >= (filterByAdultsTicketNumber ?: 1)
        }

        return PagingApiResponse(
            succeeded = true,
            message = arrayListOf(
                filterByDateTo?.toFormattedDashDateString() ?: "",
                filterByDateFrom?.toFormattedDashDateString() ?: "",
                filterByIdFromAirport ?: "",
                filterByIdFromCity ?: "",
                filterByIdToAirport ?: "",
                filterByIdToCity ?: ""
            ),
            data = filteredTickets.map { ticket ->
                ticket.toResponseAirlineTicketModel(
                    ticket.id?.toHexString() ?: "",
                    departureCity = citiesdatabase.findOne(
                        eq("_id", ObjectId(ticket.departureCityId))
                    )?.toResponseCityModel(xAppLanguageId, ""),
                    arrivalCity = citiesdatabase.findOne(
                        eq("_id", ObjectId(ticket.arrivalCityId))
                    )?.toResponseCityModel(xAppLanguageId, ""),
                    departureAirport = airPortsdatabase.findOne(
                        eq("_id", ObjectId(ticket.departureAirportId))
                    )?.toResponseAirPortModel(),
                    arrivalAirport = airPortsdatabase.findOne(
                        eq("_id", ObjectId(ticket.arrivalAirportId))
                    )?.toResponseAirPortModel(),
                    airLine = airLinesdatabase.findOne(
                        eq("_id", ObjectId(ticket.airLineId))
                    )?.toResponseAirLineModel(),
                    returnAirLine = airLinesdatabase.findOne(
                        eq("_id", ObjectId(ticket.airLineId))
                    )?.toResponseAirLineModel(),
                )
            },
            currentPage = pageNumber,
            totalPages = totalPages,
            totalCount = totalCount,
            hasPreviousPage = hasPreviousPage,
            hasNextPage = hasNextPage,
            errorCode = errorCode
        )
    }

    override suspend fun getAllMonthFlightTicketsFiltration(
        userId: String,
        pageSize: Int,
        pageNumber: Int,
        filterByAdultsTicketNumber: Int?,
        filterByChildrenTicketNumber: Int?,
        xAppLanguageId: Int,
        filterByDate: Long?,
        filterByIdFromAirport: String?,
        filterByIdFromCity: String?,
        filterByIdToAirport: String?,
        filterByIdToCity: String?,
        directFlightOnly: Boolean?,
    ): PagingApiResponse<List<ResponseAirlineTicketModel>?> {
        val skip = (pageNumber - 1) * pageSize
        val queryForItemFilter = mutableListOf<Bson>()

        queryForItemFilter.add(eq("isVisible", true))

        if (filterByAdultsTicketNumber != null) {
            queryForItemFilter.add(gte("numberOfSeats", filterByAdultsTicketNumber))
        }

        if (directFlightOnly == true) {
            if (!filterByIdFromCity.isNullOrEmpty()) {
                queryForItemFilter.add(eq("departureCityId", filterByIdFromCity))
            }

            if (filterByDate != null) {
                queryForItemFilter.add(
                    eq(
                        "departureDate",
                        filterByDate.toFormattedDashDateString()
                    )
                )
            }
        } else {
            if (!filterByIdFromCity.isNullOrEmpty() || !filterByIdToCity.isNullOrEmpty()) {
                val orConditions = mutableListOf<Bson>()
                orConditions.add(
                    and(
                        eq("departureCityId", filterByIdFromCity),
                        eq("arrivalCityId", filterByIdToCity)
                    )
                )

                orConditions.add(
                    and(
                        eq("departureCityId", filterByIdToCity),
                        eq("arrivalCityId", filterByIdFromCity)
                    )
                )
                queryForItemFilter.add(or(orConditions))
            }

            if (filterByDate != null) {
                val orConditions = mutableListOf<Bson>()

                val targetMonth = filterByDate.toFormattedDashMonthDateString() // Extract month
                val targetYear = filterByDate.toFormattedDashYearDateString() // Extract year

                orConditions.add(
                    regex("departureDate", "^\\d{2}-$targetMonth-$targetYear$")

                )
                println("Filtering by month: $targetMonth, year: $targetYear (To)")


                queryForItemFilter.add(or(orConditions))
            }
        }

        val finalQuery =
            if (queryForItemFilter.isNotEmpty()) and(queryForItemFilter) else Filters.empty()
        val sortCriteria = ascending("departureDate", "pricePerSeat")
        val listOfAirlinesTickets =
            airLinesTickets.find(finalQuery).sort(sortCriteria).skip(skip).limit(pageSize).toList()


        val filteredTickets = listOfAirlinesTickets.filter { ticket ->
            // Query the database for a matching purchase
            val matchingPurchases = purchaseModel.find(
                eq("airLineModel.id", ticket.id?.toHexString()) // Ensure this matches your schema
            ).toList() // Convert the result to a list

            val totalPurchasedSeats = matchingPurchases.sumOf { it.numberOfRooms ?: 0 }
            val availableSeats = (ticket.numberOfSeats ?: 0) - totalPurchasedSeats

            // Return true if there are enough available seats
            availableSeats >= (filterByAdultsTicketNumber ?: 1)
        }

        val listOfLongs = mutableListOf<Long>()
        filteredTickets.map {
            listOfLongs.add(dateStringToTimestamp(it.departureDate))
        }.toList()
        return PagingApiResponse(
            succeeded = true,
            data = filteredTickets.map { hotelTicketModel ->
                hotelTicketModel.toResponseAirlineTicketModel(
                    hotelTicketModel.id?.toHexString() ?: "",
                    departureCity = ResponseCityModel(
                        id = hotelTicketModel.departureCityId,
                        name = "",
                        countryName = "",
                        twoDigitCountryCode = "",
                        threeDigitCountryCode = ""
                    ),
                    arrivalCity =
                    ResponseCityModel(
                        id = hotelTicketModel.arrivalCityId,
                        name = "",
                        countryName = "",
                        twoDigitCountryCode = "",
                        threeDigitCountryCode = ""
                    ),
                    departureAirport = null,
                    arrivalAirport = null,
                    airLine = null,
                    returnAirLine = null
                )
            },
            errorCode = null
        )
    }

    override suspend fun getReturnTicketDate(
        returnTicketId: String
    ): ApiResponse<String?> = withContext(Dispatchers.IO){
        val listOfAirlinesTickets =
            airLinesTickets.find(eq("roundTripId", returnTicketId)).toList()
          ApiResponse(
            succeeded = true,
            data = getLatestAirlineTicket(listOfAirlinesTickets)?.departureDate,
            errorCode = null
        )
    }
    private suspend fun getLatestAirlineTicket(tickets: List<AirlineTicketModel>): AirlineTicketModel? =
        withContext(Dispatchers.IO) {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            return@withContext tickets.maxByOrNull { LocalDate.parse(it.departureDate, formatter) }
        }


    fun dateStringToTimestamp(dateString: String): Long {
        // Define the formatter to match the input date format
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        // Parse the date string to LocalDate
        val localDate = LocalDate.parse(dateString, formatter)

        // Convert LocalDate to timestamp (epoch milliseconds)
        return localDate.atStartOfDay(ZoneId.of("Asia/Amman")).toInstant().toEpochMilli()
    }

    private fun String.monthField() = "\$substr: ['$this', 3, 2]" // Extract MM
    private fun String.yearField() = "\$substr: ['$this', 6, 4]" // Extract yyyy

    override suspend fun getAllMonthTicketsFiltration(
        userId: String,
        filterByAdultsTicketNumber: Int?,
        filterByChildrenTicketNumber: Int?,
        filterByRoomsTicketNumber: Int?,
        filterByDate: Long?,
        filterByHotelId: String?,
        filterByCityId: String?
    ): PagingApiResponse<List<Long>?> {
        val queryForItemFilter = mutableListOf<Bson>()

        // Filter by hotelId
        if (!filterByHotelId.isNullOrEmpty()) {
            queryForItemFilter.add(eq("hotelId", filterByHotelId))
        }

        // Filter by cityId
        if (!filterByCityId.isNullOrEmpty()) {
            val hotelsInCity = hotels.find(eq("cityId", filterByCityId)).toList()
            val hotelIdsInCity = hotelsInCity.map { it.id?.toHexString() ?: "" }
            queryForItemFilter.add(`in`("hotelId", hotelIdsInCity))
        }

        // Filter by number of adults, children, and rooms if not null
        if (filterByAdultsTicketNumber != null) {
            queryForItemFilter.add(gte("numberOfAdultsAllowance", filterByAdultsTicketNumber))
        }
//        if (filterByChildrenTicketNumber != null) {
//            queryForItemFilter.add(gte("numberOfChildrenAllowance", filterByChildrenTicketNumber))
//        }
        if (filterByRoomsTicketNumber != null) {
            queryForItemFilter.add(gte("numberOfRoomsPerNight", filterByRoomsTicketNumber))
        }
        // Final query
        val finalQuery = and(queryForItemFilter)

        // Get total count and pagination
        val totalCount = hotelTickets.find(finalQuery).toList()

        //////
        val queryForItemFilterMap = mutableListOf<Bson>()

        // Filter by hotelId
        if (!filterByHotelId.isNullOrEmpty()) {
            queryForItemFilterMap.add(eq("hotelTicketModel.hotel.id", ObjectId(filterByHotelId)))
        }
        if (!filterByCityId.isNullOrEmpty()) {

            val hotelsInCity = hotels.find(eq("cityId", filterByCityId)).toList()
            val hotelIdsInCity = hotelsInCity.mapNotNull { it.id?.toHexString() }
            queryForItemFilterMap.add(
                `in`(
                    "hotelTicketModel.hotel.id",
                    hotelIdsInCity.map { it })
            )
        }

        // Filter by number of adults, children, and rooms if not null
        if (filterByAdultsTicketNumber != null) {
            queryForItemFilterMap.add(
                eq(
                    "hotelTicketModel.numberOfAdultsAllowance",
                    filterByAdultsTicketNumber
                )
            )
        }
        if (filterByChildrenTicketNumber != null) {
            queryForItemFilterMap.add(
                eq(
                    "hotelTicketModel.numberOfChildrenAllowance",
                    filterByChildrenTicketNumber
                )
            )
        }
        if (filterByRoomsTicketNumber != null) {
            queryForItemFilterMap.add(
                gte(
                    "hotelTicketModel.numberOfRoomsPerNight",
                    filterByRoomsTicketNumber
                )
            )
        }
        // Final query
        val finalQueryMap =
            if (queryForItemFilterMap.isNotEmpty()) and(queryForItemFilterMap) else BsonDocument()

        // Fetch filtered PurchaseModel list
        val purchaseModels = purchaseModel.find(finalQueryMap).toList()


        val listOfPurchased = getLeftPurchaseAvailability(
            filterByDate,
            purchaseModels
        )

        val listOfTickets = getLeftTicketsAvailability(
            filterByDate,
            totalCount
        )

        val remainingRooms = calculateRemainingRooms(listOfTickets, listOfPurchased)

        val leftDates = filterDatesAsTimestamps(remainingRooms)

        return PagingApiResponse(
            succeeded = true,
            data = leftDates,
            errorCode = null
        )

    }

    fun filterDatesAsTimestamps(finalFilteredList: List<RoomAvailability>): List<Long> {
        // Define the date format
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        return finalFilteredList
            .filter { it.roomsLeft > 0 } // Filter out entries with roomsLeft <= 0
            .map {
                dateFormat.parse(it.dateString)?.time ?: 0L // Convert dateString to timestamp
            }
    }

    fun calculateRemainingRooms(
        finalFilteredList: List<RoomAvailability>,
        listOfPurchased: List<RoomAvailability>
    ): List<RoomAvailability> {
        // Create a mutable map from finalFilteredList for quick lookup by dateString
        val mainListMap = finalFilteredList.associateBy { it.dateString }.toMutableMap()

        // Iterate through the listOfPurchased
        for (purchase in listOfPurchased) {
            val dateString = purchase.dateString
            val purchasedRooms = purchase.roomsLeft

            // Check if the dateString exists in the main list
            val mainAvailability = mainListMap[dateString]
            if (mainAvailability != null) {
                // Subtract the purchased rooms from the available rooms
                val updatedRoomsLeft = mainAvailability.roomsLeft - purchasedRooms
                mainListMap[dateString] =
                    mainAvailability.copy(roomsLeft = updatedRoomsLeft.coerceAtLeast(0))
            }
        }
        val finalFilteredList = mainListMap.values.toList()

        // Return the updated list
        return finalFilteredList
    }


    fun compareTimestamps(list1: List<Long>, list2: List<RoomAvailability>): List<Long> {
        val result = mutableListOf<Long>()

        // Convert Long to LocalDate to compare year, month, and day only
        val datesList1 = list1.map {
            LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it), ZoneOffset.UTC)
                .toLocalDate()
        }

        val datesList2 = list2.map {
            LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it.date), ZoneOffset.UTC)
                .toLocalDate()
        }

        // Compare the dates in list1 against list2
        for (date1 in datesList1) {
            if (datesList2.none { it.year == date1.year && it.month == date1.month && it.dayOfMonth == date1.dayOfMonth }) {
                // Add the corresponding timestamp (Long) to the result
                val timestamp = datesList1[datesList1.indexOf(date1)]
                result.add(list1[datesList1.indexOf(date1)])
            }
        }

        // Compare the dates in list2 against list1
        for (date2 in datesList2) {
            if (datesList1.none { it.year == date2.year && it.month == date2.month && it.dayOfMonth == date2.dayOfMonth }) {
                // Add the corresponding timestamp (Long) to the result
                val timestamp = datesList2[datesList2.indexOf(date2)]
                result.add(list2[datesList2.indexOf(date2)].date)
            }
        }

        return result
    }


    private fun getLeftPurchaseAvailability(
        filterByDate: Long?,
        purchaseModels: List<PurchaseModel>,
    ): List<RoomAvailability> {
        val filteredPurchaseModelsList = purchaseModels.filter { purchase ->
            val checkInDate = purchase.checkInDate ?: 0L
            val checkOutDate = purchase.checkOutDate ?: 0L
            val filterDate = filterByDate ?: 0L

            // Get month and year for check-in, check-out, and filter date
            val (checkInMonth, checkInYear) = getMonthYear(checkInDate)
            val (checkOutMonth, checkOutYear) = getMonthYear(checkOutDate)
            val (filterMonth, filterYear) = getMonthYear(filterDate)

            // Check if either check-in or check-out date has the same month and year as the filter date
            (checkInMonth == filterMonth && checkInYear == filterYear) ||
                    (checkOutMonth == filterMonth && checkOutYear == filterYear)
        }

        val allRoomAvailability = mutableListOf<RoomAvailability>()

        for (purchase in filteredPurchaseModelsList) {
            val checkInDate = purchase.checkInDate ?: continue
            val checkOutDate = purchase.checkOutDate ?: continue
            val numberOfRooms = purchase.numberOfRooms
            val hotelTicketId = purchase.hotelTicketModel?.id ?: continue
            val checkInCheckOutDateString =
                "${checkInDate.toFormattedDateString()} - ${purchase.checkOutDate.toFormattedDateString()}"
            println()

            // Iterate through each day in the range
            for (date in checkInDate..checkOutDate step 24 * 60 * 60 * 1000L) {
                val currentStringDate = date.toFormattedDateString()
                allRoomAvailability.add(
                    RoomAvailability(
                        date = date,
                        roomsLeft = numberOfRooms,
                        hotelTicketId = hotelTicketId,
                        dateString = date.toFormattedDateString()
                    )
                )
            }
        }

        val dateToRoomAvailability = mutableMapOf<String, RoomAvailability>()

        for (room in allRoomAvailability) {
            // Check if the dateString already exists in the map
            if (dateToRoomAvailability.containsKey(room.dateString)) {
                // Update the total roomsLeft for the dateString
                val existingRoom = dateToRoomAvailability[room.dateString]!!
                dateToRoomAvailability[(room.dateString ?: "")] = existingRoom.copy(
                    roomsLeft = existingRoom.roomsLeft + room.roomsLeft
                )
            } else {
                // Add the entry if it doesn't exist
                dateToRoomAvailability[(room.dateString ?: "")] = room
            }
        }

        // Convert the map values to a list
        val finalFilteredList = dateToRoomAvailability.values.toList()
        return finalFilteredList
    }


    private fun getLeftTicketsAvailability(
        filterByDate: Long?,
        ticketModels: List<HotelTicketModel>
    ): List<RoomAvailability> {
        val filteredTicketModelsList = ticketModels.filter { purchase ->
            val checkInDate = purchase.fromDate ?: 0L
            val checkOutDate = purchase.toDate ?: 0L
            val filterDate = filterByDate ?: 0L

            // Get month and year for check-in, check-out, and filter date
            val (checkInMonth, checkInYear) = getMonthYear(checkInDate)
            val (checkOutMonth, checkOutYear) = getMonthYear(checkOutDate)
            val (filterMonth, filterYear) = getMonthYear(filterDate)

            // Check if either check-in or check-out date has the same month and year as the filter date
            (checkInMonth == filterMonth && checkInYear == filterYear) ||
                    (checkOutMonth == filterMonth && checkOutYear == filterYear)
        }

        val allRoomAvailability = mutableListOf<RoomAvailability>()

        for (purchase in filteredTicketModelsList) {
            val checkInDate = purchase.fromDate
            val checkOutDate = purchase.toDate
            val numberOfRooms = purchase.numberOfRoomsPerNight
            val hotelTicketId = purchase.id ?: continue
            val checkInCheckOutDateString =
                "${checkInDate.toFormattedDateString()} - ${checkOutDate.toFormattedDateString()}"
            println()
            // Iterate through each day in the range
            for (date in checkInDate..checkOutDate step 24 * 60 * 60 * 1000L) {
                val currentStringDate = date.toFormattedDateString()
                allRoomAvailability.add(
                    RoomAvailability(
                        date = date,
                        roomsLeft = numberOfRooms ?: -1,
                        hotelTicketId = hotelTicketId.toHexString(),
                        dateString = date.toFormattedDateString()
                    )
                )
            }
        }
        val dateToRoomAvailability = mutableMapOf<String, RoomAvailability>()
        for (room in allRoomAvailability) {
            // Check if the dateString already exists in the map
            if (dateToRoomAvailability.containsKey(room.dateString)) {
                // Update the total roomsLeft for the dateString
                val existingRoom = dateToRoomAvailability[room.dateString]!!
                dateToRoomAvailability[(room.dateString ?: "")] = existingRoom.copy(
                    roomsLeft = existingRoom.roomsLeft + room.roomsLeft
                )
            } else {
                // Add the entry if it doesn't exist
                dateToRoomAvailability[(room.dateString ?: "")] = room
            }
        }
        // Convert the map values to a list
        val finalFilteredList = dateToRoomAvailability.values.toList()
        return finalFilteredList
    }


    private fun getAllMonthFromPurchasedTicketsFiltration(
        filterByDate: Long?,
        purchaseModels: List<PurchaseModel>
    ): List<RoomAvailability> {
        val filteredList = purchaseModels.filter { purchase ->
            val checkInDate = purchase.checkInDate ?: 0L
            val checkOutDate = purchase.checkOutDate ?: 0L
            val filterDate = filterByDate ?: 0L

            // Get month and year for check-in, check-out, and filter date
            val (checkInMonth, checkInYear) = getMonthYear(checkInDate)
            val (checkOutMonth, checkOutYear) = getMonthYear(checkOutDate)
            val (filterMonth, filterYear) = getMonthYear(filterDate)

            // Check if either check-in or check-out date has the same month and year as the filter date
            (checkInMonth == filterMonth && checkInYear == filterYear) ||
                    (checkOutMonth == filterMonth && checkOutYear == filterYear)
        }

        val roomUsageMap = HashMap<String, MutableMap<Long, Int>>()

        filteredList.forEach { purchase ->
            val hotelTicket = purchase.hotelTicketModel ?: return@forEach
            val hotelTicketId = hotelTicket.id ?: return@forEach
            val checkInDate = purchase.checkInDate ?: return@forEach
            val checkOutDate = purchase.checkOutDate ?: return@forEach
            val numberOfRoomsPerNight = purchase.numberOfRooms ?: return@forEach

            // العدد الإجمالي للغرف يتم أخذه من الموديل
//            val totalRooms = numberOfRoomsPerNight

            // نحصل على التواريخ بين checkInDate و checkOutDate
            val dates = getDatesBetween(checkInDate, checkOutDate)

            // تحديث استخدام الغرف لكل تاريخ
            val usageMap = roomUsageMap.getOrPut(hotelTicketId) { mutableMapOf() }
            dates.forEach { date ->
                usageMap[date] = (usageMap[date] ?: 0) + (numberOfRoomsPerNight ?: 0)
            }
        }

        // إعداد قائمة التواريخ وعدد الغرف المتبقية
        val roomAvailabilityList = mutableListOf<RoomAvailability>()
        roomUsageMap.forEach { (hotelTicketId, usageMap) ->
            // الحصول على العدد الإجمالي للغرف من الموديل
            val totalRooms =
                purchaseModels.find { it.hotelTicketModel?.id == hotelTicketId }?.hotelTicketModel?.numberOfRoomsPerNight
                    ?: return@forEach

            usageMap.forEach { (date, roomsUsed) ->
                val roomsLeft = totalRooms - roomsUsed
//                if (roomsLeft == 0) {
                roomAvailabilityList.add(RoomAvailability(date, roomsLeft, hotelTicketId))
//                }
            }
        }

        return roomAvailabilityList
    }


    fun getDatesBetween(startDate: Long, endDate: Long): List<Long> {
        val dates = mutableListOf<Long>()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate

        while (calendar.timeInMillis <= endDate) {
            dates.add(calendar.timeInMillis)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return dates
    }

    fun getMonthYear(timestamp: Long): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        return Pair(month, year)
    }


    override suspend fun getAllByCityNameAndHotelName(
        searchText: String,
        pageSize: Int,
        pageNumber: Int,
        xAppLanguageId: Int
    ): PagingApiResponse<List<ResponseHotelModel>?> {

        val skip = (pageNumber - 1) * pageSize
        val query = mutableListOf<Bson>()

        // If the search text is not empty, search by city name or country codes
        if (searchText.isNotEmpty()) {
            // Search by city name (supports partial matches)
            query.add(
                CityModel::profiles.elemMatch(
                    CityProfileModel::name regex Regex(
                        ".*${Regex.escape(searchText)}.*",
                        RegexOption.IGNORE_CASE
                    )
                )
            )

            // Search by two-digit country code
            query.add(
                CityModel::twoDigitCountryCode regex Regex(
                    "^${Regex.escape(searchText)}$",
                    RegexOption.IGNORE_CASE
                )
            )

            // Search by three-digit country code
            query.add(
                CityModel::threeDigitCountryCode regex Regex(
                    "^${Regex.escape(searchText)}$",
                    RegexOption.IGNORE_CASE
                )
            )
        }

        val finalQuery = or(query)
        val totalCount = cities.countDocuments(finalQuery).toInt()
        val totalPages = if (totalCount % (if (pageSize == 0) 1 else pageSize) == 0)
            totalCount / (if (pageSize == 0) 1 else pageSize)
        else (totalCount / (if (pageSize == 0) 1 else pageSize)) + 1

        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages

        // Fetch city data matching the query
        val cityQuery = cities.find(finalQuery)
            .skip(skip)
            .limit(pageSize)
            .toList()

        val cityResponse = cityQuery.map { cityModel ->
            ResponseHotelModel(
                id = "",
                name = "",
                profiles = null,
                logo = "",
                longitude = 0.0,
                latitude = 0.0,
                stars = 0.0,
                city = cityModel.toResponseCityModel(
                    xAppLanguageId,
                    cityModel.id?.toHexString() ?: ""
                )
            )
        }

        return PagingApiResponse(
            succeeded = true,
            data = cityResponse,
            currentPage = pageNumber,
            totalPages = totalPages,
            totalCount = totalCount,
            hasPreviousPage = hasPreviousPage,
            hasNextPage = hasNextPage,
            errorCode = null
        )
    }


    override suspend fun getAllByCityNameAndAirportsName(
        searchText: String,
        pageSize: Int,
        pageNumber: Int,
        xAppLanguageId: Int
    ): PagingApiResponse<List<ResponseCityModel>?> {

        val skip = (pageNumber - 1) * pageSize
        val query = mutableListOf<Bson>()

        // If the search text is not empty, search by hotel or city name
        if (searchText.isNotEmpty()) {

            query.add(
                CityModel::profiles.elemMatch(
                    CityProfileModel::name regex Regex(
                        ".*${Regex.escape(searchText)}.*",
                        RegexOption.IGNORE_CASE
                    )
                )
            )

            // Search by two-digit country code
            query.add(
                CityModel::twoDigitCountryCode regex Regex(
                    "^${Regex.escape(searchText)}$",
                    RegexOption.IGNORE_CASE
                )
            )

            // Search by three-digit country code
            query.add(
                CityModel::threeDigitCountryCode regex Regex(
                    "^${Regex.escape(searchText)}$",
                    RegexOption.IGNORE_CASE
                )
            )

        }

        val finalQuery = or(query)
        val totalCount = cities.countDocuments(finalQuery).toInt()
        val totalPages = if (totalCount % (if (pageSize == 0) 1 else pageSize) == 0)
            totalCount / (if (pageSize == 0) 1 else pageSize)
        else (totalCount / (if (pageSize == 0) 1 else pageSize)) + 1

        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages

        return PagingApiResponse(
            succeeded = true,
            data = cities.find(finalQuery)
                .skip(skip)
                .limit(pageSize)
                .toList().mapNotNull { hotelModel ->
                    val matchingProfile =
                        hotelModel.profiles.find { it.languageId == xAppLanguageId }
                    matchingProfile?.let {
                        ResponseCityModel(
                            id = hotelModel.id?.toHexString() ?: "",
                            name = it.name,
                            profiles = hotelModel.profiles,
                            countryName = it.countryName,
                            twoDigitCountryCode = hotelModel.twoDigitCountryCode,
                            threeDigitCountryCode = hotelModel.threeDigitCountryCode,
                        )
                    }
                },
            currentPage = pageNumber,
            totalPages = totalPages,
            totalCount = totalCount,
            hasPreviousPage = hasPreviousPage,
            hasNextPage = hasNextPage,
            errorCode = errorCode
        )
    }


    fun getDateAsTimestamp(timestamp: Long): Long {
        val zoneId = ZoneId.of("Asia/Amman") // Change this if needed, e.g., ZoneId.of("GMT+2")
        val localDate = Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
        return localDate.atStartOfDay(zoneId).toInstant().toEpochMilli()

    }

}

