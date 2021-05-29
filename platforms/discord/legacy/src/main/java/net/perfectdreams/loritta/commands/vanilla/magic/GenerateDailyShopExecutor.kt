package net.perfectdreams.loritta.commands.vanilla.magic

import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.LorittaDailyShopUpdateTask

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