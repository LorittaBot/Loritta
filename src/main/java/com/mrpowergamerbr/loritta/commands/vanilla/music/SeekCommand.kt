package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.Permission
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

class SeekCommand : CommandBase() {
	override fun getLabel(): String {
		return "seek"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.get("SEEK_DESCRIPTION")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MUSIC
	}

	override fun getExample(): List<String> {
		return listOf("2:30")
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.VOICE_MUTE_OTHERS)
	}

	override fun run(context: CommandContext) {
		val manager = LorittaLauncher.loritta.getGuildAudioPlayer(context.guild)

		if (context.args.isNotEmpty()) {
			var arg = context.args[0];

			var timeSplit = arg.split(":")

			if (timeSplit.isNotEmpty()) {
				var min = timeSplit[0].toIntOrNull()
				var sec = timeSplit[0].toIntOrNull()

				if (min != null && sec != null) {
					val time = (min * 60000L) + (sec * 1000L)

					if (time > manager.player.playingTrack.duration) {
						val fancy = String.format("%02d:%02d",
								TimeUnit.MILLISECONDS.toMinutes(manager.player.playingTrack.duration),
								TimeUnit.MILLISECONDS.toSeconds(manager.player.playingTrack.duration) -
										TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(manager.player.playingTrack.duration))
						);

						context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.get("SEEK_TOO_BIG", fancy))
					} else {
						val fancy = String.format("%02d:%02d",
								TimeUnit.MILLISECONDS.toMinutes(time),
								TimeUnit.MILLISECONDS.toSeconds(time) -
										TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
						);

						manager.player.playingTrack.position = time

						context.sendMessage(context.getAsMention(true) + context.locale.get("SEEK_CHANGED", fancy))
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
	}
}