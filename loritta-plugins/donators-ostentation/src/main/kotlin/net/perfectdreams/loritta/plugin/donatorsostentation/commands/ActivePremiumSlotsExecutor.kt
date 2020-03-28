package net.perfectdreams.loritta.plugin.donatorsostentation.commands

import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.vanilla.magic.LoriToolsCommand
import net.perfectdreams.loritta.plugin.donatorsostentation.PremiumSlotsUtils

object ActivePremiumSlotsExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "premium_slots active <true/false>"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (this.args.getOrNull(0) != "premium_slots")
			return@task false
		if (this.args.getOrNull(1) != "active")
			return@task false

		val active = this.args.getOrNull(2)!!.toBoolean()

		if (active)
			PremiumSlotsUtils.announcePremiumSlots()
		else
			PremiumSlotsUtils.closePremiumSlots()

		reply(
				LorittaReply(
						"Estado de Premium Slots foi marcado como $active"
				)
		)
		return@task true
	}
}