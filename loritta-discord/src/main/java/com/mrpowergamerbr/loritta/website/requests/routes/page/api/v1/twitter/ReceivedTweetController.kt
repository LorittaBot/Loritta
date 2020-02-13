package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.twitter

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import mu.KotlinLogging
import net.perfectdreams.loritta.tables.TrackedTwitterAccounts
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.Body
import org.jooby.mvc.POST
import org.jooby.mvc.Path

@Path("/api/v1/twitter/received-tweet")
class ReceivedTweetController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@POST
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response, @Body body: String) {
		res.type(MediaType.json)

		val json = jsonParser.parse(body)
		val tweetId = json["tweetId"].long
		val userId = json["userId"].long
		val screenName = json["screenName"].string

		logger.info { "Received status $tweetId from $screenName (${tweetId}), relayed from the master cluster!" }
		val configsTrackingAccount = transaction(Databases.loritta) {
			TrackedTwitterAccounts.select {
				TrackedTwitterAccounts.twitterAccountId eq userId
			}.toMutableList()
		}
		logger.info { "There are ${configsTrackingAccount.size} tracked configs tracking ${screenName} (${userId})" }

		for (tracked in configsTrackingAccount) {
			val guild = lorittaShards.getGuildById(tracked[TrackedTwitterAccounts.guildId]) ?: continue
			val textChannel = guild.getTextChannelById(tracked[TrackedTwitterAccounts.channelId]) ?: continue

			val message = MessageUtils.generateMessage(
					tracked[TrackedTwitterAccounts.message],
					listOf(),
					guild,
					mapOf(
							"link" to "https://twitter.com/${screenName}/status/${tweetId}"
					)
			) ?: continue

			textChannel.sendMessage(message).queue()
		}
		res.send("{}")
	}
}