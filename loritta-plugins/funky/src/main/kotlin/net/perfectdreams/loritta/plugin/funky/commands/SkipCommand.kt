package net.perfectdreams.loritta.plugin.funky.commands

import com.mrpowergamerbr.loritta.utils.LorittaPermission
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.plugin.funky.FunkyPlugin
import net.perfectdreams.loritta.plugin.funky.commands.base.DSLCommandBase

object SkipCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot, m: FunkyPlugin) = create(loritta, listOf("skip", "pular")) {
		description { it["commands.audio.skip.description"] }

		userRequiredLorittaPermissions = listOf(LorittaPermission.DJ)

		executesDiscord {
			val audioManager = m.funkyManager

			val musicManager = audioManager.getMusicManager(guild) ?: return@executesDiscord
			musicManager.scheduler.isLooping = false // Remover loop
			musicManager.scheduler.nextTrack()

			reply(
					LorittaReply(
							"Música pulada!",
							"\uD83E\uDD39"
					)
			)
		}
	}
}