package com.example.data.repository.sendGrid

import com.example.data.repository.sendGridKey.SendGridKeyDataSource
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.purchaseModel.PurchaseModel
import com.example.domain.model.userModel.User
import com.example.util.AccessRole
import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import com.sendgrid.helpers.mail.objects.Personalization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import java.io.IOException

class SendGridDataSourceImpl(database: CoroutineDatabase) : SendGridDataSource {
    private val errorCode: Int = 12444

    val searchDataSource: SendGridKeyDataSource by KoinJavaComponent.inject(SendGridKeyDataSource::class.java)


    private val users = database.getCollection<User>()
    private val purchaseModel = database.getCollection<PurchaseModel>()


    override suspend fun sendEmailUsingSendGrid(
        userId: String,
        amount: String,
    ): ApiResponse<String?> {
        return withContext(Dispatchers.IO) {
            try {
                val user = users.findOne(filter = User::id eq userId)
                val sendGrid = SendGrid(searchDataSource.getSendGridKey())

                // Fetch all admin users
                val adminUsers = users.find(User::accessRole eq AccessRole.Admin).toList()

                val email = Mail().apply {
                    from = Email(user?.emailAddress)
                    subject = "Recharge wallet"
                    templateId = "d-d8e377392fb0422a97335598a27e0ae8"

                    // Add all admins to the email recipients
                    val personalization = Personalization().apply {
                        adminUsers.forEach { adminUser ->
                            addTo(Email(adminUser.emailAddress))
                        }
                        addDynamicTemplateData("name", user?.name)
                        addDynamicTemplateData("amount", amount)
                    }
                    addPersonalization(personalization)
                }

                val request = Request().apply {
                    method = Method.POST
                    endpoint = "mail/send"
                    body = email.build()
                }

                val response = sendGrid.api(request)

                return@withContext if (response.statusCode == 202) {
                    ApiResponse(
                        data = "Email sent successfully",
                        succeeded = true,
                        errorCode = null
                    )
                } else {
                    ApiResponse(
                        data = null,
                        succeeded = false,
                        message = arrayListOf("Failed to send email, status code: ${response.statusCode}"),
                        errorCode = errorCode
                    )
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
                ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("Exception occurred: ${ex.message}"),
                    errorCode = errorCode
                )
            }
        }
    }


    override suspend fun sendTestSendGrid(
        fromEmail: String,
        toEmail: String,
        text: String,
    ): ApiResponse<String?> {
        return withContext(Dispatchers.IO) {
            try {
                val sendGrid = SendGrid(searchDataSource.getSendGridKey())
                // Iterate through each admin user and send an email
                    val email = Mail().apply {
                        from =
                            Email(fromEmail) // Replace with your sender email
                        subject = "Important Notification"
                        addContent(
                            Content(
                                "text/plain",
                                "$text"
                            )
                        ) // Plain text email body
                        addPersonalization(
                            Personalization().apply {
                                addTo(Email(toEmail))
                            }
                        )
                    }
                    // Prepare the request for each email
                    val request = Request().apply {
                        method = Method.POST
                        endpoint = "mail/send"
                        body = email.build()
                    }
                    // Execute the request
                    val response = sendGrid.api(request)
                    // If sending fails for any admin, return failure response
                    if (response.statusCode != 202) {
                        return@withContext ApiResponse(
                            data = null,
                            succeeded = false,
                            message = arrayListOf("Failed to send email to ${toEmail}, status code: ${response.statusCode}${response.body}"),
                            errorCode = errorCode
                        )
                    }
                ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("Exception occurred:"),
                    errorCode = errorCode
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("Exception occurred: ${ex.message}"),
                    errorCode = errorCode
                )
            }
        }
    }


    override suspend fun sendEmailToAllAdminsUsingSendGrid(
        merchantId: String,
        actionMessage: String,
    ): ApiResponse<String?> {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch the merchant and all admin users
                val merchant = users.findOne(filter = User::id eq merchantId)
                val adminUsers = users.find(User::accessRole eq AccessRole.Admin).toList()

                if (adminUsers.isEmpty()) {
                    return@withContext ApiResponse(
                        data = null,
                        succeeded = false,
                        message = arrayListOf("No admin users found"),
                        errorCode = errorCode
                    )
                }

                val sendGrid = SendGrid(searchDataSource.getSendGridKey())

                // Iterate through each admin user and send an email
                adminUsers.forEach { admin ->
                    val email = Mail().apply {
                        from =
                            Email("usamaomarsoftware@gmail.com") // Replace with your sender email
                        subject = "Important Notification"
                        addContent(
                            Content(
                                "text/plain",
                                "${merchant?.emailAddress} ${actionMessage}"
                            )
                        ) // Plain text email body
                        addPersonalization(
                            Personalization().apply {
                                addTo(Email(admin.emailAddress))
                            }
                        )
                    }

                    // Prepare the request for each email
                    val request = Request().apply {
                        method = Method.POST
                        endpoint = "mail/send"
                        body = email.build()
                    }

                    // Execute the request
                    val response = sendGrid.api(request)

                    // If sending fails for any admin, return failure response
                    if (response.statusCode != 202) {
                        return@withContext ApiResponse(
                            data = null,
                            succeeded = false,
                            message = arrayListOf("Failed to send email to ${admin.emailAddress}, status code: ${response.statusCode}"),
                            errorCode = errorCode
                        )
                    }
                }

                // Return success if all emails are sent
                ApiResponse(
                    data = "Emails sent successfully to all admins",
                    succeeded = true,
                    errorCode = null
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("Exception occurred: ${ex.message}"),
                    errorCode = errorCode
                )
            }
        }
    }


    override suspend fun confiramtionOfAccountApprove(
        merchantId: String,
        adminId: String,
        actionMessage: String,
    ): ApiResponse<String?> {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch the merchant and all admin users
                val merchant = users.findOne(filter = User::id eq merchantId)
                val admin = users.findOne(filter = User::id eq adminId)
                val sendGrid = SendGrid(searchDataSource.getSendGridKey())
                // Iterate through each admin user and send an email
                val email = Mail().apply {
                    from = Email(admin?.emailAddress) // Replace with your sender email
                    subject = "Hayyak BlockToBook"
                    addContent(Content("text/plain", actionMessage)) // Plain text email body
                    addPersonalization(
                        Personalization().apply {
                            addTo(Email(merchant?.emailAddress))
                        }
                    )
                }

                // Prepare the request for each email
                val request = Request().apply {
                    method = Method.POST
                    endpoint = "mail/send"
                    body = email.build()
                }

                // Execute the request
                val response = sendGrid.api(request)

                // If sending fails for any admin, return failure response
                if (response.statusCode != 202) {
                    return@withContext ApiResponse(
                        data = null,
                        succeeded = false,
                        message = arrayListOf("Failed to send email to ${admin?.emailAddress}, status code: ${response.statusCode}"),
                        errorCode = errorCode
                    )
                }

                // Return success if all emails are sent
                ApiResponse(
                    data = "Emails sent successfully to all admins",
                    succeeded = true,
                    errorCode = null
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
                ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("Exception occurred: ${ex.message}"),
                    errorCode = errorCode
                )
            }
        }
    }

    override suspend fun sendEmailToAdminAndMerchantAfterUpdatingCustomer(
        merchantId: String,
        ticketId: String
    ): ApiResponse<String?> = withContext(Dispatchers.IO) {
        try {
            // Fetch the purchase record and determine the owner ID
            val foundPurchase = purchaseModel.findOne(PurchaseModel::id eq ObjectId(ticketId))
            val ownerId = when {
                foundPurchase?.hotelTicketModel != null -> foundPurchase.hotelTicketModel.userId
                foundPurchase?.airLineModel != null -> foundPurchase.airLineModel.userId
                else -> null
            }

            // Validate the owner and merchant models
            val ownerModel = users.findOne(filter = User::id eq ownerId)
                ?: return@withContext ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("Owner not found"),
                    errorCode = errorCode
                )

            val merchantModel = users.findOne(filter = User::id eq merchantId)
                ?: return@withContext ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("Merchant not found"),
                    errorCode = errorCode
                )

            // Retrieve all admin users
            val adminUsers = users.find(User::accessRole eq AccessRole.Admin).toList()
            if (adminUsers.isEmpty()) {
                return@withContext ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("No admin users found"),
                    errorCode = errorCode
                )
            }

            // Send email notifications using SendGrid
            val sendGrid = SendGrid(searchDataSource.getSendGridKey())

            // Function to send an email
            suspend fun sendEmail(to: String, subject: String, content: String): Boolean {
                val email = Mail().apply {
                    from = Email("usamaomarsoftware@gmail.com") // Replace with your sender email
                    this.subject = subject
                    addContent(Content("text/plain", content))
                    addPersonalization(Personalization().apply { addTo(Email(to)) })
                }

                val request = Request().apply {
                    method = Method.POST
                    endpoint = "mail/send"
                    body = email.build()
                }

                val response = sendGrid.api(request)
                return response.statusCode == 202
            }

            // Email content for owner
            val ownerEmailContent = "${merchantModel.emailAddress} قام باضافة زبائن جدد"
            if (!sendEmail(ownerModel.emailAddress, "Important Notification", ownerEmailContent)) {
                return@withContext ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("Failed to send email to owner: ${ownerModel.emailAddress}"),
                    errorCode = errorCode
                )
            }

            // Email content for admins
            adminUsers.forEach { admin ->
                val adminEmailContent = "${merchantModel.emailAddress} قام باضافة زبائن جدد"
                if (!sendEmail(admin.emailAddress, "Important Notification", adminEmailContent)) {
                    return@withContext ApiResponse(
                        data = null,
                        succeeded = false,
                        message = arrayListOf("Failed to send email to admin: ${admin.emailAddress}"),
                        errorCode = errorCode
                    )
                }
            }

            // Return success response
            ApiResponse(
                data = "Emails sent successfully to owner and all admins",
                succeeded = true,
                errorCode = null
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Exception occurred: ${ex.message}"),
                errorCode = errorCode
            )
        }
    }



    // Function to build email body (HTML format)
    private fun buildArabicEmailBody(name: String, amount: Double, path: String): String {
        return """
        <html>
        <body style="direction: rtl; text-align: right;">
            <h1>مرحبًا، $name</h1>
            <p>هل يمكن ان تقوم بشحن رصيدي بالمبلغ $$amount قد تم بنجاح.</p>
            <p>يمكنك عرض الإيصال الخاص بك من خلال <a href="$path">هذا الرابط</a>.</p>
            <br>
            <p>شكرًا لاستخدامك لخدماتنا!</p>
        </body>
        </html>
    """.trimIndent()
    }


}