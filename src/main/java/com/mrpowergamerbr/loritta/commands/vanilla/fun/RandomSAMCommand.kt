package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.extensions.getRandom
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import org.jsoup.Jsoup

class RandomSAMCommand : AbstractCommand("randomsam", listOf("randomsouthamericamemes", "rsam", "rsouthamericamemes"), CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String {
		return "Pega uma postagem aleatória do South America Memes"
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		// TODO: Migrar para a API do Twitter após o Twitter remover a suspensão da minha conta @mrpowergamerbr
		val response = HttpRequest.get("https://twitter.com/i/profiles/show/SoutAmericMemes/timeline/tweets?include_available_features=1&include_entities=1&max_position=${RANDOM.nextLong(1020000000000000000, 1029183800219463680)}&reset_error_state=false")
				.body()

		val payload = jsonParser.parse(response)

		val itemsHtml = payload["items_html"].string
		val document = Jsoup.parse(itemsHtml)
		val photoUrl = document.getElementsByClass("js-adaptive-photo").getRandom().attr("data-image-url")

		context.sendMessage("<:sam:383614103853203456> **|** " + context.getAsMention(true) + "Cópia não comédia! $photoUrl")
	}
}