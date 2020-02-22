package net.perfectdreams.loritta.plugin.funky.commands

import com.mrpowergamerbr.loritta.utils.LorittaPermission
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.plugin.funky.FunkyPlugin
import net.perfectdreams.loritta.plugin.funky.commands.LoopCommand.checkIfMusicIsPlaying
import net.perfectdreams.loritta.plugin.funky.commands.base.DSLCommandBase

object StopCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot, m: FunkyPlugin) = create(loritta, listOf("stop", "parar")) {
		description { it["commands.audio.stop.description"] }

		userRequiredLorittaPermissions = listOf(LorittaPermission.DJ)

		executesDiscord {
			checkMusicPremium()

			val musicManager = checkIfMusicIsPlaying(m.funkyManager)
			musicManager.scheduler.destroy()

			reply(
					LorittaReply(
							"Música parada"
					)
			)
		}
	}
}