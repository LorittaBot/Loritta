package net.perfectdreams.loritta.morenitta.utils

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import net.perfectdreams.loritta.common.utils.JsonIgnoreUnknownKeys

object ApplicationInfoUtils {
    suspend fun getApplicationInfo(http: HttpClient, applicationId: Long): ApplicationInfo? {
        val applicationInfoResponse = http.get("https://discord.com/api/v10/applications/$applicationId/rpc")

        if (applicationInfoResponse.status != HttpStatusCode.OK)
            return null

        return JsonIgnoreUnknownKeys.decodeFromString(applicationInfoResponse.bodyAsText())
    }

    @Serializable
    data class ApplicationInfo(
        val id: Long,
        val name: String,
        val icon: String? = null,
        val description: String,
        val type: Int?,
        @SerialName("cover_image")
        val coverImage: String? = null,
        @SerialName("primary_sku_id")
        val primarySkuId: String? = null,
        val slug: String? = null,
        @SerialName("guild_id")
        val guildId: Long? = null,
        @SerialName("bot_public")
        val botPublic: Boolean,
        @SerialName("bot_require_code_grant")
        val botRequireCodeGrant: Boolean,
        @SerialName("custom_install_url")
        val customInstallUrl: String? = null,
        @SerialName("install_params")
        val installParams: InstallParams? = null,
        @SerialName("verify_key")
        val verifyKey: String,
        @SerialName("terms_of_service_url")
        val termsOfServiceUrl: String? = null,
        @SerialName("privacy_policy_url")
        val privacyPolicyUrl: String? = null,
        val flags: Long,
        val tags: List<String>? = null
    )

    @Serializable
    data class InstallParams(
        val scopes: List<String>,
        val permissions: Long
    )
}