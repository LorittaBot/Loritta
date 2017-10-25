package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlin.concurrent.thread

class SpinnerCommand : CommandBase() {
	var spinningSpinners: MutableMap<String, FidgetSpinner> = mutableMapOf<String, FidgetSpinner>()

	data class FidgetSpinner(var emoji: String, var threadId: Long, var forTime: Int, var spinnedAt: Long, var lastRerotation: Long)

	override fun getLabel(): String {
        return "spinner"
    }

    override fun getAliases(): List<String> {
        return listOf("fidget");
    }

    override fun getDescription(locale: BaseLocale): String {
        return locale["SPINNER_DESCRIPTION"]
    }

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN;
	}

    override fun run(context: CommandContext) {
		if (spinningSpinners.contains(context.userHandle.id)) {
			val spinner = spinningSpinners[context.userHandle.id]!!

			val diff = (System.currentTimeMillis() - spinner.lastRerotation) / 1000

			if (diff in spinner.forTime-10..spinner.forTime) {
				var time = Loritta.random.nextInt(10, 61);

				var lowerBound = Math.max(0, time - Loritta.random.nextInt(-5, 6))
				var upperBound = Math.max(0, time - Loritta.random.nextInt(-5, 6))

				if (lowerBound > upperBound) {
					val temp = upperBound;
					upperBound = lowerBound
					lowerBound = temp
				}

				context.sendMessage(context.getAsMention(true) + "${spinner.emoji} " + context.locale.get("SPINNER_RESPINNED") + "\n\uD83D\uDD2E *" + context.locale.get("SPINNER_MAGIC_BALL", lowerBound, upperBound) + "*")


				val waitThread = thread {
					Thread.sleep((time * 1000).toLong());

					if (spinningSpinners.contains(context.userHandle.id)) {
						val spinner = spinningSpinners[context.userHandle.id]!!

						if (spinner.threadId != Thread.currentThread().id) {
							return@thread
						}
						val diff = (System.currentTimeMillis() - spinner.spinnedAt) / 1000

						context.sendMessage(context.getAsMention(true) + "${spinner.emoji} ${context.locale.get("SPINNER_SPINNED", diff)}")

						spinningSpinners.remove(context.userHandle.id)
					}
				}

				spinner.lastRerotation = System.currentTimeMillis()
				spinner.threadId = waitThread.id
				spinner.forTime = time
				spinningSpinners.put(context.userHandle.id, spinner)
			} else {
				val diff = (System.currentTimeMillis() - spinner.spinnedAt) / 1000

				context.sendMessage(context.getAsMention(true) + "${spinner.emoji} ${context.locale.get("SPINNER_OUCH")} ${context.locale.get("SPINNER_SPINNED", diff)}")

				spinningSpinners.remove(context.userHandle.id)
			}
			return
		}
		var time = Loritta.random.nextInt(10, 61); // Tempo que o Fidget Spinner irá ficar rodando

		var random = listOf("<:spinner1:327243530244325376>", "<:spinner2:327245670052397066>", "<:spinner3:327246151591919627>", "<:spinner4:344292269764902912>", "<:spinner5:344292269160923147>", "<:spinner6:344292270125613056>", "<:spinner7:344292270268350464>", "<:spinner8:344292269836206082>") // Pegar um spinner aleatório
		var spinnerEmoji = random[Loritta.random.nextInt(random.size)]

		var lowerBound = Math.max(0, time - Loritta.random.nextInt(-5, 6))
		var upperBound = Math.max(0, time - Loritta.random.nextInt(-5, 6))

		if (lowerBound > upperBound) {
			val temp = upperBound;
			upperBound = lowerBound
			lowerBound = temp
		}
		var msg = context.sendMessage(context.getAsMention(true) + "$spinnerEmoji ${context.locale.get("SPINNER_SPINNING")}" + "\n\uD83D\uDD2E *" + context.locale.get("SPINNER_MAGIC_BALL", lowerBound, upperBound) + "*")

		val waitThread = thread {
			Thread.sleep((time * 1000).toLong());

			if (spinningSpinners.contains(context.userHandle.id)) {
				val spinner = spinningSpinners[context.userHandle.id]!!

				if (spinner.threadId != Thread.currentThread().id) {
					return@thread
				}
				msg.delete().complete()
				context.sendMessage(context.getAsMention(true) + "$spinnerEmoji ${context.locale.get("SPINNER_SPINNED", time)}")

				spinningSpinners.remove(context.userHandle.id)
			}
		}

		val fidgetSpinner = FidgetSpinner(spinnerEmoji, waitThread.id, time, System.currentTimeMillis(), System.currentTimeMillis())

		spinningSpinners.put(context.userHandle.id, fidgetSpinner)
    }
}