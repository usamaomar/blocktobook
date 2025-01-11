package com.example.data.repository.hotelTicketsDataSource

import com.example.domain.model.cityModel.CityModel
import com.example.domain.model.cityModel.toResponseCityModel
import com.example.domain.model.hotelModel.HotelModel
import com.example.domain.model.hotelModel.HotelProfileModel
import com.example.domain.model.hotelModel.toResponseHotelModel
import com.example.domain.model.hotelTicketModel.CreateHotelTicketModel
import com.example.domain.model.hotelTicketModel.HotelTicketModel
import com.example.domain.model.hotelTicketModel.ResponseHotelTicketModel
import com.example.domain.model.hotelTicketModel.UpdateHotelTicketModel
import com.example.domain.model.hotelTicketModel.toResponseHotelTicketWithHotelModel
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.util.isNullOrBlank
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.gte
import com.mongodb.client.model.Filters.`in`
import com.mongodb.client.model.Filters.lte
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.elemMatch
import org.litote.kmongo.regex

class HotelTicketDataSourceImpl(database: CoroutineDatabase) : HotelTicketDataSource {

    private val hotels = database.getCollection<HotelModel>()
    private val hotelTickets = database.getCollection<HotelTicketModel>()
    private val cities = database.getCollection<CityModel>()

    private val errorCode: Int = 266

    override suspend fun post(
        userId: String,
        hotelTicketModel: CreateHotelTicketModel
    ): ApiResponse<String?> {
        val existingHotel = hotels.findOne(
            eq("_id", ObjectId(hotelTicketModel.hotelId))
        )

        if (existingHotel == null) {
            return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Hotel Not exists"),
                errorCode = errorCode
            )
        }
        // List to hold valid HotelTicketModel objects
        val hotelTicketsToInsert = mutableListOf<HotelTicketModel>()

        // Iterate over hotelTicketInfoCreateModel to validate and add to the list
        for (info in hotelTicketModel.hotelTicketInfoList) {
            // Check roomClass
            if (info.roomClass < 0 || info.roomClass > 4) {
                return ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("Room Class not found"),
                    errorCode = errorCode
                )
            }

            // Check transportation
            if (info.transportation != null) {
                if (info.transportation < 0 || info.transportation > 3) {
                    return ApiResponse(
                        data = null,
                        succeeded = false,
                        message = arrayListOf("Transportation not found"),
                        errorCode = errorCode
                    )
                }
            }

            // Create a HotelTicketModel from the current info
            val hotelTicket = HotelTicketModel(
                hotelId = hotelTicketModel.hotelId,
                ticketNumber = null, // Or generate a ticket number if needed
                pricePerNight = info.pricePerNight,
                fromDate = hotelTicketModel.fromDate,
                toDate = hotelTicketModel.toDate,
                roomClass = info.roomClass,
                roomCategory = info.roomCategory,
                transportation = info.transportation,
                userId = userId,
                isVisible = info.isVisible,
                numberOfRoomsPerNight = info.numberOfRoomsPerNight,
                numberOfAdultsAllowance = info.numberOfAdultsAllowance,
                numberOfChildrenAllowance = info.numberOfChildrenAllowance,
                childrenAge = info.childrenAge
            )

            hotelTicketsToInsert.add(hotelTicket)
        }

        // Insert all valid HotelTicketModel objects into the database
        val insertResults = hotelTickets.insertMany(hotelTicketsToInsert)
        if (insertResults.wasAcknowledged()) {
            return ApiResponse(data = "Success", succeeded = true, errorCode = errorCode)
        } else {
            return ApiResponse(data = "Fail", succeeded = false, errorCode = errorCode)
        }
    }

    override suspend fun put(
        userId: String,
        updateHotelTicketModel: UpdateHotelTicketModel
    ): ApiResponse<HotelTicketModel?> {
        if (updateHotelTicketModel.hotelId != null) {
            val existingHotel = hotels.findOne(
                eq(
                    "_id", ObjectId(updateHotelTicketModel.hotelId)
                )
            )
            if (existingHotel == null) {
                return ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("Hotel Not exists"), errorCode = errorCode
                )
            }
        }
        if (updateHotelTicketModel.transportation != null) {
            if (updateHotelTicketModel.transportation < 0 || updateHotelTicketModel.transportation > 3) {
                return ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("transportation not found"),
                    errorCode = errorCode
                )
            }
        }
        if (updateHotelTicketModel.roomClass < 0 || updateHotelTicketModel.roomClass > 4) {
            return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Room Class not found"),
                errorCode = errorCode
            )
        }

        val filter = eq("_id", ObjectId(updateHotelTicketModel.id))
        val ticketModel = hotelTickets.findOne(filter)
            ?: return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Hotel Ticket not found"),
                errorCode = errorCode
            )

        if(ticketModel.userId != userId){
            return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("You are not allowed to update this ticket"),
                errorCode = errorCode)
        }
        val update = Updates.combine(
            Updates.set(
                "hotelId",
                if (updateHotelTicketModel.hotelId.isNullOrBlank()) ticketModel.hotelId else updateHotelTicketModel.hotelId
            ),
            Updates.set(
                "pricePerNight",
                if (updateHotelTicketModel.price.isNullOrBlank()) ticketModel.pricePerNight else updateHotelTicketModel.price
            ),
            Updates.set(
                "fromDate",
                if (updateHotelTicketModel.reservationDate.isNullOrBlank()) ticketModel.fromDate else updateHotelTicketModel.reservationDate
            ),
            Updates.set(
                "toDate",
                if (updateHotelTicketModel.checkOutDate.isNullOrBlank()) ticketModel.toDate else updateHotelTicketModel.checkOutDate
            ),
            Updates.set(
                "roomClass",
                if (updateHotelTicketModel.roomClass.isNullOrBlank()) ticketModel.roomClass else updateHotelTicketModel.roomClass
            ),
            Updates.set(
                "roomCategory",
                if (updateHotelTicketModel.roomCategory.isNullOrBlank()) ticketModel.roomCategory else updateHotelTicketModel.roomCategory
            ),
            Updates.set(
                "transportation",
                if (updateHotelTicketModel.transportation.isNullOrBlank()) ticketModel.transportation else updateHotelTicketModel.transportation
            ),
            Updates.set(
                "isVisible",
                if (updateHotelTicketModel.isVisible.isNullOrBlank()) ticketModel.isVisible else updateHotelTicketModel.isVisible
            ),
            Updates.set(
                "numberOfRoomsPerNight",
                if (updateHotelTicketModel.quantity.isNullOrBlank()) ticketModel.numberOfRoomsPerNight else updateHotelTicketModel.quantity
            ),
            Updates.set(
                "ticketNumber",
                if (updateHotelTicketModel.ticketNumber.isNullOrBlank()) ticketModel.ticketNumber else updateHotelTicketModel.ticketNumber
            )
              ,
            Updates.set(
                "numberOfAdultsAllowance",
                if (updateHotelTicketModel.numberOfAdults.isNullOrBlank()) ticketModel.numberOfAdultsAllowance else updateHotelTicketModel.numberOfAdults
            ),
            Updates.set(
                "numberOfChildrenAllowance",
                if (updateHotelTicketModel.numberOfChildren.isNullOrBlank()) ticketModel.numberOfChildrenAllowance else updateHotelTicketModel.numberOfChildren
            ),
            Updates.set(
                "childrenAge",
                if (updateHotelTicketModel.childrenAge.isNullOrBlank()) ticketModel.childrenAge else updateHotelTicketModel.childrenAge
            )
        )
        val updateResult = hotelTickets.updateOne(filter, update)
        return if (updateResult.matchedCount > 0) {
            ApiResponse(data = null, succeeded = true, errorCode = errorCode,message = arrayListOf("Success"))
        } else {
            ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Hotel Ticket not found"),
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
        filterByHotelIds: List<String>?,
    ): PagingApiResponse<List<ResponseHotelTicketModel>?> {
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
            val foundHotel = hotels.findOne(finalQueryForSearchFilter)
            queryForItemFilter.add(
                eq("hotelId", foundHotel?.id?.toHexString())
            )
        }

        queryForItemFilter.add(
            eq("userId", userId)
        )

        if (filterByPriceRangeFrom != null && filterByPriceRangeTo != null) {
            queryForItemFilter.add(
                and(
                    gte("price", filterByPriceRangeFrom),
                    lte("price", filterByPriceRangeTo)
                )
            )
        } else if (filterByPriceRangeFrom != null) {
            queryForItemFilter.add(
                gte("price", filterByPriceRangeFrom)
            )
        } else if (filterByPriceRangeTo != null) {
            queryForItemFilter.add(
                lte("price", filterByPriceRangeTo)
            )
        }

        if (!filterByHotelIds.isNullOrEmpty()) {
            queryForItemFilter.add(
                `in`("hotelId", filterByHotelIds)
            )
        }

        if (filterByDateFrom != null && filterByDateTo != null) {
            queryForItemFilter.add(
                and(
                    gte("reservationDate", filterByDateFrom),
                    lte("checkOutDate", filterByDateTo)
                )
            )
        } else if (filterByDateFrom != null) {
            queryForItemFilter.add(
                gte("reservationDate", filterByDateFrom)
            )
        } else if (filterByDateTo != null) {
            queryForItemFilter.add(
                lte("checkOutDate", filterByDateTo)
            )
        }

        if (filterByVisibility != null) {
            queryForItemFilter.add(
                eq("isVisible", filterByVisibility)
            )
        }

        val finalQuery = and(queryForItemFilter)
        val totalCount = hotelTickets.countDocuments(finalQuery).toInt()
        val totalPages =
            if (totalCount % (if (pageSize == 0) 1 else pageSize) == 0) totalCount / (if (pageSize == 0) 1 else pageSize) else (totalCount / (if (pageSize == 0) 1 else pageSize)) + 1
        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages
        return PagingApiResponse(
            succeeded = true,
            data = hotelTickets.find(finalQuery)
                .skip(skip)
                .limit(pageSize)
                .toList().map { hotelTicketModel ->
                    val filterHotel = eq("_id", ObjectId(hotelTicketModel.hotelId))
                    val hotelModel = hotels.findOne(filterHotel)
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
            errorCode = errorCode
        )
    }
}