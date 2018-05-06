package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent

class MusicInfoCommand : AbstractCommand("tocando", listOf("playing", "playingnow", "musicinfo", "np"), CommandCategory.MUSIC) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["MUSICINFO_DESCRIPTION"]
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_ADD_REACTION)
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val manager = LorittaLauncher.loritta.getGuildAudioPlayer(context.guild)
		if (manager.player.playingTrack == null) {
			context.reply(
					LoriReply(
							locale["MUSICINFO_NOMUSIC", context.config.commandPrefix],
							Constants.ERROR
					)
			)
		} else {
			val embed = LorittaUtilsKotlin.createTrackInfoEmbed(context)
			val message = context.sendMessage(embed)
			context.metadata.put("currentTrack", manager.scheduler.currentTrack!!) // Salvar a track atual

			message.onReactionAddByAuthor(context) {
				if (context.lorittaUser.hasPermission(LorittaPermission.DJ)) {
					if (it.reactionEmote.name == "⏪") {
						RestartSongCommand.skip(context, locale, manager)
					}
					if (it.reactionEmote.name == "⏯") {
						if (manager.player.isPaused) {
							manager.player.isPaused = false
							context.sendMessage("▶ **|** " + context.getAsMention(true) + context.locale.get("UNPAUSE_CONTINUANDO", context.config.commandPrefix))
						} else {
							manager.player.isPaused = true
							context.sendMessage("\u23F8 **|** " + context.getAsMention(true) + context.locale.get("PAUSAR_PAUSADO", context.config.commandPrefix))
						}
					}
					if (it.reactionEmote.name == "⏩") {
						loritta.skipTrack(context)
					}
				}
			}

			if (context.lorittaUser.hasPermission(LorittaPermission.DJ)) {
				message.addReaction("⏪").complete()
				message.addReaction("⏯").complete()
				message.addReaction("⏩").complete()
			}

			if (context.lorittaUser.hasPermission(LorittaPermission.DJ)) {
				message.addReaction("⏯").complete()
			}

			if (context.lorittaUser.hasPermission(LorittaPermission.DJ)) {
				message.addReaction("⏩").complete()
			}

			if (context.config.musicConfig.voteToSkip)
				message.addReaction("\uD83E\uDD26").complete()

			message.addReaction("\uD83D\uDD22").complete()
		}
	}

	override fun onCommandReactionFeedback(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		LorittaUtilsKotlin.handleMusicReaction(context, e, msg)
	}
}