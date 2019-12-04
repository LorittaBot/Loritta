package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.rss

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import mu.KotlinLogging
import net.perfectdreams.loritta.tables.DefaultRssFeeds
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.Body
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/api/v1/rss/default")
class DefaultRssFeedsController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@GET
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response, @Body body: String) {
		res.type(MediaType.json)

		val array = jsonArray()

		transaction(Databases.loritta) {
			DefaultRssFeeds.selectAll().forEach {
				array.add(
						jsonObject(
								"feedId" to it[DefaultRssFeeds.feedId],
								"feedUrl" to it[DefaultRssFeeds.feedUrl]
						)
				)
			}
		}

		res.send(array)
	}
}