package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.extensions.getRandom
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import org.jsoup.Jsoup

class RandomSAMCommand : AbstractCommand("randomsam", listOf("randomsouthamericamemes", "rsam", "rsouthamericamemes"), CommandCategory.FUN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return "Pega uma postagem aleatória do South America Memes"
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		// TODO: Migrar para a API do Twitter após o Twitter remover a suspensão da minha conta @mrpowergamerbr
		val upperBound = (System.currentTimeMillis().toBigInteger().multiply(1039952060005920769.toBigInteger())).divide(1536938490000.toBigInteger())
		val lowerBound = upperBound - 1183800219463680.toBigInteger()

		val response = HttpRequest.get("https://twitter.com/i/profiles/show/SoutAmericMemes/timeline/tweets?include_available_features=1&include_entities=1&max_position=${RANDOM.nextLong(lowerBound.toLong(), upperBound.toLong())}&reset_error_state=false")
				.body()

		val payload = jsonParser.parse(response)

		val itemsHtml = payload["items_html"].string
		val document = Jsoup.parse(itemsHtml)
		val photoUrl = document.getElementsByClass("js-adaptive-photo").getRandom().attr("data-image-url")

		context.sendMessage("<:sam:383614103853203456> **|** " + context.getAsMention(true) + "Cópia não comédia! $photoUrl")
	}
}