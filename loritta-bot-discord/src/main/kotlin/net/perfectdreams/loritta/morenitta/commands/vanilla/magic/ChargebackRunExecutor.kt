package net.perfectdreams.loritta.morenitta.commands.vanilla.magic

import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.PaymentUtils

object ChargebackRunExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "chargeback_run <id> <quantity> <remove_sonhos> <notify_self> <notify_user>"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (args.getOrNull(0) != "chargeback_run")
			return@task false

		val userId = args[1].toLong()
		val quantity = args[2].toLong()
		val removeSonhos = args[3].toBoolean()
		val notifySelf = args[4].toBoolean()
		val notifyUser = args[5].toBoolean()

		val triggeredSonhos = PaymentUtils.removeSonhosDueToChargeback(
				loritta,
				userId,
				quantity,
				removeSonhos,
				notifySelf,
				notifyUser
		)

		var str = ""
		for (entry in triggeredSonhos) {
			val totalQuantity = entry.value.sumOf { it.quantity }

			str += "${entry.key} ($totalQuantity):\n"
			for (removeSonhos in entry.value) {
				str += " ${(removeSonhos.usersThatTriggeredTheCheck + entry.key).joinToString("->")} (${removeSonhos.additionalContext}): ${removeSonhos.quantity}"
				str += "\n"
			}

			str += "\n"
		}

		sendFile(str.toByteArray(Charsets.UTF_8), "transactions.txt", "Finished!")
		return@task true
	}
}