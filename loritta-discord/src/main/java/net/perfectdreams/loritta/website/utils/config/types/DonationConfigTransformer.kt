package net.perfectdreams.loritta.website.utils.config.types

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullBool
import com.github.salomonbrys.kotson.nullString
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.DonationConfig
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.statement.HttpResponse
import io.ktor.http.userAgent
import net.dv8tion.jda.api.entities.Guild

object DonationConfigTransformer : ConfigTransformer {
    override val payloadType: String = "donation"
    override val configKey: String = "donationConfig"

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        loritta.newSuspendedTransaction {
            val donationConfig = serverConfig.donationConfig ?: DonationConfig.new {
                this.dailyMultiplier = false
                this.customBadge = false
            }
            donationConfig.dailyMultiplier = payload["dailyMultiplier"].nullBool ?: donationConfig.dailyMultiplier
            donationConfig.customBadge = payload["customBadge"].nullBool ?: donationConfig.customBadge

            serverConfig.donationConfig = donationConfig
        }

        val data = payload["badgeImage"].nullString

        if (data != null) {
            loritta.http.patch<HttpResponse>("${loritta.instanceConfig.loritta.website.url}/api/v1/guilds/${guild.idLong}/badge") {
                userAgent(loritta.lorittaCluster.getUserAgent())
                header("Authorization", loritta.lorittaInternalApiKey.name)

                body = gson.toJson(
                        jsonObject(
                                "badge" to data
                        )
                )
            }
        }
    }

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        return loritta.newSuspendedTransaction {
            jsonObject(
                    "customBadge" to (serverConfig.donationConfig?.customBadge ?: false),
                    "dailyMultiplier" to (serverConfig.donationConfig?.dailyMultiplier ?: false)
            )
        }
    }
}