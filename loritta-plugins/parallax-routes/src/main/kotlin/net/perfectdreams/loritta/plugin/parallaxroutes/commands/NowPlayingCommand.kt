package net.perfectdreams.loritta.plugin.funky.commands

import com.mrpowergamerbr.loritta.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.funky.FunkyPlugin
import net.perfectdreams.loritta.plugin.funky.commands.base.DSLCommandBase

object NowPlayingCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot, m: FunkyPlugin) = create(loritta, listOf("nowplaying", "tocando", "np", "song", "música", "musica")) {
		description { it["commands.audio.nowplaying.description"] }

		executesDiscord {
			checkMusicPremium()

			val musicManager = checkIfMusicIsPlaying(m.funkyManager)

			val nowPlaying = musicManager.scheduler.currentlyPlaying ?: return@executesDiscord

			val position = musicManager.link.player.trackPosition
			val duration = nowPlaying.track.duration

			val divided = ((position / duration.toDouble()) * 19).toInt()
			val characters = "═".repeat(19).map { it.toString() }.toMutableList()
			characters.add(divided, "\uD83D\uDD35")

			val embed = EmbedBuilder()
					.setAuthor(nowPlaying.track.info.author)
					.setTitle("\uD83C\uDFB5 ${nowPlaying.track.info.title}", nowPlaying.track.info.uri)
					.setThumbnail("https://i.ytimg.com/vi/${nowPlaying.track.identifier}/hqdefault.jpg")
					.setColor(Constants.LORITTA_AQUA)
					.setFooter(nowPlaying.requestedBy.name, nowPlaying.requestedBy.avatarUrl)
					.setDescription("[${characters.joinToString("")}]")

			sendMessage(embed.build())
		}
	}
}