package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.CommandCategory
import java.util.concurrent.TimeUnit

class SeekCommand : AbstractCommand("seek", category = CommandCategory.MUSIC, lorittaPermissions = listOf(LorittaPermission.DJ)) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.get("SEEK_DESCRIPTION")
	}

	override fun getExamples(): List<String> {
		return listOf("2:30")
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val manager = loritta.audioManager.getGuildAudioPlayer(context.guild)

		if (manager.player.playingTrack != null) {
			if (context.args.isNotEmpty()) {
				val arg = context.args[0]

				val timeSplit = arg.split(":")

				if (timeSplit.size == 2) {
					val min = timeSplit[0].toIntOrNull()
					val sec = timeSplit[1].toIntOrNull()

					if (min != null && sec != null) {
						val time = (min * 60000L) + (sec * 1000L)

						if (time > manager.player.playingTrack.duration) {
							val fancy = String.format("%02d:%02d",
									TimeUnit.MILLISECONDS.toMinutes(manager.player.playingTrack.duration),
									TimeUnit.MILLISECONDS.toSeconds(manager.player.playingTrack.duration) -
											TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(manager.player.playingTrack.duration))
							)

							context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.legacyLocale.get("SEEK_TOO_BIG", fancy))
						} else {
							val fancy = String.format("%02d:%02d",
									TimeUnit.MILLISECONDS.toMinutes(time),
									TimeUnit.MILLISECONDS.toSeconds(time) -
											TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
							)

							manager.player.seekTo(time)

							context.sendMessage(context.getAsMention(true) + context.legacyLocale.get("SEEK_CHANGED", fancy))
						}
					} else {
						context.explain()
					}
				} else {
					context.explain()
				}
			} else {
				context.explain()
			}
		} else {
			context.reply(
					LoriReply(
							locale["MUSICINFO_NOMUSIC", context.config.commandPrefix],
							Constants.ERROR
					)
			)
		}
	}
}