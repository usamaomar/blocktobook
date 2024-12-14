package com.example.data.repository.userDataSource

import com.example.domain.model.publicModel.ApiResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import java.io.File

class UploadDataSourceImpl : UploadDataSource {

    private val errorCode: Int = 14


    override suspend fun create(multiPart: MultiPartData): ApiResponse<String?> {
        var fileName: String? = null
        var fileExtension: String? = null
        var filePath: String? = null
        multiPart.forEachPart { part ->
            if (part is PartData.FileItem) {
                fileName = part.originalFileName
                fileExtension = File(part.originalFileName ?: "").extension
                if (fileExtension != "jpg" && fileExtension != "jpeg" && fileExtension != "png") {
                ApiResponse(
                        data = null,
                        succeeded = false,
                        message = arrayListOf("Only JPG, JPEG, and PNG files are allowed"),
                        errorCode = errorCode
                    )
                    return@forEachPart
                }

                val uploadsDir = File("uploads")
                if (!uploadsDir.exists()) {
                    uploadsDir.mkdirs()  // Create the directory if it doesn't exist
                }

                val file = File(uploadsDir, fileName!!)
                part.streamProvider().use { its ->
                    file.outputStream().buffered().use {
                        its.copyTo(it)
                    }
                }
                filePath = file.absolutePath  // Get the full path to the uploaded file
            }
            part.dispose()
        }
        return  if (filePath != null) {
            ApiResponse(
                data = "$filePath",
                succeeded = true,
                message = arrayListOf(),
                errorCode = errorCode
            )
        } else {
            ApiResponse(
                data = "$filePath",
                succeeded = false,
                message = arrayListOf(),
                errorCode = errorCode, code = HttpStatusCode.BadRequest.value
            )
        }
    }


}