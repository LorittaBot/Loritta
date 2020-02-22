package net.perfectdreams.loritta.plugin.funky.commands

import com.mrpowergamerbr.loritta.utils.LorittaPermission
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.plugin.funky.FunkyPlugin
import net.perfectdreams.loritta.plugin.funky.commands.LoopCommand.checkIfMusicIsPlaying
import net.perfectdreams.loritta.plugin.funky.commands.PlayCommand.checkMusicPremium
import net.perfectdreams.loritta.plugin.funky.commands.base.DSLCommandBase

object ResumeCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot, m: FunkyPlugin) = create(loritta, listOf("resume", "resumir")) {
		description { it["commands.audio.resume.description"] }

		userRequiredLorittaPermissions = listOf(LorittaPermission.DJ)

		executesDiscord {
			checkMusicPremium()

			val musicManager = checkIfMusicIsPlaying(m.funkyManager)
			musicManager.scheduler.player.isPaused = false

			reply(
					LorittaReply(
							"Música despausada"
					)
			)
		}
	}
}