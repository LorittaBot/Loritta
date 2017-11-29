package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta

class RandomMemeguy1997 : CommandBase("randomemeguy1997") {
	override fun getAliases(): List<String> {
		return listOf("randommemeguy", "randomeme", "randomemeguy", "randomemeguy1997")
	}

	override fun getDescription(): String {
		return "Pega uma postagem aleatória do Memeguy1997"
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val source = if (Loritta.random.nextBoolean()) "página" else "grupo";

		val post = if (source == "página") {
			loritta.memeguy1997PageCache.get(Loritta.random.nextInt(loritta.memeguy1997PageCache.size))
		} else {
			loritta.memeguy1997GroupCache.get(Loritta.random.nextInt(loritta.memeguy1997GroupCache.size))
		}

		if (post != null) {
			context.sendMessage("<:memeguy1997:383612878147682317> **|** " + context.getAsMention(true) + "(Fonte: *$source do Memeguy1997*) ${post.url} `${post.description}`")
		} else {
			context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + "Não consegui encontrar nenhum meme na página do Memeguy1997...")
		}
	}
}