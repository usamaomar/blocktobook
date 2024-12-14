package com.example.data.repository.sendGrid

import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.userModel.User
import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Email
import com.sendgrid.helpers.mail.objects.Personalization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import java.io.IOException

class SendGridDataSourceImpl (database: CoroutineDatabase) : SendGridDataSource {
    private val errorCode: Int = 12444


    private val users = database.getCollection<User>()


    override suspend fun sendEmailUsingSendGrid(
        userId: String,
        amount: String,
    ): ApiResponse<String?> {
        return withContext(Dispatchers.IO) {
            try {
                // Create a SendGrid instance
                val user = users.findOne(filter = User::id eq "JXcGq1dAfjao3cZAqZUCcZ2Mnms1")
                val sendGrid = SendGrid("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")
                val email = Mail().apply {
                    from = Email(user?.emailAddress)
                    subject = "Recharge wallet"
                    templateId = "d-d8e377392fb0422a97335598a27e0ae8"
                    // Recipient saucer
                    val personalization = Personalization().apply {
                        addTo(Email("usamaomarsoftware@gmail.com"))
                        addDynamicTemplateData("name", user?.name)
                        addDynamicTemplateData("amount", amount)

                    }
                    addPersonalization(personalization)
                }
                // Prepare the request
                val request = Request().apply {
                    method = Method.POST
                    endpoint = "mail/send"
                    body = email.build()
                }

                // Execute the request
                val response = sendGrid.api(request)

                // Check the response status
                return@withContext if (response.statusCode == 202) {
                    // Success, return a success response
                    ApiResponse(
                        data = "Email sent successfully",
                        succeeded = true,
                        errorCode = null
                    )
                } else {
                    // Error, return a failure response with details
                    ApiResponse(
                        data = null,
                        succeeded = false,
                        message = arrayListOf("Failed to send email, status code: ${response.statusCode}"),
                        errorCode = errorCode
                    )
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
                // Return a failure response in case of exception
                ApiResponse(
                    data = null,
                    succeeded = false,
                    message = arrayListOf("Exception occurred: ${ex.message}"),
                    errorCode = errorCode
                )
            }
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