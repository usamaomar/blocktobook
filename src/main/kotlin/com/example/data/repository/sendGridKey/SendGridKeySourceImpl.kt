package com.example.data.repository.sendGridKey

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.InputStream
import java.util.Base64

class SendGridKeySourceImpl()  : SendGridKeyDataSource {

    override suspend fun getSendGridKey(): String {
        val serviceAccount: InputStream? =
            this::class.java.classLoader.getResourceAsStream("ktor-sendgrid.json")
        val mapper = jacksonObjectMapper()
        val config: JsonNode = mapper.readTree(serviceAccount)
       return decodeBase64(config["send_gr_key"].asText())
    }

    private fun decodeBase64(encodedString: String): String {
        val decodedBytes = Base64.getDecoder().decode(encodedString)
        return String(decodedBytes, Charsets.UTF_8)
    }
}