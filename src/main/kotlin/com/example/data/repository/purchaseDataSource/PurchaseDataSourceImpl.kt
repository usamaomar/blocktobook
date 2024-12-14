package com.example.data.repository.purchaseDataSource

import com.example.data.repository.cartDataSource.CartDataSource
import com.example.data.repository.cartDataSource.validateCartAddition
import com.example.data.repository.cityDataSource.ProfileDataSource
import com.example.domain.model.adminWalletAmount.AdminWalletAmount
import com.example.domain.model.airlinesTicketModel.ResponseAirlineTicketModel
import com.example.domain.model.cartModel.CartModel
import com.example.domain.model.cartModel.ResponseCartModel
import com.example.domain.model.cartModel.toCartModel
import com.example.domain.model.cartModel.toPurchaseModel
import com.example.domain.model.cityModel.CityModel
import com.example.domain.model.hotelModel.HotelModel
import com.example.domain.model.hotelTicketModel.HotelTicketModel
import com.example.domain.model.hotelTicketModel.ResponseHotelTicketModel
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.purchaseModel.CreateCustomerListModel
import com.example.domain.model.purchaseModel.CreateCustomerModel
import com.example.domain.model.purchaseModel.CustomerModel
import com.example.domain.model.purchaseModel.DatesModel
import com.example.domain.model.purchaseModel.PurchaseModel
import com.example.domain.model.purchaseModel.ResponsePurchasedHotelTicketModel
import com.example.domain.model.purchaseModel.toCustomerModel
import com.example.domain.model.purchaseModel.toCustomerModelList
import com.example.domain.model.subscriptionTypesModel.SubscriptionTypeModel
import com.example.domain.model.transactionModel.TransactionModel
import com.example.domain.model.userModel.User
import com.example.domain.model.walletAmountModel.WalletAmountModel
import com.example.util.Status
import com.example.util.TopUpType
import com.example.util.TransactionType
import com.example.util.toDoubleAmount
import com.google.api.gax.rpc.Batch
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates.combine
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.gte
import org.litote.kmongo.inc
import org.litote.kmongo.lte
import org.litote.kmongo.or
import org.litote.kmongo.setValue
import java.security.SecureRandom
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.random.asKotlinRandom

class PurchaseDataSourceImpl(database: CoroutineDatabase) : PurchaseDataSource {
    private val cartDataSource: CartDataSource by KoinJavaComponent.inject(CartDataSource::class.java)
    private val hotelTicketCollection = database.getCollection<HotelTicketModel>()
    private val walletCollection = database.getCollection<WalletAmountModel>()
    private val transactionCollection = database.getCollection<TransactionModel>()
    private val purchaseModel = database.getCollection<PurchaseModel>()
    private val adminWalletAmount = database.getCollection<AdminWalletAmount>()
    private val subscriptionTypeDatabase = database.getCollection<SubscriptionTypeModel>()
    val profileDataSource: ProfileDataSource by KoinJavaComponent.inject(ProfileDataSource::class.java)

    private val hotels = database.getCollection<HotelModel>()
    private val cities = database.getCollection<CityModel>()

    private val errorCode: Int = 12
    private val users = database.getCollection<User>()

    override suspend fun checkOut(userId: String): ApiResponse<String?> {
        // Fetch all required data in parallel
        val (pagingApiResponse, user, walletAmount) = coroutineScope {
            val cartData = async { cartDataSource.getAll(userId, 100000, 1, 1) }
            val userData = async { users.findOne(User::id eq userId) }
            val walletData = async { walletCollection.findOne(WalletAmountModel::userId eq userId) }

            Triple(cartData.await(), userData.await(), walletData.await())
        }

        val cartList = pagingApiResponse?.data
        if (cartList.isNullOrEmpty()) {
            return ApiResponse(
                data = "Error", succeeded = false, errorCode = errorCode,
                message = arrayListOf("Cart is empty")
            )
        }

        cartList.forEach { cartModel ->
            if (cartModel.hotelTicketModel != null) {
                val purchaseModelListItems = purchaseModel.find(
                    Filters.eq(
                        "hotelTicketModel.hotelId",
                        ObjectId(cartModel.hotelTicketModel.id)
                    )
                ).toList()
                val validateCartAddition =
                    validateCartAddition(purchaseModelListItems, cartModel.toCartModel())
                if (validateCartAddition != null) {
                    return ApiResponse(
                        data = cartModel.id,
                        succeeded = false,
                        errorCode = errorCode,
                        message = arrayListOf("The selected date range conflicts with existing bookings and exceeds availability.")
                    )
                }
            } else if (cartModel.returnAirLineTicketModel != null) {
                val airlineTicketModel = purchaseModel.find(
                    Filters.eq(
                        "airLineModel.id",
                        ObjectId(cartModel.airLineModel?.id)
                    )
                ).toList()

                val totalSeatsAvailable =
                    airlineTicketModel.sumOf { it.airLineModel?.numberOfSeats ?: 0 }

                if (totalSeatsAvailable != 0) {
                    if (cartModel.numberOfRooms > totalSeatsAvailable) {
                        throw IllegalArgumentException("Error: Not enough seats available for the requested number.")
                    }
                }

                val returnAirlineTicketModel = purchaseModel.find(
                    Filters.eq(
                        "airLineModel.returnAirLineModel.id",
                        ObjectId(cartModel.returnAirLineTicketModel.id)
                    )
                ).toList()
                val totalReturnSeatsAvailable =
                    returnAirlineTicketModel.sumOf { it.returnAirLineModel?.numberOfSeats ?: 0 }
                if (totalReturnSeatsAvailable != 0) {
                    if (cartModel.numberOfRooms > totalReturnSeatsAvailable) {
                        throw IllegalArgumentException("Error: Not enough seats available for the requested number.")
                    }
                }
            } else if (cartModel.airLineModel != null) {
                val airlineTicketModel = purchaseModel.find(
                    Filters.eq(
                        "airLineModel.id",
                        ObjectId(cartModel.airLineModel.id)
                    )
                ).toList()

                val totalSeatsAvailable =
                    airlineTicketModel.sumOf { it.airLineModel?.numberOfSeats ?: 0 }
                if (totalSeatsAvailable != 0) {
                    if (cartModel.numberOfRooms > totalSeatsAvailable) {
                        throw IllegalArgumentException("Error: Not enough seats available for the requested.")
                    }
                }
            }
        }


        val cartAmount = cartDataSource.getAmount(userId).toDouble()
        val blockToBookFees = user?.companyInfo?.blockToBookFees ?: 0.0
        val currentWalletAmount = walletAmount?.amount ?: 0.0

        // Check if sufficient funds are available
        if ((currentWalletAmount + blockToBookFees) < cartAmount) {
            return ApiResponse(
                data = "Error", succeeded = false, errorCode = errorCode,
                message = arrayListOf("Insufficient funds")
            )
        }

        val checkoutId = generateUniqueToken()
        val failedPurchase = coroutineScope {
            cartList.map { cartModel ->
                async {
                    processCartModel(cartModel, checkoutId, userId)
                }
            }.awaitAll().any { !it.succeeded }
        }

        if (failedPurchase) {
            return ApiResponse(
                data = "Error", succeeded = false, errorCode = errorCode,
                message = arrayListOf("Purchase failed due to insufficient stock")
            )
        }

        // Update wallet and admin wallet
        val buyerWallet = walletCollection.findOne(WalletAmountModel::userId eq userId)
        val currentWalletAmount1 = buyerWallet?.amount ?: 0.0
        val updatedBuyerWalletAmount = currentWalletAmount1 - blockToBookFees
        updateBuyerWallet(userId, updatedBuyerWalletAmount, blockToBookFees, checkoutId)

        return ApiResponse(data = "Success", succeeded = true, errorCode = errorCode)
    }

    override suspend fun checkOutSubscription(
        userId: String,
        subscriptionId: String
    ): ApiResponse<String?> {
        val (subscriptionData, user, walletAmount) = coroutineScope {
            val subscriptionData = async {
                subscriptionTypeDatabase.findOne(
                    Filters.eq(
                        "_id",
                        ObjectId(subscriptionId)
                    )
                )
            }
            val userData = async { users.findOne(User::id eq userId) }
            val walletData = async { walletCollection.findOne(WalletAmountModel::userId eq userId) }
            Triple(subscriptionData.await(), userData.await(), walletData.await())
        }
        val cartAmount = subscriptionData?.price
        val currentWalletAmount = walletAmount?.amount ?: 0.0
        // Check if sufficient funds are available
        if ((currentWalletAmount) < (cartAmount ?: 0.0)) {
            return ApiResponse(
                data = "Error", succeeded = false, errorCode = errorCode,
                message = arrayListOf("Insufficient funds")
            )
        }
        walletCollection.updateOne(
            WalletAmountModel::userId eq userId,
            setValue(WalletAmountModel::amount, currentWalletAmount - (cartAmount ?: 0.0))
        )
        val subscriptionType =
            subscriptionTypeDatabase.findOne(Filters.eq("_id", ObjectId(subscriptionId)))
                ?: return ApiResponse(
                    data = "Error", succeeded = false, errorCode = errorCode,
                    message = arrayListOf("Subscription not found")
                )
        profileDataSource.updateSubscription(userId, subscriptionType.type)
        return ApiResponse(data = "Success", succeeded = true, errorCode = errorCode)
    }


    override suspend fun getAll(
        userId: String,
        searchText: String,
        pageSize: Int,
        pageNumber: Int,
        xAppLanguageId: Int,
        filterByDateFrom: Long?,
        filterByDateTo: Long?,
        filterByVisibility: Boolean?,
        filterByPriceRangeFrom: Double?,
        filterByPriceRangeTo: Double?,
        filterByHotelIds: List<String>?,
        status: Int?
    ): PagingApiResponse<List<ResponsePurchasedHotelTicketModel>?> {
        val skip = (pageNumber - 1) * pageSize
        val queryForSearchFilter = mutableListOf<Bson>()
        val queryForItemFilter = mutableListOf<Bson>()

        // Apply filters similar to the previous method
        if (searchText.isNotEmpty()) {
            queryForSearchFilter.add(
                Filters.regex("hotelTicketModel.hotel.profiles.name", searchText)
            )
            val foundPurchase = purchaseModel.findOne(and(queryForSearchFilter))
            queryForItemFilter.add(
                Filters.eq("hotelTicketModel.hotelId", foundPurchase?.hotelTicketModel?.hotel?.id)
            )
        }
        queryForItemFilter.add(Filters.and(Filters.ne("hotelTicketModel", null)))


        queryForItemFilter.add(Filters.eq("userId", userId))

        if (filterByPriceRangeFrom != null || filterByPriceRangeTo != null) {
            queryForItemFilter.add(
                Filters.and(
                    filterByPriceRangeFrom?.let { Filters.gte("hotelTicketModel.price", it) },
                    filterByPriceRangeTo?.let { Filters.lte("hotelTicketModel.price", it) }
                )
            )
        }

        if (!filterByHotelIds.isNullOrEmpty()) {
            queryForItemFilter.add(Filters.`in`("hotelTicketModel.hotelId", filterByHotelIds))
        }

        if (filterByDateFrom != null || filterByDateTo != null) {
            queryForItemFilter.add(
                Filters.and(
                    filterByDateFrom?.let { Filters.gte("createdAt", it) },
                    filterByDateTo?.let { Filters.lte("createdAt", it) }
                )
            )
        }

        if (filterByVisibility != null) {
            queryForItemFilter.add(Filters.eq("hotelTicketModel.isVisible", filterByVisibility))
        }

        status?.let {
            queryForItemFilter.add(Filters.eq("status", it))
        }

        val finalQuery = and(queryForItemFilter)
        val totalCount = purchaseModel.countDocuments(finalQuery).toInt()
        val totalPages =
            if (totalCount % (if (pageSize == 0) 1 else pageSize) == 0) totalCount / (if (pageSize == 0) 1 else pageSize) else (totalCount / (if (pageSize == 0) 1 else pageSize)) + 1

        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages

        val data = purchaseModel.find(finalQuery)
            .skip(skip)
            .limit(pageSize)
            .toList()
            .map { purchase ->
                ResponsePurchasedHotelTicketModel(
                    id = purchase.id?.toHexString(),
                    hotelTicketModel = purchase.hotelTicketModel,
                    userId = purchase.userId,
                    checkoutId = purchase.checkoutId,
                    status = purchase.status,
                    customerModel = purchase.customerModel,
                    createdAt = purchase.createdAt,
                    checkOutDate = purchase.checkOutDate,
                    checkInDate = purchase.checkInDate,
                    numberOfRooms = purchase.numberOfRooms
                )
            }

        return PagingApiResponse(
            succeeded = true,
            data = data,
            currentPage = pageNumber,
            totalPages = totalPages,
            totalCount = totalCount,
            hasPreviousPage = hasPreviousPage,
            hasNextPage = hasNextPage,
            errorCode = errorCode
        )
    }

    override suspend fun getAllForAirlines(
        userId: String,
        searchText: String,
        pageSize: Int,
        pageNumber: Int,
        xAppLanguageId: Int,
        filterByDateFrom: Long?,
        filterByDateTo: Long?,
        filterByVisibility: Boolean?,
        filterByPriceRangeFrom: Double?,
        filterByPriceRangeTo: Double?,
        filterByHotelIds: List<String>?,
        status: Int?
    ): PagingApiResponse<List<ResponsePurchasedHotelTicketModel>?> {
        val skip = (pageNumber - 1) * pageSize
        val queryForSearchFilter = mutableListOf<Bson>()
        val queryForItemFilter = mutableListOf<Bson>()

        // Apply filters similar to the previous method
        if (searchText.isNotEmpty()) {
            queryForSearchFilter.add(
                Filters.regex("airLineModel.departureCity.profiles.name", searchText)
            )
            val foundPurchase = purchaseModel.findOne(and(queryForSearchFilter))
            queryForItemFilter.add(
                Filters.eq(
                    "airLineModel.departureCity.id",
                    foundPurchase?.airLineModel?.departureCity?.id
                )
            )
        }

        queryForItemFilter.add(Filters.eq("userId", userId))

        if (filterByPriceRangeFrom != null || filterByPriceRangeTo != null) {
            queryForItemFilter.add(
                Filters.and(
                    filterByPriceRangeFrom?.let { Filters.gte("airLineModel.pricePerSeat", it) },
                    filterByPriceRangeTo?.let { Filters.lte("airLineModel.pricePerSeat", it) }
                )
            )
        }

        queryForItemFilter.add(Filters.and(Filters.ne("airLineModel", null)))

        if (!filterByHotelIds.isNullOrEmpty()) {
            queryForItemFilter.add(Filters.`in`("airLineModel.departureCity.id", filterByHotelIds))
        }

        if (filterByDateFrom != null || filterByDateTo != null) {
            queryForItemFilter.add(
                Filters.and(
                    filterByDateFrom?.let { Filters.gte("createdAt", it) },
                    filterByDateTo?.let { Filters.lte("createdAt", it) }
                )
            )
        }

        if (filterByVisibility != null) {
            queryForItemFilter.add(Filters.eq("airLineModel.isVisible", filterByVisibility))
        }

        status?.let {
            queryForItemFilter.add(Filters.eq("status", it))
        }

        val finalQuery = and(queryForItemFilter)
        val totalCount = purchaseModel.countDocuments(finalQuery).toInt()
        val totalPages =
            if (totalCount % (if (pageSize == 0) 1 else pageSize) == 0) totalCount / (if (pageSize == 0) 1 else pageSize) else (totalCount / (if (pageSize == 0) 1 else pageSize)) + 1

        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages

        val data = purchaseModel.find(finalQuery)
            .skip(skip)
            .limit(pageSize)
            .toList()
            .map { purchase ->
                ResponsePurchasedHotelTicketModel(
                    id = purchase.id?.toHexString(),
                    hotelTicketModel = null,
                    airLineCustomerModels = purchase.airLineCustomerModels,
                    airLineModel = purchase.airLineModel,
                    returnAirLineModel = purchase.returnAirLineModel,
                    userId = purchase.userId,
                    checkoutId = purchase.checkoutId,
                    status = purchase.status,
                    note = purchase.note,
                    customerModel = purchase.customerModel,
                    createdAt = purchase.createdAt,
                    checkOutDate = purchase.checkOutDate,
                    checkInDate = purchase.checkInDate,
                    numberOfRooms = purchase.numberOfRooms
                )
            }

        return PagingApiResponse(
            succeeded = true,
            data = data,
            currentPage = pageNumber,
            totalPages = totalPages,
            totalCount = totalCount,
            hasPreviousPage = hasPreviousPage,
            hasNextPage = hasNextPage,
            errorCode = errorCode
        )
    }


    override suspend fun getAllMerchantHotelReservations(
        userId: String,
        searchText: String,
        pageSize: Int,
        pageNumber: Int,
        xAppLanguageId: Int,
        filterByDateFrom: Long?,
        filterByDateTo: Long?,
        filterByVisibility: Boolean?,
        filterByPriceRangeFrom: Double?,
        filterByPriceRangeTo: Double?,
        filterByHotelIds: List<String>?,
        status: Int?
    ): PagingApiResponse<List<ResponsePurchasedHotelTicketModel>?>? {
        val skip = (pageNumber - 1) * pageSize
        val queryForSearchFilter = mutableListOf<Bson>()
        val queryForItemFilter = mutableListOf<Bson>()

        // Apply filters similar to the previous method
        if (searchText.isNotEmpty()) {
            queryForSearchFilter.add(
                Filters.regex("hotelTicketModel.hotel.profiles.name", searchText)
            )
            val foundPurchase = purchaseModel.findOne(and(queryForSearchFilter))
            queryForItemFilter.add(
                Filters.eq("hotelTicketModel.hotelId", foundPurchase?.hotelTicketModel?.hotel?.id)
            )
        }

        queryForItemFilter.add(Filters.eq("hotelTicketModel.userId", userId))

        if (filterByPriceRangeFrom != null || filterByPriceRangeTo != null) {
            queryForItemFilter.add(
                Filters.and(
                    filterByPriceRangeFrom?.let { Filters.gte("hotelTicketModel.price", it) },
                    filterByPriceRangeTo?.let { Filters.lte("hotelTicketModel.price", it) }
                )
            )
        }

        if (!filterByHotelIds.isNullOrEmpty()) {
            queryForItemFilter.add(Filters.`in`("hotelTicketModel.hotelId", filterByHotelIds))
        }

        if (filterByDateFrom != null || filterByDateTo != null) {
            queryForItemFilter.add(
                Filters.and(
                    filterByDateFrom?.let { Filters.gte("createdAt", it) },
                    filterByDateTo?.let { Filters.lte("createdAt", it) }
                )
            )
        }

        if (filterByVisibility != null) {
            queryForItemFilter.add(Filters.eq("hotelTicketModel.isVisible", filterByVisibility))
        }

        status?.let {
            queryForItemFilter.add(Filters.eq("status", it))
        }

        val finalQuery = and(queryForItemFilter)
        val totalCount = purchaseModel.countDocuments(finalQuery).toInt()
        val totalPages =
            if (totalCount % (if (pageSize == 0) 1 else pageSize) == 0) totalCount / (if (pageSize == 0) 1 else pageSize) else (totalCount / (if (pageSize == 0) 1 else pageSize)) + 1

        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages

        val data = purchaseModel.find(finalQuery)
            .skip(skip)
            .limit(pageSize)
            .toList()
            .map { purchase ->
                ResponsePurchasedHotelTicketModel(
                    id = purchase.id?.toHexString(),
                    hotelTicketModel = purchase.hotelTicketModel ?: error("Hotel ticket missing"),
                    userId = purchase.userId,
                    checkoutId = purchase.checkoutId,
                    status = purchase.status,
                    customerModel = purchase.customerModel,
                    createdAt = purchase.createdAt,
                    checkOutDate = purchase.checkOutDate,
                    checkInDate = purchase.checkInDate,
                    numberOfRooms = purchase.numberOfRooms
                )
            }

        return PagingApiResponse(
            succeeded = true,
            data = data,
            currentPage = pageNumber,
            totalPages = totalPages,
            totalCount = totalCount,
            hasPreviousPage = hasPreviousPage,
            hasNextPage = hasNextPage,
            errorCode = errorCode
        )
    }

    override suspend fun getAllMerchantAirlineReservations(
        userId: String,
        searchText: String,
        pageSize: Int,
        pageNumber: Int,
        xAppLanguageId: Int,
        filterByDateFrom: Long?,
        filterByDateTo: Long?,
        filterByVisibility: Boolean?,
        filterByPriceRangeFrom: Double?,
        filterByPriceRangeTo: Double?,
        filterByHotelIds: List<String>?,
        status: Int?
    ): PagingApiResponse<List<ResponsePurchasedHotelTicketModel>?>? {


        val skip = (pageNumber - 1) * pageSize
        val queryForSearchFilter = mutableListOf<Bson>()
        val queryForItemFilter = mutableListOf<Bson>()

        // Apply filters similar to the previous method
        if (searchText.isNotEmpty()) {
            queryForSearchFilter.add(
                Filters.regex("airLineModel.departureCity.profiles.name", searchText)
            )
            val foundPurchase = purchaseModel.findOne(and(queryForSearchFilter))
            queryForItemFilter.add(
                Filters.eq(
                    "airLineModel.departureCity.id",
                    foundPurchase?.airLineModel?.departureCity?.id
                )
            )
        }

        queryForItemFilter.add(Filters.eq("airLineModel.userId", userId))

        if (filterByPriceRangeFrom != null || filterByPriceRangeTo != null) {
            queryForItemFilter.add(
                Filters.and(
                    filterByPriceRangeFrom?.let { Filters.gte("airLineModel.pricePerSeat", it) },
                    filterByPriceRangeTo?.let { Filters.lte("airLineModel.pricePerSeat", it) }
                )
            )
        }

        if (!filterByHotelIds.isNullOrEmpty()) {
            queryForItemFilter.add(Filters.`in`("airLineModel.departureCity.id", filterByHotelIds))
        }

        if (filterByDateFrom != null || filterByDateTo != null) {
            queryForItemFilter.add(
                Filters.and(
                    filterByDateFrom?.let { Filters.gte("createdAt", it) },
                    filterByDateTo?.let { Filters.lte("createdAt", it) }
                )
            )
        }

        if (filterByVisibility != null) {
            queryForItemFilter.add(Filters.eq("airLineModel.isVisible", filterByVisibility))
        }

        status?.let {
            queryForItemFilter.add(Filters.eq("status", it))
        }

        val finalQuery = and(queryForItemFilter)
        val totalCount = purchaseModel.countDocuments(finalQuery).toInt()
        val totalPages =
            if (totalCount % (if (pageSize == 0) 1 else pageSize) == 0) totalCount / (if (pageSize == 0) 1 else pageSize) else (totalCount / (if (pageSize == 0) 1 else pageSize)) + 1

        val hasPreviousPage = pageNumber > 1
        val hasNextPage = pageNumber < totalPages

        val data = purchaseModel.find(finalQuery)
            .skip(skip)
            .limit(pageSize)
            .toList()
            .map { purchase ->
                ResponsePurchasedHotelTicketModel(
                    id = purchase.id?.toHexString(),
                    hotelTicketModel = null,
                    airLineModel = purchase.airLineModel,
                    returnAirLineModel = purchase.returnAirLineModel,
                    userId = purchase.userId,
                    checkoutId = purchase.checkoutId,
                    status = purchase.status,
                    customerModel = purchase.customerModel,
                    createdAt = purchase.createdAt,
                    checkOutDate = purchase.checkOutDate,
                    checkInDate = purchase.checkInDate,
                    numberOfRooms = purchase.numberOfRooms,
                    airLineCustomerModels = purchase.airLineCustomerModels
                )
            }

        return PagingApiResponse(
            succeeded = true,
            data = data,
            currentPage = pageNumber,
            totalPages = totalPages,
            totalCount = totalCount,
            hasPreviousPage = hasPreviousPage,
            hasNextPage = hasNextPage,
            errorCode = errorCode
        )
    }

    override suspend fun approveHotelReservation(
        userId: String,
        purchasedId: String,
        ticketNumber: String
    ): ApiResponse<String?> {
        val foundPurchase = purchaseModel.findOne(PurchaseModel::id eq ObjectId(purchasedId))
            ?: return ApiResponse(data = "Error", succeeded = false)
        if(foundPurchase.airLineModel !=null){
            if (foundPurchase.status == null || foundPurchase.status == Status.PENDING.ordinal || foundPurchase.status == Status.EDITED_FOR_REJECTION.ordinal) {
                purchaseModel.updateOne(
                    PurchaseModel::id eq ObjectId(purchasedId),
                    combine(
                        setValue(PurchaseModel::status, Status.APPROVED.ordinal),
                        setValue(
                            PurchaseModel::airLineModel / ResponseAirlineTicketModel::ticketNumber,
                            ticketNumber
                        )
                    )
                )
                return ApiResponse(data = "Success", succeeded = true)
            } else if (foundPurchase.status == Status.EDITED.ordinal) {

                val merchantWalletAmount =
                    walletCollection.findOne(WalletAmountModel::userId eq userId)?.amount
                val merchantTotal = merchantWalletAmount?.plus(50)
                val merchantBlockToBookFees =
                    users.findOne(User::id eq userId)?.companyInfo?.blockToBookFees
                walletCollection.updateOne(
                    WalletAmountModel::userId eq userId,
                    setValue(WalletAmountModel::amount, merchantTotal)
                )
                transactionCollection.insertOne(
                    TransactionModel(
                        userId = userId,
                        blockToBookFees = merchantBlockToBookFees ?: 0.0,
                        amount = merchantTotal ?: 0.0,
                        chargerId = userId,
                        topUpType = TopUpType.EDIT_FEES.ordinal,
                        transactionType = TransactionType.PLUS.ordinal,
                        createdDate = System.currentTimeMillis(),
                        checkoutId = foundPurchase.checkoutId
                    )
                )
                //minus from buyer
                val buyerWalletAmount =
                    walletCollection.findOne(WalletAmountModel::userId eq foundPurchase.userId)?.amount
                val total = buyerWalletAmount?.minus(50)
                val blockToBookFees =
                    users.findOne(User::id eq foundPurchase.userId)?.companyInfo?.blockToBookFees
                walletCollection.updateOne(
                    WalletAmountModel::userId eq foundPurchase.userId,
                    setValue(WalletAmountModel::amount, total)
                )
                transactionCollection.insertOne(
                    TransactionModel(
                        userId = userId,
                        amount = blockToBookFees ?: 0.0,
                        blockToBookFees = blockToBookFees ?: 0.0,
                        chargerId = userId,
                        topUpType = TopUpType.EDIT_FEES.ordinal,
                        transactionType = TransactionType.MINUS.ordinal,
                        createdDate = System.currentTimeMillis(),
                        checkoutId = foundPurchase.checkoutId
                    )
                )
                purchaseModel.updateOne(
                    PurchaseModel::id eq ObjectId(purchasedId),
                    combine(
                        setValue(PurchaseModel::status, Status.APPROVED.ordinal),
                        setValue(
                            PurchaseModel::airLineModel / ResponseAirlineTicketModel::ticketNumber,
                            ticketNumber
                        )
                    )
                )
                return ApiResponse(data = "Success", succeeded = true)
            }
        }

        if (!isMyTimestampValid(foundPurchase.hotelTicketModel?.fromDate ?: 0)) {
            return ApiResponse(data = "Error", succeeded = false)
        }

        if (foundPurchase.status == null || foundPurchase.status == Status.PENDING.ordinal || foundPurchase.status == Status.EDITED_FOR_REJECTION.ordinal) {
            purchaseModel.updateOne(
                PurchaseModel::id eq ObjectId(purchasedId),
                combine(
                    setValue(PurchaseModel::status, Status.APPROVED.ordinal),
                    setValue(
                        PurchaseModel::hotelTicketModel / ResponseHotelTicketModel::ticketNumber,
                        ticketNumber
                    )
                )
            )
            return ApiResponse(data = "Success", succeeded = true)
        } else if (foundPurchase.status == Status.EDITED.ordinal) {

            val merchantWalletAmount =
                walletCollection.findOne(WalletAmountModel::userId eq userId)?.amount
            val merchantTotal = merchantWalletAmount?.plus(50)
            val merchantBlockToBookFees =
                users.findOne(User::id eq userId)?.companyInfo?.blockToBookFees
            walletCollection.updateOne(
                WalletAmountModel::userId eq userId,
                setValue(WalletAmountModel::amount, merchantTotal)
            )
            transactionCollection.insertOne(
                TransactionModel(
                    userId = userId,
                    blockToBookFees = merchantBlockToBookFees ?: 0.0,
                    amount = merchantTotal ?: 0.0,
                    chargerId = userId,
                    topUpType = TopUpType.EDIT_FEES.ordinal,
                    transactionType = TransactionType.PLUS.ordinal,
                    createdDate = System.currentTimeMillis(),
                    checkoutId = foundPurchase.checkoutId
                )
            )
            //minus from buyer
            val buyerWalletAmount =
                walletCollection.findOne(WalletAmountModel::userId eq foundPurchase.userId)?.amount
            val total = buyerWalletAmount?.minus(50)
            val blockToBookFees =
                users.findOne(User::id eq foundPurchase.userId)?.companyInfo?.blockToBookFees
            walletCollection.updateOne(
                WalletAmountModel::userId eq foundPurchase.userId,
                setValue(WalletAmountModel::amount, total)
            )
            transactionCollection.insertOne(
                TransactionModel(
                    userId = userId,
                    amount = blockToBookFees ?: 0.0,
                    blockToBookFees = blockToBookFees ?: 0.0,
                    chargerId = userId,
                    topUpType = TopUpType.EDIT_FEES.ordinal,
                    transactionType = TransactionType.MINUS.ordinal,
                    createdDate = System.currentTimeMillis(),
                    checkoutId = foundPurchase.checkoutId
                )
            )
            purchaseModel.updateOne(
                PurchaseModel::id eq ObjectId(purchasedId),
                combine(
                    setValue(PurchaseModel::status, Status.APPROVED.ordinal),
                    setValue(
                        PurchaseModel::hotelTicketModel / ResponseHotelTicketModel::ticketNumber,
                        ticketNumber
                    )
                )
            )
            return ApiResponse(data = "Success", succeeded = true)
        }
        return ApiResponse(data = "Error", succeeded = false)
    }

    override suspend fun rejectHotelReservation(
        userId: String,
        purchasedId: String,
        note: String
    ): ApiResponse<String?> {
        val foundPurchase = purchaseModel.findOne(PurchaseModel::id eq ObjectId(purchasedId))
            ?: return ApiResponse(data = "Error", succeeded = false)
        if(foundPurchase.airLineModel !=null){
            if (foundPurchase.status == Status.PENDING.ordinal) {
                purchaseModel.updateOne(
                    PurchaseModel::id eq ObjectId(purchasedId),
                    combine(
                        setValue(PurchaseModel::status, Status.REJECTED.ordinal),
                        setValue(
                            PurchaseModel::note,
                            note
                        )
                    )
                )
                return ApiResponse(data = "Success", succeeded = true)
            }
        }else{
            if (!isMyTimestampValid(foundPurchase.hotelTicketModel?.fromDate ?: 0)) {
                return ApiResponse(data = "Error", succeeded = false)
            }
            if (foundPurchase.status == Status.PENDING.ordinal) {
                purchaseModel.updateOne(
                    PurchaseModel::id eq ObjectId(purchasedId),
                    combine(
                        setValue(PurchaseModel::status, Status.REJECTED.ordinal),
                        setValue(
                            PurchaseModel::customerModel / CustomerModel::note,
                            note
                        )
                    )
                )
                return ApiResponse(data = "Success", succeeded = true)
            }

        }
        return ApiResponse(data = "Error", succeeded = false)
    }

    override suspend fun cancelHotelReservation(
        userId: String,
        purchasedId: String,
        note: String
    ): ApiResponse<String?> {
        val foundPurchase = purchaseModel.findOne(PurchaseModel::id eq ObjectId(purchasedId))
            ?: return ApiResponse(data = "Error", succeeded = false)
        if (!isMyTimestampValid(foundPurchase.hotelTicketModel?.fromDate ?: 0)) {
            return ApiResponse(data = "Error", succeeded = false)
        }
        val merchantWalletAmount =
            walletCollection.findOne(WalletAmountModel::userId eq userId)?.amount
        val merchantTotal =
            merchantWalletAmount?.minus(foundPurchase.hotelTicketModel?.pricePerNight ?: 0.0)
        val merchantBlockToBookFees =
            users.findOne(User::id eq userId)?.companyInfo?.blockToBookFees
        walletCollection.updateOne(
            WalletAmountModel::userId eq userId,
            setValue(WalletAmountModel::amount, merchantTotal)
        )
        transactionCollection.insertOne(
            TransactionModel(
                userId = userId,
                blockToBookFees = merchantBlockToBookFees ?: 0.0,
                amount = merchantTotal ?: 0.0,
                chargerId = userId,
                topUpType = TopUpType.CANCEL_FEES.ordinal,
                transactionType = TransactionType.MINUS.ordinal,
                createdDate = System.currentTimeMillis(),
                checkoutId = foundPurchase.checkoutId
            )
        )
        ////
        val buyerWalletAmount =
            walletCollection.findOne(WalletAmountModel::userId eq foundPurchase.userId)?.amount
        val total = buyerWalletAmount?.plus(foundPurchase.hotelTicketModel?.pricePerNight ?: 0.0)
        val blockToBookFees =
            users.findOne(User::id eq foundPurchase.userId)?.companyInfo?.blockToBookFees
        walletCollection.updateOne(
            WalletAmountModel::userId eq foundPurchase.userId,
            setValue(WalletAmountModel::amount, total)
        )
        transactionCollection.insertOne(
            TransactionModel(
                userId = userId,
                amount = blockToBookFees ?: 0.0,
                blockToBookFees = blockToBookFees ?: 0.0,
                chargerId = userId,
                topUpType = TopUpType.CANCEL_FEES.ordinal,
                transactionType = TransactionType.PLUS.ordinal,
                createdDate = System.currentTimeMillis(),
                checkoutId = foundPurchase.checkoutId
            )
        )
        purchaseModel.updateOne(
            PurchaseModel::id eq ObjectId(purchasedId),
            combine(
                setValue(PurchaseModel::status, Status.CANCELED.ordinal),
                setValue(
                    PurchaseModel::customerModel / CustomerModel::note,
                    note
                )
            )
        )
        return ApiResponse(data = "Success", succeeded = true)
    }

    fun isMyTimestampValid(timestamp: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        return currentTime < timestamp
    }


    private suspend fun processCartModel(
        cartModel: ResponseCartModel, checkoutId: String, userId: String
    ): ApiResponse<String?> {
        val hotelTicketModel = cartModel.hotelTicketModel
        if (hotelTicketModel != null) {
            purchaseModel.insertOne(
                cartModel.toPurchaseModel(
                    System.currentTimeMillis(),
                    checkoutId
                )
            )
            // Delete from cart
            cartDataSource.delete(userId, hotelTicketModel.id ?: "")

            val hotelPrice = cartModel.hotelTicketModel.pricePerNight ?: 0.0
            val airlinePrice = cartModel.airLineModel?.pricePerSeat ?: 0.0

            // Calculate the number of nights between check-in and check-out dates
            val fromDate = cartModel.checkInDate
            val toDate = cartModel.checkOutDate

            val nights = run {
                val checkInDate = LocalDate.ofEpochDay(fromDate / (24 * 60 * 60 * 1000))
                val checkOutDate = LocalDate.ofEpochDay(toDate / (24 * 60 * 60 * 1000))
                ChronoUnit.DAYS.between(checkInDate, checkOutDate).toInt().coerceAtLeast(1)
            }
            // Wallet operations
            updateWallets(
                buyerId = cartModel.userId,
                sellerId = hotelTicketModel.userId ?: "",
                cartAmount = (hotelPrice + airlinePrice) * nights,
                checkoutId = checkoutId
            )
        } else {
            purchaseModel.insertOne(
                cartModel.toPurchaseModel(
                    System.currentTimeMillis(),
                    checkoutId
                )
            )
            cartDataSource.delete(userId, cartModel.id ?: "")
            val total = if (cartModel.returnAirLineTicketModel != null) {
                if (cartModel.returnAirLineTicketModel.roundTripId == cartModel.airLineModel?.roundTripId) {
                    (cartModel.airLineModel?.pricePerSeatRoundTrip ?: 0.0) * cartModel.numberOfRooms
                } else {
                    ((cartModel.airLineModel?.pricePerSeat
                        ?: 0.0) + (cartModel.returnAirLineTicketModel.pricePerSeat)) * cartModel.numberOfRooms
                }
            } else {
                (cartModel.airLineModel?.pricePerSeat ?: 0.0) * cartModel.numberOfRooms
            }
            updateWallets(
                buyerId = cartModel.userId,
                sellerId = cartModel.airLineModel?.userId ?: "",
                cartAmount = total,
                checkoutId = checkoutId
            )
        }
        return ApiResponse(data = "Success", succeeded = true)
    }

    private suspend fun updateBuyerWallet(
        userId: String, updatedAmount: Double, blockToBookFees: Double, checkoutId: String
    ) {
        walletCollection.updateOne(
            WalletAmountModel::userId eq userId,
            setValue(WalletAmountModel::amount, updatedAmount)
        )
        adminWalletAmount.insertOne(
            AdminWalletAmount(
                amount = blockToBookFees,
                createdDate = System.currentTimeMillis(),
                merchantId = userId,
                checkoutId = checkoutId
            )
        )
        transactionCollection.insertOne(
            TransactionModel(
                userId = userId,
                amount = blockToBookFees,
                blockToBookFees = blockToBookFees,
                chargerId = userId,
                topUpType = TopUpType.BLOCK_TO_BOOK_FEES.ordinal,
                transactionType = TransactionType.MINUS.ordinal,
                createdDate = System.currentTimeMillis(),
                checkoutId = checkoutId
            )
        )
    }

    override suspend fun createOrUpdateCustomerInfo(
        userId: String, createCustomerModel: CreateCustomerModel
    ): ApiResponse<String?> {
        val foundPurchase = purchaseModel.findOne(
            PurchaseModel::id eq ObjectId(createCustomerModel.purchasedHotelModelId)
        )
        if (foundPurchase?.customerModel == null) {
            purchaseModel.updateOne(
                PurchaseModel::id eq ObjectId(createCustomerModel.purchasedHotelModelId),
                setValue(PurchaseModel::customerModel, createCustomerModel.toCustomerModel())
            )
            purchaseModel.updateOne(
                PurchaseModel::id eq ObjectId(createCustomerModel.purchasedHotelModelId),
                setValue(PurchaseModel::status, Status.PENDING.ordinal)
            )
        } else if (foundPurchase.status == Status.REJECTED.ordinal) {
            // add to merchant
            purchaseModel.updateOne(
                PurchaseModel::id eq ObjectId(createCustomerModel.purchasedHotelModelId),
                setValue(PurchaseModel::customerModel, createCustomerModel.toCustomerModel())
            )
            purchaseModel.updateOne(
                PurchaseModel::id eq ObjectId(createCustomerModel.purchasedHotelModelId),
                setValue(PurchaseModel::status, Status.EDITED_FOR_REJECTION.ordinal)
            )
        } else {
            // add to merchant
            purchaseModel.updateOne(
                PurchaseModel::id eq ObjectId(createCustomerModel.purchasedHotelModelId),
                setValue(PurchaseModel::customerModel, createCustomerModel.toCustomerModel())
            )
            purchaseModel.updateOne(
                PurchaseModel::id eq ObjectId(createCustomerModel.purchasedHotelModelId),
                setValue(PurchaseModel::status, Status.EDITED.ordinal)
            )
        }
        return ApiResponse(data = "Success", succeeded = true)
    }


    override suspend fun createOrUpdateCustomerInfoList(
        userId: String, createCustomerModel: CreateCustomerListModel
    ): ApiResponse<String?> {
        val foundPurchase = purchaseModel.findOne(
            PurchaseModel::id eq ObjectId(createCustomerModel.purchasedAirlineTicketId)
        )
        if (foundPurchase?.airLineCustomerModels == null) {
            purchaseModel.updateOne(
                PurchaseModel::id eq ObjectId(createCustomerModel.purchasedAirlineTicketId),
                setValue(
                    PurchaseModel::airLineCustomerModels,
                    createCustomerModel.createCustomerListModel.toCustomerModelList()
                )
            )
            purchaseModel.updateOne(
                PurchaseModel::id eq ObjectId(createCustomerModel.purchasedAirlineTicketId),
                setValue(PurchaseModel::status, Status.PENDING.ordinal)
            )
        } else if (foundPurchase.status == Status.REJECTED.ordinal) {
            // add to merchant
            purchaseModel.updateOne(
                PurchaseModel::id eq ObjectId(createCustomerModel.purchasedAirlineTicketId),
                setValue(
                    PurchaseModel::airLineCustomerModels,
                    createCustomerModel.createCustomerListModel.toCustomerModelList()
                )
            )
            purchaseModel.updateOne(
                PurchaseModel::id eq ObjectId(createCustomerModel.purchasedAirlineTicketId),
                setValue(PurchaseModel::status, Status.EDITED_FOR_REJECTION.ordinal)
            )
        } else {
            // add to merchant
            purchaseModel.updateOne(
                PurchaseModel::id eq ObjectId(createCustomerModel.purchasedAirlineTicketId),
                setValue(
                    PurchaseModel::airLineCustomerModels,
                    createCustomerModel.createCustomerListModel.toCustomerModelList()
                )
            )
            purchaseModel.updateOne(
                PurchaseModel::id eq ObjectId(createCustomerModel.purchasedAirlineTicketId),
                setValue(PurchaseModel::status, Status.EDITED.ordinal)
            )
        }
        return ApiResponse(data = "Success", succeeded = true)
    }


    private suspend fun updateWallets(
        buyerId: String, sellerId: String, cartAmount: Double, checkoutId: String
    ) {
        // Parallel wallet operations for buyer and seller
        coroutineScope {
            val buyerWalletUpdate = async {
                walletCollection.updateOne(
                    WalletAmountModel::userId eq buyerId,
                    inc(WalletAmountModel::amount, -cartAmount.toDoubleAmount())
                )
            }

            val sellerWalletUpdate = async {
                walletCollection.updateOne(
                    WalletAmountModel::userId eq sellerId,
                    inc(WalletAmountModel::amount, cartAmount.toDoubleAmount())
                )
            }

            buyerWalletUpdate.await()
            sellerWalletUpdate.await()

            // Record transactions
            transactionCollection.insertMany(
                listOf(
                    createTransaction(
                        buyerId,
                        cartAmount.toDoubleAmount(),
                        checkoutId,
                        TransactionType.MINUS
                    ),
                    createTransaction(
                        sellerId,
                        cartAmount.toDoubleAmount(),
                        checkoutId,
                        TransactionType.PLUS
                    )
                )
            )
        }
    }

    private suspend fun createTransaction(
        userId: String, amount: Double, checkoutId: String, type: TransactionType
    ): TransactionModel {
        return TransactionModel(
            userId = userId,
            amount = amount,
            blockToBookFees = users.findOne(User::id eq userId)?.companyInfo?.blockToBookFees
                ?: 0.0,
            chargerId = userId,
            topUpType = TopUpType.PURCHASE.ordinal,
            transactionType = type.ordinal,
            createdDate = System.currentTimeMillis(),
            checkoutId = checkoutId
        )
    }

    public fun generateUniqueToken(length: Int = 24): String {
        val characters = "4039fkeoidfwm0ef90329mifwe2039"
        val secureRandom = SecureRandom()
        return (1..length).map { characters.random(secureRandom.asKotlinRandom()) }.joinToString("")
    }
}
