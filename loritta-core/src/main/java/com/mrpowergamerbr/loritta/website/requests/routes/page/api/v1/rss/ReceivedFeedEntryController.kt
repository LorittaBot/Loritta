package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.rss

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import mu.KotlinLogging
import net.perfectdreams.loritta.tables.TrackedRssFeeds
import net.perfectdreams.loritta.tables.TrackedTwitterAccounts
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.Body
import org.jooby.mvc.POST
import org.jooby.mvc.Path

@Path("/api/v1/rss/received-entry")
class ReceivedFeedEntryController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@POST
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response, @Body body: String) {
		res.type(MediaType.json)

		val json = jsonParser.parse(body)

		val feedUrl = json["feedUrl"].string
		val entry = json["entry"].obj

		logger.info { "Received entry $entry from $feedUrl, relayed from the master cluster!" }
		val configsTrackingAccount = transaction(Databases.loritta) {
			TrackedRssFeeds.select {
				TrackedRssFeeds.feedUrl eq feedUrl
			}.toMutableList()
		}
		logger.info { "There are ${configsTrackingAccount.size} tracked configs tracking $feedUrl" }

		for (tracked in configsTrackingAccount) {
			val guildId = tracked[TrackedTwitterAccounts.guildId]
			val serverConfig = loritta.getOrCreateServerConfig(guildId)

			val hasCustomRssFeedsSupport = transaction(Databases.loritta) {
				(serverConfig.donationKey?.value ?: 0.0 >= LorittaPrices.CUSTOM_RSS_FEEDS)
			}

			if (!(feedUrl.startsWith("{") && feedUrl.endsWith("}")) && !hasCustomRssFeedsSupport) // O servidor não possui custom rss feeds!
				continue

			val guild = lorittaShards.getGuildById(tracked[TrackedTwitterAccounts.guildId]) ?: continue
			val textChannel = guild.getTextChannelById(tracked[TrackedTwitterAccounts.channelId]) ?: continue

			val message = MessageUtils.generateMessage(
					tracked[TrackedRssFeeds.message],
					listOf(),
					guild,
					mapOf(
							"title" to entry["title"].string,
							"link" to entry["link"].string
					)
			) ?: continue

			textChannel.sendMessage(message).queue()
		}

		res.send("{}")
	}
}