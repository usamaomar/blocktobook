package com.example.data.repository.airLinesTicketsDataSource

import com.example.domain.model.airlinesModel.AirLineModel
import com.example.domain.model.airlinesModel.toResponseAirLineModel
import com.example.domain.model.airlinesTicketModel.AirlineTicketModel
import com.example.domain.model.airlinesTicketModel.CreateAirlineTicketModel
import com.example.domain.model.airlinesTicketModel.ResponseAirlineTicketModel
import com.example.domain.model.airlinesTicketModel.UpdateAirlineTicketModel
import com.example.domain.model.airlinesTicketModel.toResponseAirlineTicketModel
import com.example.domain.model.airportsModel.AirPortModel
import com.example.domain.model.airportsModel.toResponseAirPortModel
import com.example.domain.model.cityModel.CityModel
import com.example.domain.model.cityModel.toResponseCityModel
import com.example.domain.model.hotelModel.HotelModel
import com.example.domain.model.hotelModel.HotelProfileModel
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.purchaseModel.PurchaseModel
import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.aggregate
import org.litote.kmongo.elemMatch
import org.litote.kmongo.regex
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

class AirLineTicketDataSourceImpl(database: CoroutineDatabase) : AirLineTicketDataSource {

    private val airLinesTickets = database.getCollection<AirlineTicketModel>()
    private val citiesdatabase = database.getCollection<CityModel>()
    private val airPortsdatabase = database.getCollection<AirPortModel>()
    private val airLinesdatabase = database.getCollection<AirLineModel>()
    private val purchaseModel = database.getCollection<PurchaseModel>()

    private val errorCode: Int = 266

    override suspend fun post(
        userId: String,
        airLineModel: CreateAirlineTicketModel
    ): ApiResponse<String?> {
        val airlineTicketsToInsert = mutableListOf<AirlineTicketModel>()
        var roundTripId: String? = null
        if (airLineModel.isRoundTrip == true) {
            roundTripId = generateUniqueToken()
            for (info in airLineModel.airlineTicketInfoList) {
                // Create a HotelTicketModel from the current info
                val hotelTicket = AirlineTicketModel(
                    userId = userId,
                    departureCityId = airLineModel.arrivalCityId,
                    arrivalCityId = airLineModel.departureCityId,
                    roundTripId = roundTripId,
                    departureAirportId = airLineModel.arrivalAirportId,
                    arrivalAirportId = airLineModel.departureAirportId,
                    airLineId = airLineModel.returnAirLineId ?: "",
                    departureDate = airLineModel.roundTripDepartureDate ?: "00-00-0000",
                    arrivalDate = airLineModel.roundTripArrivalDate ?: "00-00-0000",
                    departureTime = airLineModel.roundTripDepartureTime ?: "00-00-0000",
                    arrivalTime = airLineModel.roundTripArrivalTime ?: "00-00-0000",
                    isRoundTrip = airLineModel.isRoundTrip,
                    travelClass = info.travelClass,
                    pricePerSeat = info.pricePerSeat,
                    pricePerSeatRoundTrip = info.pricePerSeatRoundTrip,
                    numberOfSeats = info.numberOfSeats,
                    numberOfChildren = info.numberOfChildren,
                    totalAllowances = info.totalAllowances,
                    childAge = info.childAge,
                    isVisible = info.isVisible,
                )
                airlineTicketsToInsert.add(hotelTicket)
            }
        }

        for (info in airLineModel.airlineTicketInfoList) {
            // Create a HotelTicketModel from the current info
            val hotelTicket = AirlineTicketModel(
                userId = userId,
                departureCityId = airLineModel.departureCityId,
                arrivalCityId = airLineModel.arrivalCityId,
                roundTripId = roundTripId,
                departureAirportId = airLineModel.departureAirportId,
                arrivalAirportId = airLineModel.arrivalAirportId,
                airLineId = airLineModel.airLineId,
                departureDate = airLineModel.departureDate,
                arrivalDate = airLineModel.arrivalDate,
                departureTime = airLineModel.departureTime,
                arrivalTime = airLineModel.arrivalTime,
                isRoundTrip = airLineModel.isRoundTrip,
                travelClass = info.travelClass,
                pricePerSeat = info.pricePerSeat,
                pricePerSeatRoundTrip = info.pricePerSeatRoundTrip,
                numberOfSeats = info.numberOfSeats,
                numberOfChildren = info.numberOfChildren,
                totalAllowances = info.totalAllowances,
                childAge = info.childAge,
                isVisible = info.isVisible,
            )
            airlineTicketsToInsert.add(hotelTicket)
        }
        val insertResults = airLinesTickets.insertMany(airlineTicketsToInsert)
        if (insertResults.wasAcknowledged()) {
            return ApiResponse(data = "Success", succeeded = true, errorCode = errorCode)
        } else {
            return ApiResponse(data = "Fail", succeeded = false, errorCode = errorCode)
        }
    }


    override suspend fun getById(id: String): ApiResponse<ResponseAirlineTicketModel?> {
        val filter = Filters.eq("_id", ObjectId(id))
        if (airLinesTickets.findOne(filter) == null) {
            return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Ticket not found"),
                errorCode = errorCode
            )
        }
        val cityModel = airLinesTickets.findOne(filter)
        return ApiResponse(data = null, succeeded = true, errorCode = errorCode)
    }

    override suspend fun put(
        userId: String,
        updateAirlineTicketModel: UpdateAirlineTicketModel
    ): ApiResponse<String?> {
        val filter = Filters.eq("_id", ObjectId(updateAirlineTicketModel.id))
        val ticketModel = airLinesTickets.findOne(filter)
            ?: return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Not Correct Ticket to update"),
                errorCode = errorCode
            )

        if (ticketModel.userId != userId) {
            return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Not Authorized to update"),
                errorCode = errorCode
            )
        }

        val update = Updates.combine(
            Updates.set(
                "departureCityId",
                if (updateAirlineTicketModel.departureCityId.isNullOrBlank()) ticketModel.departureCityId else updateAirlineTicketModel.departureCityId
            ),
            Updates.set(
                "arrivalCityId",
                if (updateAirlineTicketModel.arrivalCityId.isNullOrBlank()) ticketModel.arrivalCityId else updateAirlineTicketModel.arrivalCityId
            ),
            Updates.set(
                "departureAirportId",
                if (updateAirlineTicketModel.departureAirportId.isNullOrBlank()) ticketModel.departureAirportId else updateAirlineTicketModel.departureAirportId
            ),
            Updates.set(
                "arrivalAirportId",
                if (updateAirlineTicketModel.arrivalAirportId.isNullOrBlank()) ticketModel.arrivalAirportId else updateAirlineTicketModel.arrivalAirportId
            ),
            Updates.set(
                "airLineId",
                if (updateAirlineTicketModel.airLineId.isNullOrBlank()) ticketModel.airLineId else updateAirlineTicketModel.airLineId
            ),
            Updates.set(
                "departureDate",
                if (updateAirlineTicketModel.departureDate.isNullOrBlank()) ticketModel.departureDate else updateAirlineTicketModel.departureDate
            ),
            Updates.set(
                "arrivalDate",
                if (updateAirlineTicketModel.arrivalDate.isNullOrBlank()) ticketModel.arrivalDate else updateAirlineTicketModel.arrivalDate
            ),
            Updates.set(
                "departureTime",
                if (updateAirlineTicketModel.departureTime.isNullOrBlank()) ticketModel.departureTime else updateAirlineTicketModel.departureTime
            ),
            Updates.set(
                "arrivalTime",
                if (updateAirlineTicketModel.arrivalTime.isNullOrBlank()) ticketModel.arrivalTime else updateAirlineTicketModel.arrivalTime
            ),
            Updates.set(
                "travelClass",
                if (updateAirlineTicketModel.travelClass == 0) ticketModel.travelClass else updateAirlineTicketModel.travelClass
            ),
            Updates.set(
                "pricePerSeat",
                if (updateAirlineTicketModel.pricePerSeat == 0.0) ticketModel.pricePerSeat else updateAirlineTicketModel.pricePerSeat
            ),
            Updates.set(
                "pricePerSeatRoundTrip",
                updateAirlineTicketModel.pricePerSeatRoundTrip ?: ticketModel.pricePerSeatRoundTrip
            ),
            Updates.set(
                "numberOfSeats",
                updateAirlineTicketModel.numberOfSeats ?: ticketModel.numberOfSeats
            ),
            Updates.set(
                "numberOfChildren",
                updateAirlineTicketModel.numberOfChildren ?: ticketModel.numberOfChildren
            ),
            Updates.set(
                "totalAllowances",
                updateAirlineTicketModel.totalAllowances ?: ticketModel.totalAllowances
            ),
            Updates.set(
                "childAge",
                updateAirlineTicketModel.childAge ?: ticketModel.childAge
            ),
            Updates.set(
                "isVisible",
                updateAirlineTicketModel.isVisible ?: ticketModel.isVisible
            ),
            Updates.set(
                "userId",
                userId
            )
        )
        val updateResult = airLinesTickets.updateOne(filter, update)
//        roundTrip
        if(ticketModel.roundTripId !=null) {
            val filterRound = Filters.and(
                Filters.eq("roundTripId", ticketModel.roundTripId),
                Filters.ne("_id", ObjectId(updateAirlineTicketModel.id))
            )

            val ticketModelRound = airLinesTickets.findOne(filterRound)
            val updateRound = Updates.combine(
                Updates.set(
                    "pricePerSeatRoundTrip",
                    updateAirlineTicketModel.pricePerSeatRoundTrip
                        ?: ticketModelRound?.pricePerSeatRoundTrip
                )
            )
            val updateResultRound = airLinesTickets.updateOne(filterRound, updateRound)
        }
        return  if (updateResult.matchedCount > 0) {
            ApiResponse(data = "Success", succeeded = true, errorCode = errorCode)
        } else {
            ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("AirLineTicket not found"),
                errorCode = errorCode
            )
        }
    }


    override suspend fun getAll(
        userId: String,
        searchText: String,
        pageSize: Int,
        pageNumber: Int,
        xAppLanguageId: Int,
        filterByDateFrom: Long?,
        filterByDateTo: Long?,
        filterByVisibility: Boolean?,
        filterByPriceRangeFrom: Double?,
        filterByPriceRangeTo: Double?,
        filterByAirLineIds: List<String>?,
    ): PagingApiResponse<List<ResponseAirlineTicketModel>?> {
        val skip = (pageNumber - 1) * pageSize
        val queryForSearchFilter = mutableListOf<Bson>()
        val queryForItemFilter = mutableListOf<Bson>()
        if (searchText.isNotEmpty()) {
            queryForSearchFilter.add(
                HotelModel::profiles.elemMatch(
                    HotelProfileModel::name regex searchText
                )
            )
            val finalQueryForSearchFilter = and(queryForSearchFilter)
            val foundHotel = airLinesTickets.findOne(finalQueryForSearchFilter)
            queryForItemFilter.add(
                Filters.eq("hotelId", foundHotel?.id?.toHexString())
            )
        }

        queryForItemFilter.add(
            Filters.eq("userId", userId)
        )

        if (filterByPriceRangeFrom != null && filterByPriceRangeTo != null) {
            queryForItemFilter.add(
                and(
                    Filters.gte("price", filterByPriceRangeFrom),
                    Filters.lte("price", filterByPriceRangeTo)
                )
            )
        } else if (filterByPriceRangeFrom != null) {
            queryForItemFilter.add(
                Filters.gte("price", filterByPriceRangeFrom)
            )
        } else if (filterByPriceRangeTo != null) {
            queryForItemFilter.add(
                Filters.lte("price", filterByPriceRangeTo)
            )
        }

        if (!filterByAirLineIds.isNullOrEmpty()) {
            queryForItemFilter.add(
                Filters.`in`("airLineIds", filterByAirLineIds)
            )
        }

        if (filterByDateFrom != null && filterByDateTo != null) {
            queryForItemFilter.add(
                and(
                    Filters.gte("reservationDate", filterByDateFrom),
                    Filters.lte("checkOutDate", filterByDateTo)
                )
            )
        } else if (filterByDateFrom != null) {
            queryForItemFilter.add(
                Filters.gte("reservationDate", filterByDateFrom)
            )
        } else if (filterByDateTo != null) {
            queryForItemFilter.add(
                Filters.lte("checkOutDate", filterByDateTo)
            )
        }

        if (filterByVisibility != null) {
            queryForItemFilter.add(
                Filters.eq("isVisible", filterByVisibility)
            )
        }


        val queryForPurchasedModels = mutableListOf<Bson>()
        queryForPurchasedModels.add(Filters.eq("airLineModel.userId", userId))



        val finalQuery = and(queryForItemFilter)
        val totalCount = airLinesTickets.countDocuments(finalQuery).toInt()
        val totalPages =
            if (totalCount % (if (pageSize == 0) 1 else pageSize) == 0) totalCount / (if (pageSize == 0) 1 else pageSize) else (totalCount / (if (pageSize == 0) 1 else pageSize)) + 1
        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages
        return PagingApiResponse(
            succeeded = true,
            data = airLinesTickets.find(finalQuery)
                .skip(skip)
                .limit(pageSize)
                .toList().map { hotelTicketModel ->
                    hotelTicketModel.toResponseAirlineTicketModel(
                        hotelTicketModel.id?.toHexString() ?: "",
                        departureCity = citiesdatabase.findOne(Filters.eq("_id", ObjectId(hotelTicketModel.departureCityId)))?.toResponseCityModel(xAppLanguageId,   "") ,
                        arrivalCity = citiesdatabase.findOne(Filters.eq("_id", ObjectId(hotelTicketModel.arrivalCityId)))?.toResponseCityModel(xAppLanguageId,   ""),
                        departureAirport = airPortsdatabase.findOne(Filters.eq("_id", ObjectId(hotelTicketModel.departureAirportId)))?.toResponseAirPortModel(),
                        arrivalAirport = airPortsdatabase.findOne(Filters.eq("_id", ObjectId(hotelTicketModel.arrivalAirportId)))?.toResponseAirPortModel(),
                        airLine = airLinesdatabase.findOne(Filters.eq("_id", ObjectId(hotelTicketModel.airLineId)))?.toResponseAirLineModel(),
                        returnAirLine = airLinesdatabase.findOne(Filters.eq("_id", ObjectId(hotelTicketModel.airLineId)))?.toResponseAirLineModel(),
                        numberOfSeatsLeft = getTotalNumberOfRoomsForUser(userId ,hotelTicketModel.id?.toHexString(), hotelTicketModel.numberOfSeats?:0)
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

    suspend fun getTotalNumberOfRoomsForUser(userId: String?,id: String?, totalRooms: Int): Int {
        // Create a query filter to find all documents with the matching userId under airLineModel
        val queryForItemFilter = mutableListOf<Bson>()
        queryForItemFilter.add(Filters.eq("airLineModel.userId", userId))
        queryForItemFilter.add(Filters.eq("airLineModel.id", id))

        // Combine all filters into one final query
        val finalQuery = and(queryForItemFilter)

        // Fetch the documents that match the query
        val documents = purchaseModel.find(finalQuery).toList()

        // Sum the numberOfRooms field from all matching documents
        val totalPurchased  = documents.sumOf { it.numberOfRooms }


        return totalRooms - totalPurchased
    }

    private suspend fun calculateAvailableRooms(hotelTicketId: ObjectId?, totalRooms: Int?): Int {
        // Return 0 if either the hotel ticket ID or total rooms is null
        if (hotelTicketId == null || totalRooms == null) {
            return 0 // No valid data to calculate available rooms
        }

        // Aggregate query to calculate the total number of rooms purchased for the given hotel ticket ID
        val purchasedRooms = purchaseModel.aggregate<PurchaseModel>(
            listOf(
                Filters.eq("airLineModel.id", hotelTicketId.toHexString()),
                Aggregates.group(null, Accumulators.sum("numberOfSeats", "\$numberOfRooms"))
            )
        ).first()?.numberOfRooms ?: 0
        // Return the number of available rooms by subtracting purchased rooms from total rooms
        return totalRooms - purchasedRooms
    }


    public fun generateUniqueToken(length: Int = 24): String {
        val characters = "4039fkeoidfwm0ef90329mifwe2039"
        val secureRandom = SecureRandom()
        return (1..length).map { characters.random(secureRandom.asKotlinRandom()) }.joinToString("")
    }

}