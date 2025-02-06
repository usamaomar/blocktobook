package com.example.data.repository.userDataSource

import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.userModel.UpdateUser
import com.example.domain.model.userModel.User

interface UserDataSource {
    suspend fun getUserInfo(userId: String): User?
    suspend fun saveUserInfo(user: User): User?
    suspend fun deleteUser(userId: String): Boolean
    suspend fun updateUserInfo(
        userId: String,
        updatedUserModel: UpdateUser
    ): User?

    suspend fun getAll(searchText: String,
                       pageSize: Int,
                       pageNumber: Int,
                       xurrenttime : Long,
                       xAppLanguageId: Int): PagingApiResponse<List<User>?>?

}