package com.example.data.repository.authDataSource

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.domain.model.authModel.CreateEmailAdminModel
import com.example.domain.model.authModel.CreateEmailModel
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.userModel.UserSession
import com.example.domain.model.authModel.CreateRefreshTokenModel
import com.example.domain.model.userModel.User
import com.example.domain.model.authModel.ResponseTokenModel
import com.example.domain.model.authModel.UpdateTokenModel
import com.example.plugins.decodeJwtPayload
import com.example.util.AccessRole
import com.example.util.Constants
import com.example.util.Constants.AUDIENCE
import com.example.util.Constants.ISSUER
import com.example.util.Constants.LOG_OU
import com.example.util.isNullOrBlank
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.firebase.auth.FirebaseAuth
import com.mongodb.client.model.Updates
import org.litote.kmongo.coroutine.CoroutineDatabase
import io.ktor.util.hex
import org.litote.kmongo.eq
import java.util.Date

class AuthDataSourceImpl(database: CoroutineDatabase) : AuthDataSource {
    private val errorCode: Int = 1
    private val users = database.getCollection<User>()
    private val refreshTokenDataBase = database.getCollection<UpdateTokenModel>()

    override suspend fun googleLoginMerchant(
        tokenId: String?
    ): Map<String, Any> {
        val result = verifyGoogleTokenId(tokenId = tokenId ?: "")
        if (result != null) {
            val sub = result.payload?.get("sub").toString()
            val name = result.payload?.get("name").toString()
            val emailAddress = result.payload?.get("email").toString()
            val profilePhoto = result.payload?.get("picture").toString()
            val accessToken = generateAccessToken(
                userId = sub,
                secret = hex("00112233445566778899aabbccddeeff").toString(),
                issuer = result.payload.issuer,
                audience = result.payload.audience.toString()
            )
            val refreshToken = generateRefreshToken(
                userId = sub,
                secret = hex("00112233bbccddeeff").toString(),
                issuer = result.payload.issuer,
            )
            val user = User(
                id = sub,
                name = name,
                emailAddress = emailAddress,
                profilePhoto = profilePhoto,
                accessRole = AccessRole.Merchant,
            )
            val existingUser = users.findOne(filter = User::id eq sub)
            if (existingUser == null) {
                users.insertOne(document = user).wasAcknowledged()
                refreshTokenDataBase.insertOne(
                    document = UpdateTokenModel(
                        refreshToken = refreshToken,
                        token = accessToken,
                        userId = sub
                    )
                ).wasAcknowledged()
            } else {
                val filter = UpdateTokenModel::refreshToken eq refreshToken
                val update = Updates.set(
                    "updateTokenModel", UpdateTokenModel(
                        refreshToken = refreshToken,
                        token = accessToken,
                        userId = sub
                    )
                )
                refreshTokenDataBase.updateOne(filter = filter, update = update)
            }
            return mapOf(
                "ApiResponse" to ApiResponse(
                    succeeded = true,
                    data = ResponseTokenModel(token = accessToken, refreshToken = refreshToken),
                    errorCode = errorCode
                ),
                "UserSession" to UserSession(id = sub, name = name)
            )
        } else {
            return mapOf(
                "ApiResponse" to ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("Token verification failed"),
                    errorCode = errorCode
                )
            )
        }
    }

    override suspend fun loginByEmailMerchant(createEmailModel: CreateEmailModel): Map<String, Any> {
        var existingUser = users.findOne(filter = User::emailAddress eq createEmailModel.email)
        if (existingUser == null) {
            val user = User(
                id = "",
                name = createEmailModel.name,
                emailAddress = createEmailModel.email,
                accessRole = AccessRole.Merchant, profilePhoto = "")
            val insertResult = users.insertOne(document = user)
            val insertedId = insertResult.insertedId?.asObjectId()?.value?.toString()
            val update = Updates.combine(
                Updates.set(
                    "id",
                    insertedId
                )
            )
         users.updateOne(User::emailAddress eq createEmailModel.email, update)
         existingUser = users.findOne(filter = User::emailAddress eq createEmailModel.email)
        }
        val accessToken = generateAccessToken(
            userId = existingUser?.id ?: "",
            secret = hex("00112233445566778899aabbccddeeff").toString(),
            issuer = ISSUER,
            audience = AUDIENCE
        )
        val refreshToken = generateRefreshToken(
            userId = existingUser?.id ?: "",
            secret = hex("00112233bbccddeeff").toString(),
            issuer = ISSUER,
        )
        refreshTokenDataBase.insertOne(
            document = UpdateTokenModel(
                refreshToken = refreshToken,
                token = accessToken,
                userId = existingUser?.id ?: "",
            )
        ).wasAcknowledged()
        return mapOf(
            "ApiResponse" to ApiResponse(
                succeeded = true,
                data = ResponseTokenModel(token = accessToken, refreshToken = refreshToken),
                errorCode = errorCode
            ),
            "UserSession" to UserSession(id = existingUser?.id ?: "", name = existingUser?.name ?: "")
        )
    }

    override suspend fun loginByToken(createEmailModel: CreateEmailModel): Map<String, Any> {
        var existingUser = users.findOne(filter = User::emailAddress eq createEmailModel.email)
        if (existingUser == null) {
            val user = User(
                id = createEmailModel.uid,
                name = createEmailModel.name,
                emailAddress = createEmailModel.email,
                accessRole = AccessRole.Merchant, profilePhoto = "")
         users.insertOne(document = user)
         existingUser = users.findOne(filter = User::emailAddress eq createEmailModel.email)
        }
        val accessToken = generateAccessToken(
            userId = existingUser?.id ?: "",
            secret = hex("00112233445566778899aabbccddeeff").toString(),
            issuer = ISSUER,
            audience = AUDIENCE
        )
        val refreshToken = generateRefreshToken(
            userId = existingUser?.id ?: "",
            secret = hex("00112233bbccddeeff").toString(),
            issuer = ISSUER,
        )
        refreshTokenDataBase.insertOne(
            document = UpdateTokenModel(
                refreshToken = refreshToken,
                token = accessToken,
                userId = existingUser?.id ?: "",
            )
        ).wasAcknowledged()
        return mapOf(
            "ApiResponse" to ApiResponse(
                succeeded = true,
                data = ResponseTokenModel(token = accessToken, refreshToken = refreshToken),
                errorCode = errorCode
            ),
            "UserSession" to UserSession(id = existingUser?.id ?: "", name = existingUser?.name ?: "")
        )
    }

    override suspend fun createEmailMerchant(createEmailModel: CreateEmailModel): Map<String, Any>  {
        val existingUser = users.findOne(filter = User::emailAddress eq createEmailModel.email)
        if (existingUser == null) {
            val userRecord = FirebaseAuth.getInstance().createUser(
                com.google.firebase.auth.UserRecord.CreateRequest()
                    .setEmail(createEmailModel.email)
                    .setDisplayName(createEmailModel.name)
            )
            val user = User(
                id = userRecord.uid,
                name = createEmailModel.name,
                emailAddress = createEmailModel.email,
                accessRole = AccessRole.Merchant,
                profilePhoto = ""
            )
            val accessToken = generateAccessToken(
                userId = userRecord.uid,
                secret = hex("00112233445566778899aabbccddeeff").toString(),
                issuer = ISSUER,
                audience = AUDIENCE
            )
            val refreshToken = generateRefreshToken(
                userId = userRecord.uid,
                secret = hex("00112233bbccddeeff").toString(),
                issuer = ISSUER,
            )
            users.insertOne(document = user).wasAcknowledged()
            refreshTokenDataBase.insertOne(
                document = UpdateTokenModel(
                    refreshToken = refreshToken,
                    token = accessToken,
                    userId = userRecord.uid
                )
            ).wasAcknowledged()
            return mapOf(
                "ApiResponse" to ApiResponse(
                    succeeded = true,
                    data = ResponseTokenModel(token = accessToken, refreshToken = refreshToken),
                    errorCode = errorCode
                ),
                "UserSession" to UserSession(id = userRecord.uid, name = userRecord.displayName)
            )
        } else {
            return mapOf(
                "ApiResponse" to ApiResponse(
                    succeeded = false,
                    message = arrayListOf("User already exists"),
                    data = null,
                    code = Constants.USER_HAS_ACCOUNT,
                    errorCode = errorCode
                )
            )
        }
    }
    override suspend fun createEmailAdmin(createEmailModel: CreateEmailAdminModel): Map<String, Any>  {
        val existingUser = users.findOne(filter = User::emailAddress eq createEmailModel.email)
        if (existingUser == null) {
            val userRecord = FirebaseAuth.getInstance().createUser(
                com.google.firebase.auth.UserRecord.CreateRequest()
                    .setEmail(createEmailModel.email)
//                    .setPassword(createEmailModel.password)
                    .setDisplayName(createEmailModel.userName)
            )
            val user = User(
                id = userRecord.uid,
                name = createEmailModel.userName,
                emailAddress = createEmailModel.email,
                accessRole = AccessRole.Admin,
                profilePhoto = ""
            )
            val accessToken = generateAccessToken(
                userId = userRecord.uid,
                secret = hex("00112233445566778899aabbccddeeff").toString(),
                issuer = ISSUER,
                audience = AUDIENCE
            )
            val refreshToken = generateRefreshToken(
                userId = userRecord.uid,
                secret = hex("00112233bbccddeeff").toString(),
                issuer = ISSUER,
            )
            users.insertOne(document = user).wasAcknowledged()
            refreshTokenDataBase.insertOne(
                document = UpdateTokenModel(
                    refreshToken = refreshToken,
                    token = accessToken,
                    userId = userRecord.uid
                )
            ).wasAcknowledged()
            return mapOf(
                "ApiResponse" to ApiResponse(
                    succeeded = true,
                    data = ResponseTokenModel(token = accessToken, refreshToken = refreshToken),
                    errorCode = errorCode
                ),
                "UserSession" to UserSession(id = userRecord.uid, name = userRecord.displayName)
            )
        } else {
            return mapOf(
                "ApiResponse" to ApiResponse(
                    succeeded = false,
                    message = arrayListOf("User already exists"),
                    data = null,
                    code = Constants.USER_HAS_ACCOUNT,
                    errorCode = errorCode
                )
            )
        }
    }

    override suspend fun refresh(tokenModel: CreateRefreshTokenModel?): Map<String, Any> {
        val tokenDecode = decodeJwtPayload(tokenModel?.token ?: "")
        val refreshDecode = decodeJwtPayload(tokenModel?.refreshToken ?: "")
        if (tokenDecode["userId"] != refreshDecode["userId"]) {
            return mapOf(
                "ApiResponse" to ApiResponse(
                    succeeded = false,
                    message = arrayListOf("You are not authorized"),
                    data = null,
                    code = Constants.SUBSCRIPTION_EXPIRED,
                    errorCode = errorCode
                )
            )
        }
        val userModel =
            users.findOne(filter = User::id eq refreshDecode["userId"]) ?: return mapOf(
                "ApiResponse" to ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("User not found"),
                    code = LOG_OU,
                    errorCode = errorCode
                )
            )
        val accessToken = generateAccessToken(
            userId = userModel.id ?: "",
            secret = hex("00112233445566778899aabbccddeeff").toString(),
            issuer = ISSUER,
            audience = AUDIENCE
        )
        val refreshToken = generateRefreshToken(
            userId = userModel.id ?: "",
            secret = hex("00112233bbccddeeff").toString(),
            issuer = ISSUER,
        )
        val filter = UpdateTokenModel::userId eq userModel.id
        val update = Updates.set(
            "updateTokenModel", UpdateTokenModel(
                refreshToken = refreshToken,
                token = accessToken, userId = userModel.id ?: ""
            )
        )
        val updateResult = refreshTokenDataBase.updateOne(filter = filter, update = update)
        if (updateResult.wasAcknowledged()) {
            return mapOf(
                "ApiResponse" to ApiResponse(
                    succeeded = true,
                    data = ResponseTokenModel(token = accessToken, refreshToken = refreshToken),
                    errorCode = errorCode
                ),
                "UserSession" to UserSession(
                    id = userModel.id ?: "",
                    name = userModel.name ?: ""
                )
            )
        } else {
            return mapOf(
                "ApiResponse" to ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("Error"),
                    errorCode = errorCode
                )
            )
        }
    }

    override suspend fun getTest(): String? {
         return "Usama Last uok"
    }

    private fun generateAccessToken(
        userId: String, secret: String, issuer: String, audience: String
    ): String? {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
//            .withExpiresAt(Date(System.currentTimeMillis() + (45 * 60 * 1000)))
            .withExpiresAt(Date(System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)))
            .sign(Algorithm.HMAC256(secret))
    }

    private fun generateRefreshToken(userId: String, secret: String, issuer: String): String {
        return JWT.create()
            .withSubject("Refresh")
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .sign(Algorithm.HMAC256(secret))
    }

}

fun verifyGoogleTokenId(tokenId: String): GoogleIdToken? {
    return try {
        val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
            .setAudience(listOf(AUDIENCE)).setIssuer(ISSUER).build()
        val payload = GoogleIdToken.parse(verifier.jsonFactory, tokenId)
        val isEmailVerified = payload.payload?.get("email_verified")
        if (isEmailVerified == true) {
            val exp = payload.payload?.get("exp").toString()
            if (!isTimestampExpired(exp.toLong())) {
                payload
            } else {
                null
            }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}


fun isTimestampExpired(timestamp: Long): Boolean {
    val currentTime = System.currentTimeMillis()
    val expirationTimeInMillis = timestamp * 1000
    return currentTime < expirationTimeInMillis
}