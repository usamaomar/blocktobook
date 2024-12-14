package com.example.domain.model.userModel

 import com.example.domain.model.profileModel.CompanyInfoModel
import com.example.domain.model.subscriptionModel.Subscription
import com.example.util.AccessRole
import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String?,
    val name: String?,
    val emailAddress: String,
    val profilePhoto: String,
    val subscription: Subscription? = null,
    val accessRole: AccessRole? = null,
    val companyInfo: CompanyInfoModel? = null
) : Principal
