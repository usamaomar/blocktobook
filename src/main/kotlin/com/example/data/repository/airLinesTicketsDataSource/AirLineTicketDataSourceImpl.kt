package com.example.data.repository.airLinesTicketsDataSource

import com.example.data.repository.sendGrid.SendGridDataSource
import com.example.domain.model.airlinesModel.AirLineModel
import com.example.domain.model.airlinesModel.toResponseAirLineModel
import com.example.domain.model.airlinesTicketModel.AirlineTicketModel
import com.example.domain.model.airlinesTicketModel.CreateAirlineTicketModel
import com.example.domain.model.airlinesTicketModel.ResponseAirlineTicketModel
import com.example.domain.model.airlinesTicketModel.UpdateAirlineTicketModel
import com.example.domain.model.airlinesTicketModel.toResponseAirlineTicketModel
import com.example.domain.model.airportsModel.AirPortModel
import com.example.domain.model.airportsModel.ResponseAirPortModel
import com.example.domain.model.airportsModel.toResponseAirPortModel
import com.example.domain.model.cityModel.CityModel
import com.example.domain.model.cityModel.toResponseCityModel
import com.example.domain.model.hotelModel.HotelModel
import com.example.domain.model.hotelModel.HotelProfileModel
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.purchaseModel.PurchaseModel
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.client.model.Updates.combine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.div
import org.litote.kmongo.elemMatch
import org.litote.kmongo.eq
import org.litote.kmongo.regex
import org.litote.kmongo.setValue
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

class AirLineTicketDataSourceImpl(database: CoroutineDatabase) : AirLineTicketDataSource {
    val sendGridDataSource: SendGridDataSource by KoinJavaComponent.inject(SendGridDataSource::class.java)
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
                    flightNumber = info.flightNumberRoundTrip,
                    pricePerInfantRoundTrip = info.pricePerInfantRoundTrip,
                    pricePerInfant = info.pricePerInfant,
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
                flightNumber = info.flightNumber,
                pricePerInfantRoundTrip = info.pricePerInfantRoundTrip,
                pricePerInfant = info.pricePerInfant,
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
        val airlineTicketModel = purchaseModel.findOne(
            Filters.eq(
                "airLineModel.id",
                updateAirlineTicketModel.id
            )
        )

        val ticketModel = airLinesTickets.findOne(Filters.eq("_id", ObjectId(updateAirlineTicketModel.id)))
            ?: return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Not Correct Ticket to update"),
                errorCode = errorCode
            )

        val returnAirlineTicketModel = purchaseModel.findOne(
            Filters.eq(
                "returnAirLineModel.id",
                updateAirlineTicketModel.id
            )
        )
        if(airlineTicketModel ==null && returnAirlineTicketModel == null && ticketModel.userId == userId){
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
                    "flightNumber",
                    updateAirlineTicketModel.flightNumber ?: ticketModel.flightNumber
                ),
                Updates.set(
                    "pricePerInfant",
                    if(updateAirlineTicketModel.numberOfChildren == 0)  0.0 else (updateAirlineTicketModel.pricePerInfant ?: ticketModel.pricePerInfant)
                ),
                Updates.set(
                    "pricePerInfantRoundTrip",
                    if(updateAirlineTicketModel.numberOfChildren == 0)  0.0 else (updateAirlineTicketModel.pricePerInfantRoundTrip ?: ticketModel.pricePerInfantRoundTrip)
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
            val updateResult = airLinesTickets.updateOne(Filters.eq("_id", ObjectId(updateAirlineTicketModel.id)), update)
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
               return if (updateResult.matchedCount > 0) {
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
        }else if(ticketModel.userId == userId){
            updateTimeOnPurchasedAndAirlineTickets(userId,updateAirlineTicketModel.id,  updateAirlineTicketModel.departureTime,   updateAirlineTicketModel.arrivalTime)
            updateVisibilityOnAirlineTickets(updateAirlineTicketModel.id,  updateAirlineTicketModel.isVisible ?: true)
            return  ApiResponse(data = "Success", succeeded = true, errorCode = errorCode)
        }
        return  ApiResponse(
            data = null,
            succeeded = false,
            message = arrayListOf("User Not Found"),
            errorCode = errorCode
        )
    }



    private suspend fun updateVisibilityOnAirlineTickets(ticketId: String, visibility: Boolean){
        val filter = Filters.eq("_id", ObjectId(ticketId))
        val ticketModel = airLinesTickets.findOne(filter)
        if(ticketModel?.isVisible!= visibility){
            airLinesTickets.updateOne(filter, Updates.combine(
                Updates.set(
                    "isVisible",
                    visibility
                ),
            ))
        }
    }

  private suspend fun updateTimeOnPurchasedAndAirlineTickets( userId: String,ticketId: String, departureTime : String?, arrivalTime: String?){
      val filter = Filters.eq("_id", ObjectId(ticketId))
      val ticketModel = airLinesTickets.findOne(filter)
      if(ticketModel?.departureTime!= departureTime || ticketModel?.arrivalTime!= arrivalTime){
           // send email

          val value = purchaseModel.updateMany(
              PurchaseModel::returnAirLineModel / ResponseAirlineTicketModel::id eq ticketId, // Correctly filters by nested field
              combine(
                  setValue(PurchaseModel::returnAirLineModel / ResponseAirlineTicketModel::departureTime, departureTime),
                  setValue(PurchaseModel::returnAirLineModel / ResponseAirlineTicketModel::arrivalTime, arrivalTime)
              )
          )


          val value2 = purchaseModel.updateMany(
              PurchaseModel::airLineModel / ResponseAirlineTicketModel::id eq ticketId, // Correctly filters by nested field
              combine(
                  setValue(PurchaseModel::airLineModel / ResponseAirlineTicketModel::departureTime, departureTime),
                  setValue(PurchaseModel::airLineModel / ResponseAirlineTicketModel::arrivalTime, arrivalTime)
              )
          )

          if(value2.modifiedCount > 0 || value.modifiedCount > 0){
              println()
          }
          airLinesTickets.updateOne(filter, Updates.combine(
              Updates.set(
                  "departureTime",
                  departureTime
              ),
              Updates.set(
                  "arrivalTime",
                  arrivalTime
              ),
          ))
          sendGridDataSource.notifyMerchantsAboutTravelUpdate(userId, ticketId, departureTime ?: "", arrivalTime ?: "")
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

        // Build the main query filter
        val queryFilters = mutableListOf<Bson>().apply {
            add(Filters.eq("userId", userId)) // Filter by user ID

            // Search filter
            if (searchText.isNotEmpty()) {
                val foundHotelId = airLinesTickets
                    .findOne(HotelModel::profiles.elemMatch(HotelProfileModel::name regex searchText))
                    ?.id?.toHexString()
                if (foundHotelId != null) add(Filters.eq("hotelId", foundHotelId))
            }

            // Price Range Filter
            filterByPriceRangeFrom?.let { add(Filters.gte("price", it)) }
            filterByPriceRangeTo?.let { add(Filters.lte("price", it)) }

            // Airline IDs Filter
            filterByAirLineIds?.takeIf { it.isNotEmpty() }?.let {
                add(Filters.`in`("airLineIds", it))
            }

            // Date Range Filter
            when {
                filterByDateFrom != null && filterByDateTo != null -> add(
                    Filters.and(
                        Filters.gte("reservationDate", filterByDateFrom),
                        Filters.lte("checkOutDate", filterByDateTo)
                    )
                )
                filterByDateFrom != null -> add(Filters.gte("reservationDate", filterByDateFrom))
                filterByDateTo != null -> add(Filters.lte("checkOutDate", filterByDateTo))
            }
        }

        // Prepare the query and pagination
        val finalQuery = and(queryFilters)
        val totalCount = airLinesTickets.countDocuments(finalQuery).toInt()
        val totalPages = if (pageSize == 0) 1 else (totalCount + pageSize - 1) / pageSize
        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages

        // Fetch tickets with pagination
        val tickets = withContext(Dispatchers.IO) {
            airLinesTickets.find(finalQuery)
                .skip(skip)
                .limit(pageSize)
                .toList()
        }

        // Batch fetch related data
        val cityIds = tickets.mapNotNull { it.departureCityId } + tickets.mapNotNull { it.arrivalCityId }
//        val airportIds = tickets.mapNotNull { it.departureAirportId } + tickets.mapNotNull { it.arrivalAirportId }
//        val airlineIds = tickets.mapNotNull { it.airLineId }

        // Fetch related data in parallel
        val cities = withContext(Dispatchers.IO) {
            citiesdatabase.find(Filters.`in`("_id", cityIds.map(::ObjectId))).toList()
        }


        // Map tickets to response models
        val data = tickets.map { ticket ->
            val departureCity = cities.find { it.id?.toHexString() == ticket.departureCityId }
            val arrivalCity = cities.find { it.id?.toHexString() == ticket.arrivalCityId }
//            val departureAirport = airports.find { it.id?.toHexString() == ticket.departureAirportId }
//            val arrivalAirport = airports.find { it.id?.toHexString() == ticket.arrivalAirportId }
//            val airline = airlines.find { it.id?.toHexString() == ticket.airLineId }

            ticket.toResponseAirlineTicketModel(
                ticket.id?.toHexString() ?: "",
                departureCity?.toResponseCityModel(xAppLanguageId, ""),
                arrivalCity?.toResponseCityModel(xAppLanguageId, ""),
                null,
                null,
                null,
                null,
                getTotalNumberOfRoomsForUser(userId, ticket.id?.toHexString(), ticket.numberOfSeats ?: 0),
                getTotalNumberOfRoomsForUser(userId, ticket.id?.toHexString(), ticket.numberOfSeats ?: 0),
            )
        }

        return PagingApiResponse(
            succeeded = true,
            data = data,
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

        val totalTwo = getTotalNumberOfRetrunRoomsForUser(userId, id, totalRooms)

        return  totalRooms - (totalTwo + totalPurchased)
    }

    suspend fun getTotalNumberOfRetrunRoomsForUser(userId: String?,id: String?, totalRooms: Int): Int {
        // Create a query filter to find all documents with the matching userId under airLineModel
        val queryForItemFilter = mutableListOf<Bson>()
        queryForItemFilter.add(Filters.eq("returnAirLineModel.userId", userId))
        queryForItemFilter.add(Filters.eq("returnAirLineModel.id", id))

        // Combine all filters into one final query
        val finalQuery = and(queryForItemFilter)

        // Fetch the documents that match the query
        val documents = purchaseModel.find(finalQuery).toList()

        // Sum the numberOfRooms field from all matching documents
        val totalPurchased  = documents.sumOf { it.numberOfRooms }


        return totalPurchased
    }




      fun generateUniqueToken(length: Int = 24): String {
        val characters = "4039fkeoidfwm0ef90329mifwe2039"
        val secureRandom = SecureRandom()
        return (1..length).map { characters.random(secureRandom.asKotlinRandom()) }.joinToString("")
    }

}