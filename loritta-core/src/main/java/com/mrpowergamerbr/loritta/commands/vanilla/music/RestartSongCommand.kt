package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.audio.GuildMusicManager
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.CommandCategory

class RestartSongCommand : AbstractCommand("restartsong", listOf("reiniciarmusica", "restarttrack", "reiniciarmusic", "reiniciarmúsica"), CommandCategory.MUSIC, lorittaPermissions = listOf(LorittaPermission.DJ)) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["RESTARTSONG_Description"]
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
		suspend fun skip(context: CommandContext, locale: LegacyBaseLocale, manager: GuildMusicManager) {
			manager.player.seekTo(0L)
			context.reply(
					LoriReply(
							locale["RESTARTSONG_SongRestarted"],
							"⏪"
					)
			)
		}
	}
}