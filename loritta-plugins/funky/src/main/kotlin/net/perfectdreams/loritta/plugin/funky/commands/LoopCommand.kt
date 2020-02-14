package net.perfectdreams.loritta.plugin.funky.commands

import com.mrpowergamerbr.loritta.utils.LorittaPermission
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.plugin.funky.FunkyPlugin
import net.perfectdreams.loritta.plugin.funky.commands.base.DSLCommandBase

object LoopCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot, m: FunkyPlugin) = create(loritta, listOf("loop")) {
		description { it["commands.audio.loop.description"] }
		userRequiredLorittaPermissions = listOf(LorittaPermission.DJ)

		executesDiscord {
			val audioManager = m.funkyManager

			val musicManager = audioManager.getMusicManager(guild) ?: return@executesDiscord

			// Limpar lista de qualquer música que tenha
			musicManager.scheduler.queue.clear()
			musicManager.scheduler.isLooping = true

			reply(
					LorittaReply(
							"Música está em loop! Para desativar, use o comando novamente"
					)
			)
		}
	}
}