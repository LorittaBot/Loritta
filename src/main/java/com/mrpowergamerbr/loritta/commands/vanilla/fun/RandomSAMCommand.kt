package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta

class RandomSAMCommand : AbstractCommand("randomsam", listOf("randomsouthamericamemes", "rsam", "rsouthamericamemes"), CommandCategory.FUN) {
	override fun getDescription(locale: BaseLocale): String {
		return "Pega uma postagem aleatória do South America Memes"
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val source = "página"

		if (loritta.southAmericaMemesPageCache.isEmpty()) {
			context.reply(
					LoriReply(
							"Atualmente eu não tenho nenhum post da página da South America Memes para te mostrar... tente mais tarde!",
							Constants.ERROR

					)
			)
			return
		}
		val post = loritta.southAmericaMemesPageCache[Loritta.RANDOM.nextInt(loritta.southAmericaMemesPageCache.size)]

		if (post != null) {
			context.sendMessage("<:sam:383614103853203456> **|** " + context.getAsMention(true) + "Cópia não comédia! (Fonte: *$source do South America Memes*) ${post.url} `${post.description}`")
		} else {
			context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + "Não consegui encontrar nenhum meme na página do South America Memes...")
		}
	}
}