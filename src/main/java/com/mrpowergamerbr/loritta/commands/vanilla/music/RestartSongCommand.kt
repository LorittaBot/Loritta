package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.audio.GuildMusicManager
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta

class RestartSongCommand : AbstractCommand("restartsong", listOf("reiniciarmusica", "restarttrack", "reiniciarmusic", "reiniciarmúsica"), CommandCategory.MUSIC, lorittaPermissions = listOf(LorittaPermission.DJ)) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["RESTARTSONG_Description"]
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val manager = loritta.audioManager.getGuildAudioPlayer(context.guild)

		if (manager.player.playingTrack != null) {
			skip(context, locale, manager)
		} else {
			context.reply(
					LoriReply(
							locale["MUSICINFO_NOMUSIC", context.config.commandPrefix],
							Constants.ERROR
					)
			)
		}
	}

	companion object {
		fun skip(context: CommandContext, locale: BaseLocale, manager: GuildMusicManager) {
			manager.player.playingTrack.position = 0L
			context.reply(
					LoriReply(
							locale["RESTARTSONG_SongRestarted"],
							"⏪"
					)
			)
		}
	}
}