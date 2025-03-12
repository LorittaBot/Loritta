package net.perfectdreams.loritta.morenitta.website.utils.config.types

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.ktor.http.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.DonationKey
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class ActiveDonationKeysTransformer(val loritta: LorittaBot) : ConfigTransformer {
    override val payloadType: String = "activekeys"
    override val configKey: String = "activeDonationKeys"

    override suspend fun fromJson(userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        val keyIds = payload["keyIds"].array.map { it.long }
        val currentlyActiveKeys = loritta.newSuspendedTransaction {
            DonationKeys.selectAll().where { DonationKeys.activeIn eq serverConfig.id }
                .map { it[DonationKeys.id].value }
        }

        val validKeys = mutableListOf<Long>()

        for (keyId in keyIds) {
            val donationKey = loritta.newSuspendedTransaction {
                DonationKey.findById(keyId)
            } ?: throw WebsiteAPIException(HttpStatusCode.Forbidden,
                WebsiteUtils.createErrorPayload(
                    loritta,
                    LoriWebCode.FORBIDDEN,
                    "loritta.errors.keyDoesntExist"
                )
            )

            if (donationKey.userId != userIdentification.id.toLong() && keyId !in currentlyActiveKeys)
                throw WebsiteAPIException(HttpStatusCode.Forbidden,
                    WebsiteUtils.createErrorPayload(
                        loritta,
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
                "user" to WebsiteUtils.transformToJson(loritta.lorittaShards.retrieveUserById(it.userId)!!)
            )
        }
        return array.toJsonArray()
    }
}