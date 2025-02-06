package com.example.data.repository.sendGridKey


interface SendGridKeyDataSource {
    suspend fun getSendGridKey():String

}