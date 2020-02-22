package net.perfectdreams.loritta.plugin.funky.commands

import com.mrpowergamerbr.loritta.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.funky.FunkyPlugin
import net.perfectdreams.loritta.plugin.funky.commands.base.DSLCommandBase

object PlaylistCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot, m: FunkyPlugin) = create(loritta, listOf("playlist", "fila")) {
		description { it["commands.audio.playlist.description"] }

		executesDiscord {
			checkMusicPremium()

			val musicManager = checkIfMusicIsPlaying(m.funkyManager)

			val embed = EmbedBuilder()
					.setTitle("\uD83C\uDFB6 Músicas na fila")
					.setColor(Constants.LORITTA_AQUA)
					.setDescription(
							(musicManager.scheduler.queue.toMutableList().apply { musicManager.scheduler.currentlyPlaying?.let { this.add(0, it) } }).joinToString("\n") {
								buildString {
									if (musicManager.scheduler.currentlyPlaying == it) {
										this.append("▶️")
										if (musicManager.scheduler.isLooping) {
											this.append("\uD83D\uDD01")
										}
										this.append(' ')
									}
									this.append(it.requestedBy.asMention)
									this.append(' ')
									this.append("[${it.track.info.title}](${it.track.info.uri})")
								}
							}
					)

			sendMessage(embed.build())
		}
	}
}