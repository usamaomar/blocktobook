//package com.example.data.repository.countryDataSource
//
//import com.example.data.repository.cityDataSource.CountryDataSource
//import com.example.domain.model.publicModel.ApiResponse
//import com.example.domain.model.publicModel.PagingApiResponse
//import com.example.domain.model.cityModel.CityModel
//import com.example.domain.model.cityModel.CityProfileModel
//import com.example.domain.model.cityModel.CreateCityModel
//import com.example.domain.model.cityModel.ResponseCityModel
//import com.example.domain.model.cityModel.UpdateCityModel
//import com.example.domain.model.cityModel.toCityModel
//import com.mongodb.client.model.Filters
//import org.litote.kmongo.coroutine.CoroutineDatabase
//import org.litote.kmongo.eq
//import com.mongodb.client.model.Updates.set
//import org.litote.kmongo.and
//import org.litote.kmongo.div
//import org.litote.kmongo.elemMatch
//import org.litote.kmongo.regex
//
//class CountryDataSourceImpl(database: CoroutineDatabase) : CountryDataSource {
//
//    private val cities = database.getCollection<CityModel>()
//    private val errorCode : Int = 4
//    override suspend fun getById(id: String, xAppLanguageId: Int): ApiResponse<CityModel?> {
//        val filter = CityModel::id eq id
//        if (cities.findOne(filter) == null) {
//            return ApiResponse(
//                data = null,
//                succeeded = false,
//                message = arrayListOf("City not found"),
//                errorCode = errorCode
//            )
//        }
//        var cityModel = cities.findOne(filter)
//        val matchingProfile = cityModel?.profiles?.find { it.languageId == xAppLanguageId }
//        if (matchingProfile != null) {
//            cityModel = cityModel?.copy(
//                profiles = listOf(matchingProfile)
//            )
//        }else{
//            return ApiResponse(
//                data = null,
//                succeeded = false,
//                message = arrayListOf("Matching Profile error"),
//                errorCode = errorCode
//            )
//        }
//        return ApiResponse(data = cityModel, succeeded = true,  errorCode = errorCode)
//    }
//
//    override suspend fun post(cityModel: CreateCityModel): ApiResponse<CityModel?> {
//        val existingCity = cities.findOne(
//            CityModel::profiles / CityProfileModel::name eq cityModel.profiles.firstOrNull { it.languageId == 1 }?.name
//        )
//        return if (existingCity == null) {
//            val local: CityModel = cityModel.toCityModel()
//            val insertedId = cities.insertOne(document = local).insertedId
//            val isValid = cities.find(Filters.eq("_id", insertedId))
//            if (isValid.first() != null) {
//                ApiResponse(data =  isValid.first(), succeeded = true,  errorCode = errorCode)
//            } else {
//                ApiResponse(data = null, succeeded = false, message = arrayListOf("Error saving City"),  errorCode = errorCode)
//            }
//        } else {
//            ApiResponse(data = existingCity, succeeded = false, message = arrayListOf("All ready exists"),  errorCode = errorCode)
//        }
//    }
//
//    override suspend fun put(
//        updateCityModel: UpdateCityModel
//    ): ApiResponse<CityModel?> {
//        val filter = CityModel::id eq updateCityModel.id
//        val update = set("CityModel", CityModel)
//        cities.updateOne(filter = filter, update = update)
//        return ApiResponse(data = cities.findOne(filter = CityModel::id eq updateCityModel.id), succeeded = true,  errorCode = errorCode)
//    }
//
//    override suspend fun getAll(
//        searchText: String,
//        pageSize: Int,
//        pageNumber: Int,
//        xAppLanguageId: Int
//    ): PagingApiResponse<List<ResponseCityModel>?> {
//        val skip = (pageNumber - 1) * pageSize
//        val query = and(
//            CityModel::profiles.elemMatch(
//                CityProfileModel::name regex searchText
//            ), CityModel::profiles.elemMatch(
//                CityProfileModel::countryName regex searchText
//            ),
//            CityModel::profiles.elemMatch(CityProfileModel::languageId eq xAppLanguageId)
//        )
//        // Perform the query
//        val totalCount = cities.countDocuments(query).toInt()
//        val totalPages =
//            if (totalCount % (if (pageSize == 0)  1 else pageSize) == 0) totalCount / (if (pageSize == 0)  1 else pageSize) else (totalCount / (if (pageSize == 0)  1 else pageSize)) + 1
//        val hasPreviousPage = pageNumber > 1
//        val hasNextPage = pageNumber < totalPages
//        return PagingApiResponse(
//            succeeded = true,
//            data = cities.find(query)
//                .skip(skip)
//                .limit(pageSize)
//                .toList().mapNotNull { cityModel ->
//                    // Find the profile matching the languageId
//                    val matchingProfile = cityModel.profiles.find { it.languageId == xAppLanguageId }
//                    // If a matching profile is found, map it to CitySummary
//                    matchingProfile?.let {
//                        ResponseCityModel(
//                            id = cityModel.id.toString(),
//                            name = it.name,
//                            countryName = it.countryName,
//                            twoDigitCountryCode = cityModel.twoDigitCountryCode,
//                            threeDigitCountryCode = cityModel.threeDigitCountryCode
//                        )
//                    }
//                },
//            currentPage = pageNumber,
//            totalPages = totalPages,
//            totalCount = totalCount,
//            hasPreviousPage = hasPreviousPage,
//            hasNextPage = hasNextPage,
//            errorCode = errorCode
//        )
//    }
//}