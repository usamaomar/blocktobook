package com.example.data.repository.cityDataSource

import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.cityModel.CityModel
import com.example.domain.model.cityModel.CityProfileModel
import com.example.domain.model.cityModel.CreateCityModel
import com.example.domain.model.cityModel.ResponseCityModel
import com.example.domain.model.cityModel.UpdateCityModel
import com.example.domain.model.cityModel.toCityModel
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
import org.litote.kmongo.or
import org.litote.kmongo.regex

class CityDataSourceImpl(database: CoroutineDatabase) : CityDataSource {

    private val cities = database.getCollection<CityModel>()
    private val errorCode : Int = 2

    override suspend fun getById(id: String, xAppLanguageId: Int): ApiResponse<ResponseCityModel?> {
        val filter = eq("_id", ObjectId(id))
        if (cities.findOne(filter) == null) {
            return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("City not found"),
                errorCode = errorCode
            )
        }
        var cityModel = cities.findOne(filter)
        var responseCityModel: ResponseCityModel? = null
        val matchingProfile = cityModel?.profiles?.find { it.languageId == xAppLanguageId }
        if (matchingProfile != null) {
            cityModel = cityModel?.copy(
                profiles = listOf(matchingProfile)
            )
            responseCityModel = ResponseCityModel(
                id = id ?: "",
                name = matchingProfile.name,
                countryName = matchingProfile.countryName,
                twoDigitCountryCode = cityModel?.twoDigitCountryCode ?: "",
                threeDigitCountryCode = cityModel?.threeDigitCountryCode ?: ""
            )
        }else{
            return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Matching Profile error"),
                errorCode = errorCode
            )
        }
        return ApiResponse(data = responseCityModel, succeeded = true,  errorCode = errorCode)
    }

    override suspend fun post(cityModel: CreateCityModel,  xAppLanguageId: Int): ApiResponse<ResponseCityModel?> {
        val existingCity = cities.findOne(
            CityModel::profiles / CityProfileModel::name eq cityModel.profiles.firstOrNull { it.languageId == 1 }?.name
        )
        return if (existingCity == null) {
            val local: CityModel = cityModel.toCityModel()
            val insertResult = cities.insertOne(document = local)
            val insertedId = insertResult.insertedId?.asObjectId()?.value?.toString()
            val insertedTicket = cities.findOne(eq("_id", ObjectId(insertedId)))
            ApiResponse(data =  insertedTicket?.toResponseCityModel(xAppLanguageId,insertedId ?: ""), succeeded = true,  errorCode = errorCode)
        } else {
            val local: CityModel = cityModel.toCityModel()
            val insertResult = cities.insertOne(document = local)
            val insertedId = insertResult.insertedId?.asObjectId()?.value?.toString()
            val insertedTicket = cities.findOne(eq("_id", ObjectId(insertedId)))
            ApiResponse(
                data = insertedTicket?.toResponseCityModel(xAppLanguageId,insertedId ?: ""),
                succeeded = false,
                message = arrayListOf("All ready exists"),  errorCode = errorCode
            )
        }
    }

    override suspend fun put(
        updateCityModel: UpdateCityModel,  xAppLanguageId: Int
    ): ApiResponse<ResponseCityModel?> {
        val filter = eq("_id", ObjectId(updateCityModel.id))
        val update = combine(
            set("profiles", updateCityModel.profiles),
            set("twoDigitCountryCode", updateCityModel.twoDigitCountryCode),
            set("threeDigitCountryCode", updateCityModel.threeDigitCountryCode)
        )
        val updateResult = cities.updateOne(filter, update)
        return if (updateResult.matchedCount > 0) {
            val updatedCity = cities.findOne(filter)
            ApiResponse(data = updatedCity?.toResponseCityModel(xAppLanguageId,updateCityModel.id ?: ""), succeeded = true,  errorCode = errorCode)
        } else {
            ApiResponse(data = null, succeeded = false, message = arrayListOf("City not found"),  errorCode = errorCode)
        }
    }

    override suspend fun getAll(
        searchText: String,
        pageSize: Int,
        pageNumber: Int,
        xAppLanguageId: Int
    ): PagingApiResponse<List<ResponseCityModel>?> {
        val skip = (pageNumber - 1) * pageSize
        val query = and(
            or(
                // Match in the name (beginning, middle, or end), case-insensitive
                CityModel::profiles.elemMatch(
                    and(
                        CityProfileModel::name regex searchText.toRegex(RegexOption.IGNORE_CASE),
                        or(
                            CityProfileModel::languageId eq 1,
                            CityProfileModel::languageId eq 2
                        )
                    )
                ),
                // Match in the two-digit country code, case-insensitive
                CityModel::twoDigitCountryCode regex searchText.toRegex(RegexOption.IGNORE_CASE),
                // Match in the three-digit country code, case-insensitive
                CityModel::threeDigitCountryCode regex searchText.toRegex(RegexOption.IGNORE_CASE)
            )
        )
        // Perform the query
        val totalCount = cities.countDocuments(query).toInt()
        val totalPages =
            if (totalCount % (if (pageSize == 0)  1 else pageSize) == 0) totalCount / (if (pageSize == 0)  1 else pageSize) else (totalCount / (if (pageSize == 0)  1 else pageSize)) + 1
        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages
        return PagingApiResponse(
            succeeded = true,
            data = cities.find(query)
                .skip(skip)
                .limit(pageSize)
                .toList().mapNotNull { cityModel ->
                    val matchingProfile =
                        cityModel.profiles.find { it.languageId == xAppLanguageId }
                    matchingProfile?.let {
                        ResponseCityModel(
                            id = cityModel.id?.toHexString() ?: "",
                            name = it.name,
                            countryName = it.countryName,
                            twoDigitCountryCode = cityModel.twoDigitCountryCode,
                            threeDigitCountryCode = cityModel.threeDigitCountryCode,
                            profiles = cityModel.profiles
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