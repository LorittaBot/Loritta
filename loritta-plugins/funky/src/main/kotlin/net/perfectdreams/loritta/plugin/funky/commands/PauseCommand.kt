package net.perfectdreams.loritta.plugin.funky.commands

import com.mrpowergamerbr.loritta.utils.LorittaPermission
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.plugin.funky.FunkyPlugin
import net.perfectdreams.loritta.plugin.funky.commands.base.DSLCommandBase

object PauseCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot, m: FunkyPlugin) = create(loritta, listOf("pause", "pausar")) {
		description { it["commands.audio.pause.description"] }
		userRequiredLorittaPermissions = listOf(LorittaPermission.DJ)

		executesDiscord {
			val musicManager = m.funkyManager.getMusicManager(guild) ?: return@executesDiscord
			musicManager.scheduler.player.isPaused = true

			reply(
					LorittaReply(
							"Música pausada"
					)
			)
		}
	}
}