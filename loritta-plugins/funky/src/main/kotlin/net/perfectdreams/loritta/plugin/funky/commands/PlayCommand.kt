package net.perfectdreams.loritta.plugin.funky.commands

import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.plugin.funky.FunkyPlugin
import net.perfectdreams.loritta.plugin.funky.audio.TrackRequest
import net.perfectdreams.loritta.plugin.funky.commands.base.DSLCommandBase

object PlayCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot, m: FunkyPlugin) = create(loritta, listOf("play", "tocar")) {
		description { it["commands.audio.play.description"] }

		executesDiscord {
			checkMusicPremium()

			val musicConfig = serverConfig.retrieveMusicConfig()

			if (musicConfig == null || !musicConfig.enabled) {
				reply(
						LorittaReply(
								"Sistema de música está desativado!"
						)
				)
				return@executesDiscord
			}

			val audioManager = m.funkyManager
			val channel = this.member?.voiceState?.channel ?: run {
				reply(
						LorittaReply(
								"Você não está em um canal de voz!"
						)
				)
				return@executesDiscord
			}

			if (channel.idLong !in musicConfig.channels) {
				reply(
						LorittaReply(
								"Você não está em um canal de música!"
						)
				)
				return@executesDiscord
			}

			val link = audioManager.connect(channel)
			val musicManager = audioManager.getOrCreateMusicManager(guild, link) ?: return@executesDiscord
			val audioTrack = audioManager.resolveTrack(args[0])

			musicManager.scheduler.isLooping = false // Remover de loop
			musicManager.scheduler.queue(
					TrackRequest(
							this.sender,
							audioTrack
					)
			)

			reply(
					LorittaReply(
							"Adicionado na fila `${audioTrack.info.title.escapeMentions().stripCodeMarks()}`!",
							"\uD83D\uDCBD"
					)
			)
		}
	}
}