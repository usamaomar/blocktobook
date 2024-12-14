package com.example.data.repository.airLinesDataSource

import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.airlinesModel.AirLineModel
import com.example.domain.model.airlinesModel.AirlineProfileModel
import com.example.domain.model.airlinesModel.CreateAirLine
import com.example.domain.model.airlinesModel.ResponseAirLineModel
import com.example.domain.model.airlinesModel.UpdateAirLine
import com.example.domain.model.airlinesModel.toAirLineModel
import com.example.domain.model.cityModel.CityModel
import com.example.domain.model.cityModel.toResponseCityModel
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.combine
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import com.mongodb.client.model.Updates.set
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.div
import org.litote.kmongo.elemMatch
import org.litote.kmongo.regex

class AirLineDataSourceImpl(database: CoroutineDatabase) : AirLineDataSource {

    private val airLines = database.getCollection<AirLineModel>()
    private val cities = database.getCollection<CityModel>()
    private val errorCode : Int = 2

    override suspend fun getById(id: String, xAppLanguageId: Int): ApiResponse<ResponseAirLineModel?> {
        val filter = eq("_id", ObjectId(id))
        if (airLines.findOne(filter) == null) {
            return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("AirLine not found"),
                errorCode = errorCode
            )
        }
        var airlineModel = airLines.findOne(filter)
        val responseAirLineModel: ResponseAirLineModel?
        val matchingProfile = airlineModel?.profiles?.find { it.languageId == xAppLanguageId }
        if (matchingProfile != null) {
            airlineModel = airlineModel?.copy(
                profiles = listOf(matchingProfile)
            )
            responseAirLineModel = ResponseAirLineModel(
                id = airlineModel?.id?.toHexString() ?: "",
                name = matchingProfile.name,
                logo = airlineModel?.logo ?: "",
                code = airlineModel?.code ?: ""
            )
        }else{
            return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Matching Profile error"),
                errorCode = errorCode
            )
        }
        return ApiResponse(data = responseAirLineModel, succeeded = true,  errorCode = errorCode)
    }

    override suspend fun post(airLineModel: CreateAirLine): ApiResponse<AirLineModel?> {
        val existingCity = airLines.findOne(
            AirLineModel::profiles / AirlineProfileModel::name eq airLineModel.profiles.firstOrNull { it.languageId == 1 }?.name
        )
        return if (existingCity == null) {
            val filterCity = eq("_id", ObjectId(airLineModel.cityId))
           cities.findOne(filterCity)
                ?: return ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("City not found"),
                    errorCode = errorCode
                )
            val local: AirLineModel = airLineModel.toAirLineModel()
            val insertResult = airLines.insertOne(document = local)
            val insertedId = insertResult.insertedId?.asObjectId()?.value?.toString()
            if (insertedId  != null) {
                ApiResponse(
                    data = null,
                    succeeded = true,
                    message = arrayListOf("Success"), errorCode = errorCode
                )
            } else {
                ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("Error saving AirLine"), errorCode = errorCode
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
        updateAirLineModel: UpdateAirLine
    ): ApiResponse<AirLineModel?> {
        val filter = eq("_id", ObjectId(updateAirLineModel.id))
        val update = combine(
            set("profiles", updateAirLineModel.profiles),
            set("referenceName", updateAirLineModel.referenceName),
            set("code", updateAirLineModel.code),
            set("cityId", updateAirLineModel.cityId),
            set("logo", updateAirLineModel.logo)
        )
        val updateResult = airLines.updateOne(filter, update)
        return if (updateResult.matchedCount > 0) {
            ApiResponse(
                data = null,
                succeeded = true,
                message = arrayListOf("Success"), errorCode = errorCode
            )
        } else {
            ApiResponse(data = null, succeeded = false, message = arrayListOf("AirLine not found"),  errorCode = errorCode)
        }
    }

    override suspend fun getAll(
        searchText: String,
        pageSize: Int,
        pageNumber: Int,
        xAppLanguageId: Int
    ): PagingApiResponse<List<ResponseAirLineModel>?> {
        val skip = (pageNumber - 1) * pageSize
        val query = and(
            AirLineModel::profiles.elemMatch(
                AirlineProfileModel::name regex searchText
            ),
            AirLineModel::profiles.elemMatch(AirlineProfileModel::languageId eq xAppLanguageId)
        )
        // Perform the query
        val totalCount = airLines.countDocuments(query).toInt()
        val totalPages =
            if (totalCount % (if (pageSize == 0)  1 else pageSize) == 0) totalCount / (if (pageSize == 0)  1 else pageSize) else (totalCount / (if (pageSize == 0)  1 else pageSize)) + 1
        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages
        return PagingApiResponse(
            succeeded = true,
            data = airLines.find(query)
                .skip(skip)
                .limit(pageSize)
                .toList().mapNotNull { cityModel ->
                    val matchingProfile =
                        cityModel.profiles.find { it.languageId == xAppLanguageId }
                    val filterCity = eq("_id", ObjectId(cityModel.cityId))
                    val cityMainModel = cities.findOne(filterCity)
                    matchingProfile?.let {
                        ResponseAirLineModel(
                            id = cityModel.id?.toHexString() ?: "",
                            name = it.name, logo = cityModel.logo,
                            profiles = cityModel.profiles,
                            referenceName = cityModel.referenceName,
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