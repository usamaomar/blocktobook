package com.example.data.repository.cityDataSource

import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.profileModel.CompanyInfoModel
import com.example.domain.model.profileModel.CreateCompanyInfoModel
import com.example.domain.model.profileModel.VerifiedCompanyInfoModel
import com.example.domain.model.userModel.User

interface ProfileDataSource {
    suspend fun updateCompanyInfo(userId: String?, companyInfoModel: CompanyInfoModel): ApiResponse<CompanyInfoModel?>?
    suspend fun updateUserCompanyInfo(userId: String?, companyInfoModel: CompanyInfoModel): ApiResponse<String?>?

    suspend fun updateSubscription(
        userId: String,
        subscription: Int
    ):  ApiResponse<User?>?
    suspend fun adminUserApprove(
        verifiedCompanyInfoModel: VerifiedCompanyInfoModel
    ): ApiResponse<CompanyInfoModel?>
}
