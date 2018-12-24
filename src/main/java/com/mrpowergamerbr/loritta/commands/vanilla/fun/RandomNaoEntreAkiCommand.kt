package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale

class RandomNaoEntreAkiCommand : AbstractCommand("randomneaki", listOf("randomnaoentreaki", "randomnea", "rneaki", "rnea", "rnaoentreaki"), CommandCategory.FUN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return "Pega uma postagem aleatória do Não Entre Aki"
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (!context.isPrivateChannel && !context.message.textChannel.isNSFW) {
			context.reply(
					LoriReply(
							"A equipe do Não Entre Aki não sabe moderar o conteúdo do próprio website deles e, por isto agora o comando de memes aleatórios do Não Entre Aki só pode ser executado em canais NSFW, desculpe a inconveniência. \uD83D\uDE2D",
							Constants.ERROR
					)
			)
			return
		}

		val body = HttpRequest.get("http://www.naoentreaki.com.br/api/v1/posts/destaques/?order=semana&allowNsfw=false&limit=1&skip=${RANDOM.nextInt(0, 100000)}&random=true")
				.userAgent(Constants.USER_AGENT)
				.body()

		val json = jsonParser.parse(body).obj

		if (json["data"].array.size() == 0) {
			context.reply(
					LoriReply(
							"Atualmente eu não tenho nenhum post do Não Entre Aki para te mostrar... tente mais tarde!",
							Constants.ERROR

					)
			)
			return
		}

		val post = json["data"].array.firstOrNull { it.obj["images"].array.size() != 0 } ?: run {
			context.reply(
					LoriReply(
							"Atualmente eu não tenho nenhum post do Não Entre Aki para te mostrar... tente mais tarde!",
							Constants.ERROR

					)
			)
			return
		}
		val image = post["images"].array[0]["url"].string
		val title = post["title"].string
		val author = post["user"]["nick"].string

		context.reply(
				LoriReply(
						"`${title.stripCodeMarks()}` por ${author.escapeMentions().stripCodeMarks()} — $image",
						"<:neaki:438383984691642369>"
				)
		)
	}
}