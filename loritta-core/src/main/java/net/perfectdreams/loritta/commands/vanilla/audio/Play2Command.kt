package net.perfectdreams.loritta.commands.vanilla.audio

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.audio.TrackRequest
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.platform.discord.commands.discordCommand

object Play2Command {
	fun create(loritta: LorittaDiscord) = discordCommand(
			loritta,
			"PlayCommand",
			listOf("play2", "tocar2")
	) {
		description { it["commands.audio.play.description"] }

		requiresMusic = true

		executes {
			val context = checkType<DiscordCommandContext>(this)

			val audioManager = LorittaLauncher.loritta.audioManager
			val channel = context.member!!.voiceState?.channel

			if (audioManager == null || channel == null)
				return@executes

			val link = audioManager.connect(channel)
			val musicManager = audioManager.getOrCreateMusicManager(context.guild, link) ?: return@executes
			val audioTrack = musicManager.audioManager.resolveTrack(context.args[0])
			musicManager.scheduler.isLooping = false // Remover de loop
			musicManager.scheduler.queue(
					TrackRequest(
							context.user,
							audioTrack
					)
			)

			context.reply(
					LorittaReply(
							"Adicionado na fila `${audioTrack.info.title.escapeMentions().stripCodeMarks()}`!",
							"\uD83D\uDCBD"
					)
			)
		}
	}
}