package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class RandomNaoEntreAkiCommand : AbstractCommand("randomneaki", listOf("randomnaoentreaki", "randomnea"), CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String {
		return "Pega uma postagem aleatória do Não Entre Aki"
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val source = "página"

		val body = HttpRequest.get("http://www.naoentreaki.com.br/api/v1/posts/destaques/?order=semana&allowNsfw=false&limit=1&skip=${RANDOM.nextInt(0, 100000)}&random=true")
				.userAgent(Constants.USER_AGENT)
				.body()

		val json = JSON_PARSER.parse(body).obj

		if (json["data"].array.size() == 0) {
			context.reply(
					LoriReply(
							"Atualmente eu não tenho nenhum post do Não Entre Aki para te mostrar... tente mais tarde!",
							Constants.ERROR

					)
			)
			return
		}

		val post = json["data"].array[0]
		val image = post["images"].array[0]["url"].string

		context.reply(
				LoriReply(
						image,
						"<:neaki:438383984691642369>"
				)
		)
	}
}