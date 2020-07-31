package net.perfectdreams.loritta.plugin.fortnite.routes

import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.queueAfterWithMessagePerSecondTargetAndClusterLoadBalancing
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import io.ktor.request.receiveText
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.plugin.fortnite.FortniteStuff
import net.perfectdreams.loritta.plugin.fortnite.tables.FakeTable
import net.perfectdreams.loritta.plugin.fortnite.tables.FortniteConfigs
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.TimeUnit

class PostShopUpdateRoute(val m: FortniteStuff, loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/fortnite/shop") {
	private val logger = KotlinLogging.logger {}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val payload = JsonParser.parseString(call.receiveText())

		val fileName = payload["fileName"].string
		val apiLocaleId = payload["localeId"].string
		val isNew = payload["isNew"].bool

		logger.info { "Received Fortnite Shop Payload, file name: $fileName; locale ID: $apiLocaleId; has new stuff? $isNew" }

		m.storeFileNamesByLocaleId[apiLocaleId] = fileName

		if (isNew || FortniteStuff.forceNewBroadcast) {
			// oh frick, it is new! let's broadcast!!
			val broadcastServers = transaction(Databases.loritta) {
				(ServerConfigs innerJoin FortniteConfigs).select {
					ServerConfigs.localeId eq apiLocaleId and (FakeTable.fortniteConfig.isNotNull()) and (FortniteConfigs.advertiseNewItems eq true)
				}.toList()
			}

			val canTalkGuildIds = mutableListOf<Long>()

			loop@ for (serverConfig in broadcastServers) {
				try {
					val guild = lorittaShards.getGuildById(serverConfig[ServerConfigs.id].value) ?: continue

					val channel = guild.getTextChannelById(serverConfig[FortniteConfigs.channelToAdvertiseNewItems]
							?: continue) ?: continue

					val localeId = serverConfig[ServerConfigs.localeId]

					val storeFileName = when {
						m.storeFileNamesByLocaleId.containsKey(localeId) -> m.storeFileNamesByLocaleId[localeId]
						m.storeFileNamesByLocaleId.containsKey(Constants.DEFAULT_LOCALE_ID) -> m.storeFileNamesByLocaleId[Constants.DEFAULT_LOCALE_ID]
						else -> continue@loop
					}

					if (!channel.canTalk())
						continue

					logger.info { "Broadcasting Fortnite shop update in $channel @ $guild!" }
					channel.sendMessage("${Emotes.DEFAULT_DANCE} ${loritta.instanceConfig.loritta.website.url}assets/img/fortnite/shop/$storeFileName")
							.queueAfterWithMessagePerSecondTargetAndClusterLoadBalancing(canTalkGuildIds.size)

					canTalkGuildIds.add(guild.idLong)
				} catch (e: Exception) {
					logger.info { "Something went wrong while trying to send the shop update in ${serverConfig[ServerConfigs.id].value}" }
				}
			}
		}

		call.respondJson(jsonObject())
	}
}