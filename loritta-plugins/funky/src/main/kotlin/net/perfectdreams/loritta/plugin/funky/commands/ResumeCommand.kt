package net.perfectdreams.loritta.plugin.funky.commands

import com.mrpowergamerbr.loritta.utils.LorittaPermission
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.plugin.funky.FunkyPlugin
import net.perfectdreams.loritta.plugin.funky.commands.base.DSLCommandBase

object ResumeCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot, m: FunkyPlugin) = create(loritta, listOf("resume", "resumir")) {
		description { it["commands.audio.resume.description"] }

		userRequiredLorittaPermissions = listOf(LorittaPermission.DJ)

		executesDiscord {
			val musicManager = m.funkyManager.getMusicManager(guild) ?: return@executesDiscord
			musicManager.scheduler.player.isPaused = false

			reply(
					LorittaReply(
							"Música despausada"
					)
			)
		}
	}
}