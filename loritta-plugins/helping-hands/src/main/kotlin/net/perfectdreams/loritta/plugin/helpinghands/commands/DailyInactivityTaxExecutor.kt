package net.perfectdreams.loritta.plugin.helpinghands.commands

import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.vanilla.magic.LoriToolsCommand

object DailyInactivityTaxExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "daily_inactivity execute"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (this.args.getOrNull(0) != "daily_inactivity")
			return@task false
		if (this.args.getOrNull(1) != "execute")
			return@task false

		DailyInactivityTaxUtils.runDailyInactivityTax()

		reply(
				LorittaReply(
						"Prontinho!"
				)
		)

		return@task true
	}
}