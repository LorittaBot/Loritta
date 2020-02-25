package net.perfectdreams.loritta.plugin.fortnite.commands.fortnite

import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.vanilla.magic.LoriToolsCommand
import net.perfectdreams.loritta.plugin.fortnite.FortniteStuff

object ForceShopUpdateExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "fortnite shop force <true/false>"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (this.args.getOrNull(0) != "fortnite")
			return@task false
		if (this.args.getOrNull(1) != "shop")
			return@task false
		if (this.args.getOrNull(2) != "force")
			return@task false

		val forceNew = this.args.getOrNull(3)?.toBoolean() ?: return@task false

		FortniteStuff.forceNewBroadcast = forceNew

		reply(
				LorittaReply(
						"Estado de force shop broadcast alterado para $forceNew ^-^"
				)
		)

		return@task true
	}
}