//package com.example.data.repository.cityDataSource
//
//import com.example.domain.model.publicModel.ApiResponse
//import com.example.domain.model.publicModel.PagingApiResponse
//import com.example.domain.model.cityModel.CreateCityModel
//import com.example.domain.model.cityModel.CityModel
//import com.example.domain.model.cityModel.ResponseCityModel
//import com.example.domain.model.cityModel.UpdateCityModel
//
//interface CountryDataSource {
//    suspend fun getById(id: String, xAppLanguageId: Int): ApiResponse<CityModel?>?
//    suspend fun post(cityModel: CreateCityModel): ApiResponse<CityModel?>?
//    suspend fun put(updateCityModel: UpdateCityModel): ApiResponse<CityModel?>?
//    suspend fun getAll(searchText: String,
//                       pageSize: Int,
//                       pageNumber: Int,
//                       xAppLanguageId: Int): PagingApiResponse<List<ResponseCityModel>?>?
//}