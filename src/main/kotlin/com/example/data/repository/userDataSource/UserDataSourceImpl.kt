package com.example.data.repository.userDataSource

import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.userModel.UpdateUser
import com.example.domain.model.userModel.User
import com.example.util.AccessRole
import com.mongodb.client.model.Updates.combine
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import com.mongodb.client.model.Updates.set
import org.litote.kmongo.and
import org.litote.kmongo.ascending

class UserDataSourceImpl(database: CoroutineDatabase) : UserDataSource {

    private val users = database.getCollection<User>()
    private val errorCode: Int = 5

    override suspend fun getUserInfo(userId: String): User? {
        return users.findOne(filter = User::id eq userId)
    }

    override suspend fun saveUserInfo(user: User): User {
        val existingUser = users.findOne(filter = User::id eq user.id)
        return if (existingUser == null) {
            val isValid = users.insertOne(document = user).wasAcknowledged()
            if (isValid) {
                user
            } else {
                throw Exception("Error saving user")
            }
        } else {
            existingUser
        }
    }

    override suspend fun deleteUser(userId: String): Boolean {
        return users.deleteOne(filter = User::id eq userId).wasAcknowledged()
    }

    override suspend fun updateUserInfo(
        userId: String,
        updatedUserModel: UpdateUser
    ): User? {
        val filter = User::id eq userId
        val existingUser = users.findOne(filter = filter)
        val newUserModel = existingUser?.copy(
            name = updatedUserModel.name ?: existingUser.name,
            profilePhoto = updatedUserModel.profilePhoto ?: existingUser.profilePhoto
        )
        val update = combine(
            set("name", newUserModel?.name),
            set("profilePhoto", newUserModel?.profilePhoto),
        )
        users.updateOne(filter, update)
        return users.findOne(filter = filter)!!
    }

    override suspend fun getAll(
        searchText: String,
        pageSize: Int,
        pageNumber: Int,
        xAppLanguageId: Int
    ): PagingApiResponse<List<User>?> {
//        val skip = (pageNumber - 1) * pageSize

        // Build query with a compound filter for better search performance
        val query = and(
//            or(
//                User::name text searchText,  // Use text search for better indexing
//                User::emailAddress text searchText,
//                User::companyInfo / CompanyInfoModel::name text searchText,
//                User::companyInfo / CompanyInfoModel::facilityNumber text searchText
//            ),
            User::accessRole eq AccessRole.Merchant // Filter by access role
        )

        // Optimize counting if exact count isn't required
//        val totalCount = users.estimatedDocumentCount(query).toInt()

        // Compute pagination details
//        val totalPages = if (pageSize == 0) 1 else (totalCount + pageSize - 1) / pageSize
//        val hasPreviousPage = pageNumber > 1
//        val hasNextPage = pageNumber < totalPages

        // Perform the query efficiently
        val userList = users.find(query)
            .sort(ascending(User::name)) // Sorting for better cursor-based pagination
//            .skip(skip)
            .limit(pageSize)
            .toList()

        return PagingApiResponse(
            succeeded = true,
            data = userList,
            currentPage = pageNumber,
            totalPages = 1,
            totalCount = 1,
            hasPreviousPage = false,
            hasNextPage = false,
            errorCode = errorCode
        )
    }


//    override suspend fun getAll(
//        searchText: String,
//        pageSize: Int,
//        pageNumber: Int,
//        xAppLanguageId: Int
//    ): PagingApiResponse<List<User>?> {
//        val skip = (pageNumber - 1) * pageSize
//        val query = or(
//            User::name regex searchText, // Search by user name (partial match)
//            User::emailAddress regex searchText, // Search by email (partial match)
//            User::companyInfo / CompanyInfoModel::name regex searchText, // Search by company name
//            User::companyInfo / CompanyInfoModel::facilityNumber regex searchText // Search by facility number
//        )
//        // Perform the query
//
//        val totalCount = users.countDocuments(query).toInt()
//        val totalPages =
//            if (totalCount % (if (pageSize == 0)  1 else pageSize) == 0) totalCount / (if (pageSize == 0)  1 else pageSize) else (totalCount / (if (pageSize == 0)  1 else pageSize)) + 1
//        val hasPreviousPage = pageNumber > 1
//        val hasNextPage = pageNumber < totalPages
//        return PagingApiResponse(
//            succeeded = true,
//            data = users.find(query)
//                .skip(skip)
//                .limit(pageSize)
//                .toList().filter {
//                    it.accessRole == (AccessRole.Merchant)
//                },
//            currentPage = pageNumber,
//            totalPages = totalPages,
//            totalCount = totalCount,
//            hasPreviousPage = hasPreviousPage,
//            hasNextPage = hasNextPage,
//            errorCode = errorCode
//        )
//    }

}