package net.perfectdreams.loritta.website.utils.config.types

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.tables.DonationKeys
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.and

object UserDonationKeysTransformer : ConfigTransformer {
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
                serverInfo[it] = lorittaShards.queryGuildById(it)
            }
        }

        jobs.awaitAll()

        val array = userDonationKeys.map {
            jsonObject(
                    "id" to it.id.value,
                    "value" to it.value,
                    "expiresAt" to it.expiresAt,
                    "user" to WebsiteUtils.transformToJson(lorittaShards.retrieveUserById(it.userId)!!),
                    "activeIn" to loritta.newSuspendedTransaction { it.activeIn?.guildId?.let { serverInfo[it] } }
            )
        }
        return array.toJsonArray()
    }
}