package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent

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
		val manager = LorittaLauncher.loritta.getGuildAudioPlayer(context.guild)

		if (manager.player.playingTrack != null) {
			manager.player.playingTrack.position = 0L
			context.reply(
					LoriReply(
							locale["RESTARTSONG_SongRestarted"],
							"⏪"
					)
			)
		} else {
			context.reply(
					LoriReply(
							locale["MUSICINFO_NOMUSIC", context.config.commandPrefix],
							Constants.ERROR
					)
			)
		}
	}

	override fun onCommandReactionFeedback(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		LorittaUtilsKotlin.handleMusicReaction(context, e, msg)
	}
}