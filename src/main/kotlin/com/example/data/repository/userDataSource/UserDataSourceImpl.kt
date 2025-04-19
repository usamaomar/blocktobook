package com.example.data.repository.userDataSource

import com.example.domain.model.profileModel.CompanyInfoModel
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.userModel.UpdateUser
import com.example.domain.model.userModel.User
import com.example.util.AccessRole
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Updates.combine
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import com.mongodb.client.model.Updates.set
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.and
import org.litote.kmongo.ascending
import org.litote.kmongo.descending
import org.litote.kmongo.div
import org.litote.kmongo.or
import org.litote.kmongo.regex
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
class UserDataSourceImpl(database: CoroutineDatabase) : UserDataSource {

    private val users = database.getCollection<User>()
    private val errorCode: Int = 5

    override suspend fun getUserInfo(userId: String): User? {
        return users.findOne(filter = User::id eq userId)
    }

    override suspend fun getImageArray(imageUrl: String): ByteArray? {
        return fetchImageAsByteArray(imageUrl)
    }

    private fun fetchImageAsByteArray(imageUrl: String): ByteArray = runBlocking {
        // Initialize the Ktor HTTP client with the CIO engine
        val client = HttpClient(CIO)

        try {
            // Send a GET request to the specified image URL
            val response: HttpResponse = client.get(imageUrl)

            // Check if the response status is OK (HTTP 200)
            if (response.status.value == 200) {
                // Read the response content as a ByteArray
                response.readBytes()
            } else {
                // Handle non-OK HTTP responses
                throw Exception("Failed to fetch image: ${response.status}")
            }
        } catch (e: Exception) {
            // Handle exceptions (e.g., network errors)
            throw Exception("Error fetching image: ${e.message}")
        } finally {
            // Close the HTTP client to free resources
            client.close()
        }
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
            profilePhoto = updatedUserModel.profilePhoto ?: existingUser.profilePhoto,
            companyInfo = existingUser.companyInfo?.copy(
                companyLogo = updatedUserModel.companyLogo ?: existingUser.companyInfo.companyLogo
            )
        )
        val update = combine(
            set("name", newUserModel?.name),
            set("profilePhoto", newUserModel?.profilePhoto),
            set("companyInfo.companyLogo", newUserModel?.companyInfo?.companyLogo),
        )
        users.updateOne(filter, update)
        return users.findOne(filter = filter)!!
    }

//    override suspend fun getAll(
//        searchText: String,
//        pageSize: Int,
//        pageNumber: Int,
//        xurren: Long,
//        xAppLanguageId: Int
//    ): PagingApiResponse<List<User>?> {
//        val skip = (pageNumber - 1) * pageSize
//        val currentTime = System.currentTimeMillis()
//
//       val query = or(
//            User::name regex searchText, // Search by user name (partial match)
//            User::emailAddress regex searchText, // Search by email (partial match)
//            User::companyInfo / CompanyInfoModel::name regex searchText, // Search by company name
//            User::companyInfo / CompanyInfoModel::facilityNumber regex searchText // Search by facility number
//        )
//
//        users.createIndex(
//            Indexes.compoundIndex(
//                Indexes.ascending("name", "emailAddress", "companyInfo.name", "companyInfo.facilityNumber")
//            ),
//            IndexOptions().background(true)
//        )
//
//        val totalCount = users.estimatedDocumentCount().toInt()
//        val totalPages =
//            if (totalCount % (if (pageSize == 0)  1 else pageSize) == 0) totalCount / (if (pageSize == 0)  1 else pageSize) else (totalCount / (if (pageSize == 0)  1 else pageSize)) + 1
//        val hasPreviousPage = pageNumber > 1
//        val hasNextPage = pageNumber < totalPages
//
//        val userList = users.find(query).limit(pageSize)
//             .toList()
//
//        val queryExecutionTime = System.currentTimeMillis()
//
//        return PagingApiResponse(
//            succeeded = true,
//            data = userList,
//            message = arrayListOf("From our $xurren", "inside $currentTime", "goIn $queryExecutionTime"),
//            currentPage = pageNumber,
//            totalPages = totalPages,
//            totalCount = totalCount,
//            hasPreviousPage = hasPreviousPage,
//            hasNextPage = hasNextPage,
//            errorCode = errorCode
//        )
//    }


    override suspend fun getAll(
        searchText: String,
        pageSize: Int,
        pageNumber: Int,
        xurren: Long,
        xAppLanguageId: Int
    ): PagingApiResponse<List<User>?> {
        val skip = (pageNumber - 1) * pageSize
        val query = or(
//            User::name regex searchText, // Search by user name (partial match)
//            User::emailAddress regex searchText, // Search by email (partial match)
            User::companyInfo / CompanyInfoModel::name regex searchText, // Search by company name
            User::companyInfo / CompanyInfoModel::facilityNumber regex searchText // Search by facility number
        )
        // Perform the query

        val totalCount = users.countDocuments(query).toInt()
        val totalPages =
            if (totalCount % (if (pageSize == 0)  1 else pageSize) == 0) totalCount / (if (pageSize == 0)  1 else pageSize) else (totalCount / (if (pageSize == 0)  1 else pageSize)) + 1
        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages
        return PagingApiResponse(
            succeeded = true,
            data = users.find(query)
                .skip(skip)
                .limit(pageSize)
                .toList().filter {
                    it.accessRole == (AccessRole.Merchant)
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