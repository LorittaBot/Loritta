package net.perfectdreams.loritta.legacy.commands.vanilla.magic

import net.perfectdreams.loritta.legacy.api.commands.CommandContext
import net.perfectdreams.loritta.legacy.api.messages.LorittaReply
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.website.routes.api.v1.user.PostDeleteDataRoute

object DeleteAccountDataExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "delete_account_data <id>"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (args.getOrNull(0) != "delete_account_data")
			return@task false
		val userId = args[1].toLong()

		PostDeleteDataRoute.deleteAccountData(
				loritta as LorittaDiscord,
				userId
		)

		reply(
				LorittaReply(
						"Dados da conta de $userId foram deletados!"
				)
		)
		return@task true
	}
}