package net.perfectdreams.loritta.website.utils.config.types

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.tables.DonationKeys
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.http.HttpStatusCode
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

object ActiveDonationKeysTransformer : ConfigTransformer {
    override val payloadType: String = "activekeys"
    override val configKey: String = "activeDonationKeys"

    override suspend fun fromJson(userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        val keyIds = payload["keyIds"].array.map { it.long }
        val currentlyActiveKeys = loritta.newSuspendedTransaction {
            DonationKeys.select { DonationKeys.activeIn eq serverConfig.id }
                    .map { it[DonationKeys.id].value }
        }

        val validKeys = mutableListOf<Long>()

        for (keyId in keyIds) {
            val donationKey = loritta.newSuspendedTransaction {
                DonationKey.findById(keyId)
            } ?: throw WebsiteAPIException(HttpStatusCode.Forbidden,
                    WebsiteUtils.createErrorPayload(
                            LoriWebCode.FORBIDDEN,
                            "loritta.errors.keyDoesntExist"
                    )
            )

            if (donationKey.userId != userIdentification.id.toLong() && keyId !in currentlyActiveKeys)
                throw WebsiteAPIException(HttpStatusCode.Forbidden,
                        WebsiteUtils.createErrorPayload(
                                LoriWebCode.FORBIDDEN,
                                "loritta.errors.tryingToApplyKeyOfAnotherUser"
                        )
                )

            validKeys.add(keyId)
        }

        val deactivedKeys = currentlyActiveKeys.toMutableList().apply { this.removeAll(validKeys) }

        loritta.newSuspendedTransaction {
            DonationKeys.update({ DonationKeys.id inList deactivedKeys }) {
                it[activeIn] = null
            }
            DonationKeys.update({ DonationKeys.id inList validKeys }) {
                it[activeIn] = serverConfig.id
            }
        }
    }

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        val activeDonationKeys = loritta.newSuspendedTransaction {
            DonationKey.find { DonationKeys.activeIn eq serverConfig.id and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
                    .toList()
        }

        val array = activeDonationKeys.map {
            jsonObject(
                    "id" to it.id.value,
                    "value" to it.value,
                    "expiresAt" to it.expiresAt,
                    "user" to WebsiteUtils.transformToJson(lorittaShards.retrieveUserById(it.userId)!!)
            )
        }
        return array.toJsonArray()
    }
}