package com.example.data.repository.hotelDataSource

import com.example.domain.model.cityModel.CityModel
import com.example.domain.model.cityModel.toResponseCityModel
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.hotelModel.HotelProfileModel
import com.example.domain.model.hotelModel.ResponseHotelModel
import com.example.domain.model.hotelModel.UpdateHotelModel
import com.example.domain.model.hotelModel.CreateHotelModel
import com.example.domain.model.hotelModel.HotelModel
import com.example.domain.model.hotelModel.toHotelModel
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

class HotelDataSourceImpl(database: CoroutineDatabase) : HotelDataSource {

    private val hotels = database.getCollection<HotelModel>()
    private val errorCode: Int = 209
    private val cities = database.getCollection<CityModel>()

    override suspend fun getById(
        id: String,
        xAppLanguageId: Int
    ): ApiResponse<ResponseHotelModel?> {
        val filter = eq("_id", ObjectId(id))
        if (hotels.findOne(filter) == null) {
            return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Hotel not found"),
                errorCode = errorCode
            )
        }
        var hotelModel = hotels.findOne(filter)
        val filterCity = eq("_id", ObjectId(hotelModel?.cityId))
        val cityModel = cities.findOne(filterCity)


        val responseHotelModel: ResponseHotelModel?
        val matchingProfile = hotelModel?.profiles?.find { it.languageId == xAppLanguageId }
        if (matchingProfile != null) {
            hotelModel = hotelModel?.copy(
                profiles = listOf(matchingProfile)
            )
            responseHotelModel = ResponseHotelModel(
                id = hotelModel?.id?.toHexString() ?: "",
                name = matchingProfile.name,
                logo = hotelModel?.logo ?: "",
                longitude = hotelModel?.longitude ?: 0.0,
                latitude = hotelModel?.latitude ?: 0.0,
                stars = hotelModel?.stars ?: 0.0,
                city = cityModel?.toResponseCityModel(xAppLanguageId,cityModel.id?.toHexString() ?: ""),
            )
        } else {
            return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Matching Profile error"),
                errorCode = errorCode
            )
        }
        return ApiResponse(data = responseHotelModel, succeeded = true, errorCode = errorCode)
    }

    override suspend fun post(hotelModel: CreateHotelModel): ApiResponse<HotelModel?> {
        val existingCity = hotels.findOne(
            HotelModel::profiles / HotelProfileModel::name eq hotelModel.profiles.firstOrNull { it.languageId == 1 }?.name
        )
        return if (existingCity == null) {
            val filterCity = eq("_id", ObjectId(hotelModel.cityId))
            val cityModel = cities.findOne(filterCity)
                ?: return ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("City not found"),
                    errorCode = errorCode
                )
            val local: HotelModel = hotelModel.toHotelModel()
            val insertResult = hotels.insertOne(document = local)
            val insertedId = insertResult.insertedId?.asObjectId()?.value?.toString()
            val isValid = hotels.findOne(eq("_id", ObjectId(insertedId)))
            if (isValid  != null) {
                ApiResponse(
                    data = null,
                    succeeded = true,
                    message = arrayListOf("Success"), errorCode = errorCode
                )
            } else {
                ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("Error saving Hotel"), errorCode = errorCode
                )
            }
        } else {
            ApiResponse(
                data = existingCity,
                succeeded = false,
                message = arrayListOf("All ready exists"), errorCode = errorCode
            )
        }
    }

    override suspend fun put(
        updateHotelModel: UpdateHotelModel
    ): ApiResponse<HotelModel?> {
        val filterCity = eq("_id", ObjectId(updateHotelModel.cityId))
        if (cities.findOne(filterCity) == null) {
            return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("City not found"),
                errorCode = errorCode
            )
        }
        val filter = eq("_id", ObjectId(updateHotelModel.id))
        val update = combine(
            set("profiles", updateHotelModel.profiles),
            set("latitude", updateHotelModel.latitude),
            set("longitude", updateHotelModel.longitude),
            set("stars", updateHotelModel.stars),
            set("cityId", updateHotelModel.cityId),
            set("logo", updateHotelModel.logo)
        )
        val updateResult = hotels.updateOne(filter, update)
        return if (updateResult.matchedCount > 0) {
            ApiResponse(
                data = null,
                succeeded = true,
                message = arrayListOf("Success"), errorCode = errorCode
            )
        } else {
            ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Hotel not found"),
                errorCode = errorCode
            )
        }
    }

    override suspend fun getAll(
        searchText: String,
        pageSize: Int,
        filterByCityId: String?,
        pageNumber: Int,
        xAppLanguageId: Int
    ): PagingApiResponse<List<ResponseHotelModel>?> {
        val skip = (pageNumber - 1) * pageSize
        val query = mutableListOf<Bson>()
        if (!filterByCityId.isNullOrEmpty() && filterByCityId!="null") {
            query.add(eq("cityId", filterByCityId))
        }
        if (searchText.isNotEmpty()) {
            query.add(
                HotelModel::profiles.elemMatch(
                    HotelProfileModel::name regex searchText
                )
            )
        }

        query.add(
            HotelModel::profiles.elemMatch(
                HotelProfileModel::languageId eq xAppLanguageId
            )
        )

        val finalQuery = and(query)
        // Perform the query
        val totalCount = hotels.countDocuments(finalQuery).toInt()
        val totalPages =
            if (totalCount % (if (pageSize == 0)  1 else pageSize) == 0) totalCount / (if (pageSize == 0)  1 else pageSize) else (totalCount / (if (pageSize == 0)  1 else pageSize)) + 1
        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages
        return PagingApiResponse(
            succeeded = true,
            data = hotels.find(finalQuery)
                .skip(skip)
                .limit(pageSize)
                .toList().mapNotNull { hotelModel ->
                    val matchingProfile =
                        hotelModel.profiles.find { it.languageId == xAppLanguageId }
                    matchingProfile?.let {
                        val filterCity = eq("_id", ObjectId(hotelModel.cityId))
                        val cityModel = cities.findOne(filterCity)
                        ResponseHotelModel(
                            id = hotelModel.id?.toHexString() ?: "",
                            name = it.name,
                            profiles = hotelModel.profiles,
                            logo = hotelModel.logo,
                            longitude = hotelModel.longitude,
                            latitude = hotelModel.latitude,
                            stars = hotelModel.stars,
                            city = cityModel?.toResponseCityModel(xAppLanguageId,cityModel.id?.toHexString()?:""),
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