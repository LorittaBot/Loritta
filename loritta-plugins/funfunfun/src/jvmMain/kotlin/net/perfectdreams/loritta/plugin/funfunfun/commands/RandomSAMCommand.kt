package net.perfectdreams.loritta.plugin.funfunfun.commands

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.utils.jsonParser
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.plugin.funfunfun.commands.base.DSLCommandBase
import org.jsoup.Jsoup

object RandomSAMCommand : DSLCommandBase {
		private var lastRequest = 0L
		private var cachedMemes = listOf<JsonElement>()

	override fun command(loritta: LorittaBot) = create(loritta, listOf("randomsam", "randomsouthamericamemes", "rsam", "rsouthamericamemes")) {
		description { it["commands.fun.randomsam.description"] }

		executes {
			if (System.currentTimeMillis() >= (lastRequest + 60_000)) {
				val scripts = Jsoup.connect("https://www.instagram.com/southamericamemes/?hl=pt-br")
						.get()
						.getElementsByTag("script")

				val instagramData = scripts.first { it.html().startsWith("window._sharedData = ") }.html().substring(21).let { it.substring(0, it.length - 1) }

				val json = jsonParser.parse(instagramData).obj

				cachedMemes = json["entry_data"]["ProfilePage"].array[0]["graphql"]["user"]["edge_owner_to_timeline_media"]["edges"].array.toList()
						.filter { it["node"]["__typename"].string == "GraphImage" }
				lastRequest = System.currentTimeMillis()
			}

			val randomMeme = cachedMemes.random()
			reply(
					LorittaReply(
							"Cópia não comédia! ${randomMeme["node"]["display_url"].string}",
							"<:sam:383614103853203456>"
					)
			)
		}
	}
}