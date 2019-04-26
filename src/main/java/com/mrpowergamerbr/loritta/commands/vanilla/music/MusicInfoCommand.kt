package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.CommandCategory

class MusicInfoCommand : AbstractCommand("playing", listOf("tocando", "playingnow", "musicinfo", "np"), CommandCategory.MUSIC) {
	override fun getDescription(locale: LegacyBaseLocale): String {
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

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val manager = loritta.audioManager.getGuildAudioPlayer(context.guild)
		if (manager.player.playingTrack == null || manager.scheduler.currentTrack == null) {
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
					if (it.reactionEmote.isEmote("⏪")) {
						RestartSongCommand.skip(context, locale, manager)
					}
					if (it.reactionEmote.isEmote("⏯")) {
						if (manager.player.isPaused) {
							manager.player.isPaused = false
							context.sendMessage("▶ **|** " + context.getAsMention(true) + context.legacyLocale.get("UNPAUSE_CONTINUANDO", context.config.commandPrefix))
						} else {
							manager.player.isPaused = true
							context.sendMessage("\u23F8 **|** " + context.getAsMention(true) + context.legacyLocale.get("PAUSAR_PAUSADO", context.config.commandPrefix))
						}
					}
					if (it.reactionEmote.isEmote("⏩")) {
						loritta.audioManager.skipTrack(context)
					}
					return@onReactionAddByAuthor
				}

				LorittaUtilsKotlin.handleMusicReaction(context, it, message)
			}

			if (context.lorittaUser.hasPermission(LorittaPermission.DJ)) {
				message.addReaction("⏪").queue()
				message.addReaction("⏯").queue()
				message.addReaction("⏩").queue()
			}

			if (context.lorittaUser.hasPermission(LorittaPermission.DJ)) {
				message.addReaction("⏯").queue()
			}

			if (context.lorittaUser.hasPermission(LorittaPermission.DJ)) {
				message.addReaction("⏩").queue()
			}

			if (context.config.musicConfig.voteToSkip)
				message.addReaction("\uD83E\uDD26").queue()

			message.addReaction("\uD83D\uDD22").queue()
		}
	}
}