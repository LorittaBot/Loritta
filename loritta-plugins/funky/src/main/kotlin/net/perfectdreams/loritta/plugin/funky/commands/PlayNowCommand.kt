package net.perfectdreams.loritta.plugin.funky.commands

import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.plugin.funky.FunkyPlugin
import net.perfectdreams.loritta.plugin.funky.commands.base.DSLCommandBase

object PlayNowCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot, m: FunkyPlugin) = create(loritta, listOf("playnow", "tocaragora")) {
		description { it["commands.audio.playnow.description"] }

		executesDiscord {
			val audioManager = m.funkyManager
			val channel = this.member?.voiceState?.channel ?: return@executesDiscord

			if (channel == null)
				return@executesDiscord

			val link = audioManager.connect(channel)
			val musicManager = audioManager.getOrCreateMusicManager(guild, link) ?: return@executesDiscord
			val audioTrack = audioManager.resolveTrack(args[0])
			musicManager.scheduler.isLooping = false // Remover de loop
			musicManager.scheduler.player.playTrack(audioTrack)

			reply(
					LorittaReply(
							"Adicionado na fila *forçadamente* `${audioTrack.info.title.escapeMentions().stripCodeMarks()}`!",
							"\uD83D\uDCBD"
					)
			)
		}
	}
}