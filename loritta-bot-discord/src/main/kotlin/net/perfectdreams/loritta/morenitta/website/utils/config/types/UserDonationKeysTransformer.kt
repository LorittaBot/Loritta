package net.perfectdreams.loritta.morenitta.website.utils.config.types

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.perfectdreams.loritta.morenitta.dao.DonationKey
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import org.jetbrains.exposed.sql.and

class UserDonationKeysTransformer(val loritta: LorittaBot) : ConfigTransformer {
    override val payloadType: String = "userkeys"
    override val configKey: String = "donationKeys"

    override suspend fun toJson(userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig): JsonElement {
        val userDonationKeys = loritta.newSuspendedTransaction {
            DonationKey.find { DonationKeys.userId eq userIdentification.id.toLong() and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
                    .toList()
        }
        
        val serverInfo = mutableMapOf<Long, JsonObject?>()
        
        val guildIdsToBeQueried = loritta.newSuspendedTransaction {
            userDonationKeys.filter { it.activeIn != null }.mapNotNull {
                it.activeIn?.guildId
            }.distinct()
        }

        val jobs = guildIdsToBeQueried.map {
            GlobalScope.async {
                serverInfo[it] = loritta.lorittaShards.queryGuildById(it)
            }
        }

        jobs.awaitAll()

        val array = userDonationKeys.map {
            jsonObject(
                    "id" to it.id.value,
                    "value" to it.value,
                    "expiresAt" to it.expiresAt,
                    "user" to WebsiteUtils.transformToJson(loritta.lorittaShards.retrieveUserById(it.userId)!!),
                    "activeIn" to loritta.newSuspendedTransaction { it.activeIn?.guildId?.let { serverInfo[it] } }
            )
        }
        return array.toJsonArray()
    }
}