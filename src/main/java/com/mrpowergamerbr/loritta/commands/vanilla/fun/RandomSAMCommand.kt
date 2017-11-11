package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta

class RandomSAMCommand : CommandBase("randomsam") {
	override fun getAliases(): List<String> {
		return listOf("randomsouthamericamemes")
	}

	override fun getDescription(): String {
		return "Pega uma postagem aleatória do South America Memes"
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext) {
		val source = if (Loritta.random.nextBoolean()) "página" else "grupo";

		val post = if (source == "página") {
			loritta.southAmericaMemesPageCache.get(Loritta.random.nextInt(loritta.southAmericaMemesPageCache.size))
		} else {
			loritta.southAmericaMemesGroupCache.get(Loritta.random.nextInt(loritta.southAmericaMemesGroupCache.size))
		}

		if (post != null) {
			context.sendMessage("<:sam:331592756969603073> **|** " + context.getAsMention(true) + "Cópia não comédia! (Fonte: *$source do South America Memes*) ${post.url} `${post.description}`")
		} else {
			context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + "Não consegui encontrar nenhum meme na página do South America Memes...")
		}
	}
}