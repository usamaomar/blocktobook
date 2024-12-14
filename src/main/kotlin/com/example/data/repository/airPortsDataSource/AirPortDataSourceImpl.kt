package com.example.data.repository.airPortsDataSource

import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.airportsModel.AirPortModel
import com.example.domain.model.airportsModel.AirPortProfileModel
import com.example.domain.model.airportsModel.CreateAirPort
import com.example.domain.model.airportsModel.ResponseAirPortModel
import com.example.domain.model.airportsModel.UpdateAirPort
import com.example.domain.model.airportsModel.toAirPortModel
import com.example.domain.model.cityModel.CityModel
import com.example.domain.model.cityModel.toResponseCityModel
import com.example.domain.model.hotelModel.HotelModel
import com.example.domain.model.hotelModel.HotelProfileModel
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.combine
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import com.mongodb.client.model.Updates.set
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.div
import org.litote.kmongo.elemMatch
import org.litote.kmongo.regex

class AirPortsDataSourceImpl(database: CoroutineDatabase) : AirPortDataSource {

    private val airPorts = database.getCollection<AirPortModel>()
    private val cities = database.getCollection<CityModel>()
    private val errorCode : Int = 2

    override suspend fun getById(id: String, xAppLanguageId: Int): ApiResponse<ResponseAirPortModel?> {
        val filter = eq("_id", ObjectId(id))
        if (airPorts.findOne(filter) == null) {
            return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("AirPort not found"),
                errorCode = errorCode
            )
        }
        var airlineModel = airPorts.findOne(filter)
        val responseAirPortModel: ResponseAirPortModel?
        val matchingProfile = airlineModel?.profiles?.find { it.languageId == xAppLanguageId }
        if (matchingProfile != null) {
            airlineModel = airlineModel?.copy(
                profiles = listOf(matchingProfile)
            )
            val filterCity = eq("_id", ObjectId(airlineModel?.cityId))
            val cityMainModel = cities.findOne(filterCity)
            responseAirPortModel = ResponseAirPortModel(
                id = airlineModel?.id?.toHexString() ?: "",
                name = matchingProfile.name,
                profiles = airlineModel?.profiles,
                referenceName = airlineModel?.referenceName,
                cityId = cityMainModel?.id?.toHexString() ?: "",
                city = cityMainModel?.toResponseCityModel(xAppLanguageId,cityMainModel.id?.toHexString()?:""),
                code = airlineModel?.code
            )
        }else{
            return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Matching Profile error"),
                errorCode = errorCode
            )
        }
        return ApiResponse(data = responseAirPortModel, succeeded = true,  errorCode = errorCode)
    }

    override suspend fun post(airPortModel: CreateAirPort): ApiResponse<AirPortModel?> {
        val existingCity = airPorts.findOne(
            AirPortModel::profiles / AirPortProfileModel::name eq airPortModel.profiles.firstOrNull { it.languageId == 1 }?.name
        )
        return if (existingCity == null) {
            val local: AirPortModel = airPortModel.toAirPortModel()
            val insertedId = airPorts.insertOne(document = local).wasAcknowledged()
            if (insertedId) {
                ApiResponse(
                    data = null,
                    succeeded = true,
                    message = arrayListOf("Success"), errorCode = errorCode
                )
            } else {
                ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("Error saving AirPort"),  errorCode = errorCode
                )
            }
        } else {
            ApiResponse(
                data = existingCity,
                succeeded = false,
                message = arrayListOf("All ready exists"),  errorCode = errorCode
            )
        }
    }

    override suspend fun put(
        updateAirPortModel: UpdateAirPort
    ): ApiResponse<AirPortModel?> {
        val filter = eq("_id", ObjectId(updateAirPortModel.id))
        val update = combine(
            set("profiles", updateAirPortModel.profiles),
            set("referenceName", updateAirPortModel.referenceName),
            set("code", updateAirPortModel.code),
            set("cityId", updateAirPortModel.cityId)
        )
        val updateResult = airPorts.updateOne(filter, update)
        return if (updateResult.matchedCount > 0) {
            ApiResponse(
                data = null,
                succeeded = true,
                message = arrayListOf("Success"), errorCode = errorCode
            )
        } else {
            ApiResponse(data = null, succeeded = false, message = arrayListOf("AirPort not found"),  errorCode = errorCode)
        }
    }

    override suspend fun getAll(
        searchText: String,
        pageSize: Int,
        pageNumber: Int,
        xAppLanguageId: Int,
        filterByCityId: String?
    ): PagingApiResponse<List<ResponseAirPortModel>?> {
        val skip = (pageNumber - 1) * pageSize
        val query = mutableListOf<Bson>()
        if (!filterByCityId.isNullOrEmpty() && filterByCityId!="null") {
            query.add(eq("cityId", filterByCityId))
        }
        if (searchText.isNotEmpty()) {
            query.add(
                AirPortModel::profiles.elemMatch(
                    AirPortProfileModel::name regex searchText
                )
            )
        }
        query.add(
            AirPortModel::profiles.elemMatch(AirPortProfileModel::languageId eq xAppLanguageId)
        )
        val finalQuery = and(query)
        // Perform the query
        val totalCount = airPorts.countDocuments(finalQuery).toInt()
        val totalPages =
            if (totalCount % (if (pageSize == 0)  1 else pageSize) == 0) totalCount / (if (pageSize == 0)  1 else pageSize) else (totalCount / (if (pageSize == 0)  1 else pageSize)) + 1
        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages
        return PagingApiResponse(
            succeeded = true,
            data = airPorts.find(finalQuery)
                .skip(skip)
                .limit(pageSize)
                .toList().mapNotNull { cityModel ->
                    val matchingProfile =
                        cityModel.profiles.find { it.languageId == xAppLanguageId }
                    val filterCity = eq("_id", ObjectId(cityModel.cityId))
                    val cityMainModel = cities.findOne(filterCity)
                    matchingProfile?.let {
                        ResponseAirPortModel(
                            id = cityModel.id?.toHexString() ?: "",
                            name = it.name,
                            profiles = cityModel.profiles,
                            referenceName = cityModel.referenceName,
                            cityId = cityMainModel?.id?.toHexString() ?: "",
                            city = cityMainModel?.toResponseCityModel(xAppLanguageId,cityMainModel.id?.toHexString()?:""),
                            code = cityModel.code
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
}