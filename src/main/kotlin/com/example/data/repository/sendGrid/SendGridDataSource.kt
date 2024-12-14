package com.example.data.repository.sendGrid

import com.example.domain.model.publicModel.ApiResponse

interface SendGridDataSource {

    suspend fun sendEmailUsingSendGrid(
        userId: String,
        amount: String,
    ):ApiResponse<String?>
}