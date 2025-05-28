package com.example.data.repository.visaDataSource

import com.example.data.repository.userDataSource.UploadDataSource
import com.example.domain.model.publicModel.ApiResponse
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import java.io.File

class VisaDataSourceImpl : VisaDataSource {

    private val errorCode: Int = 14


    override suspend fun create(multiPart: MultiPartData): ApiResponse<String?> {
        var fileName: String? = null
        var fileExtension: String? = null
        var fileBytes: ByteArray? = null

        multiPart.forEachPart { part ->
            if (part is PartData.FileItem) {
                fileName = part.originalFileName
                fileExtension = File(part.originalFileName ?: "").extension.lowercase()

                if (fileExtension !in listOf("jpg", "jpeg", "png")) {
                    part.dispose()
                     ApiResponse(
                        data = null,
                        succeeded = false,
                        message = arrayListOf("Only JPG, JPEG, and PNG files are allowed"),
                        errorCode = errorCode,
                        code = HttpStatusCode.BadRequest.value
                    )
                }

                fileBytes = part.streamProvider().readBytes()
            }
            part.dispose()
        }

        if (fileName == null || fileBytes == null) {
            return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("File upload failed"),
                errorCode = errorCode,
                code = HttpStatusCode.BadRequest.value
            )
        }

        try {
            val bucketName = "hayyakblockstorege" // ✅ Replace with your bucket name
            val storage = StorageOptions.getDefaultInstance().service
            val blobInfo = BlobInfo.newBuilder(bucketName, fileName!!)
                .setContentType("image/$fileExtension")
                .build()
            storage.create(blobInfo, fileBytes)

            val publicUrl = "https://storage.googleapis.com/$bucketName/$fileName"

            return ApiResponse(
                data = publicUrl,
                succeeded = true,
                message = arrayListOf("File uploaded successfully"),
                errorCode = errorCode
            )

        } catch (e: Exception) {
            return ApiResponse(
                data = null,
                succeeded = false,
                message = arrayListOf("Failed to upload: ${e.localizedMessage}"),
                errorCode = errorCode,
                code = HttpStatusCode.InternalServerError.value
            )
        }
    }



}