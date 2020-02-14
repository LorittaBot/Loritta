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
			val musicManager = m.funkyManager.getMusicManager(guild) ?: return@executesDiscord

			val embed = EmbedBuilder()
					.setTitle("Músicas na fila")
					.setColor(Constants.LORITTA_AQUA)
					.setDescription(
							musicManager.scheduler.queue.toList().joinToString("\n") {
								buildString {
									this.append(it.requestedBy.asMention)
									this.append(' ')
									this.append(it.track.info.title)
								}
							}
					)

			musicManager.scheduler.queue.toList()

			sendMessage(embed.build())
		}
	}
}