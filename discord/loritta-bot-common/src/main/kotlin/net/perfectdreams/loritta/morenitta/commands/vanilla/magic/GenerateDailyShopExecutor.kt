package net.perfectdreams.loritta.morenitta.commands.vanilla.magic

import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.LorittaDailyShopUpdateTask

object GenerateDailyShopExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "generate daily_shop"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (args.getOrNull(0) != "generate")
			return@task false
		if (args.getOrNull(1) != "daily_shop")
			return@task false

		LorittaDailyShopUpdateTask.generate()

		reply(
				LorittaReply(
						"Loja atualizada!"
				)
		)
		return@task true
	}
}