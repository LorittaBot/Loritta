package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.twitter

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.extensions.valueOrNull
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import mu.KotlinLogging
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.Status
import org.jooby.mvc.GET
import org.jooby.mvc.Path
import twitter4j.TwitterFactory
import java.util.concurrent.TimeUnit

@Path("/api/v1/twitter/users/show")
class ShowTwitterUserController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	val cachedUsersById = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).maximumSize(10_000).build<Long, JsonObject>().asMap()
	val cachedUsersByScreenName = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).maximumSize(10_000).build<String, JsonObject>().asMap()

	@GET
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response) {
		res.type(MediaType.json)

		val tf = TwitterFactory(loritta.tweetTracker.buildTwitterConfig())
		val twitter = tf.instance

		val screenName = req.param("screenName").valueOrNull()

		if (screenName != null) {
			val cachedResponse = cachedUsersByScreenName[screenName]
			if (cachedResponse != null) {
				res.send(
						gson.toJson(
								cachedResponse
						)
				)
				return
			}
		}

		val accountId = req.param("userId").valueOrNull()

		val twitterUser = if (accountId != null) {
			val accountIdAsLong = accountId.toLong()
			val cachedResponse = cachedUsersById[accountIdAsLong]
			if (cachedResponse != null) {
				res.send(
						gson.toJson(
								cachedResponse
						)
				)
				return
			}

			twitter.users().showUser(accountId.toLong())
		} else if (screenName != null) {
			val cachedResponse = cachedUsersByScreenName[screenName]
			if (cachedResponse != null) {
				res.send(
						gson.toJson(
								cachedResponse
						)
				)
				return
			}

			twitter.users().showUser(screenName)
		} else {
			res.status(Status.NOT_FOUND)
			res.send("{}")
			return
		}

		if (twitterUser == null) {
			res.status(Status.NOT_FOUND)
			res.send("{}")
			return
		} else {
			val payload = jsonObject(
					"id" to twitterUser.id,
					"name" to twitterUser.name,
					"screenName" to twitterUser.screenName,
					"avatarUrl" to twitterUser.profileImageURLHttps
			)

			cachedUsersByScreenName[twitterUser.screenName] = payload
			cachedUsersById[twitterUser.id] = payload

			res.send(
					gson.toJson(
							payload
					)
			)
		}
	}
}