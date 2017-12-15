package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent

class MusicInfoCommand : AbstractCommand("tocando", listOf("playing", "musicinfo")) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["MUSICINFO_DESCRIPTION"]
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MUSIC
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_PERMISSIONS)
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		context.guild.selfMember.voiceState.channel
		val manager = LorittaLauncher.getInstance().getGuildAudioPlayer(context.guild)
		if (manager.player.playingTrack == null) {
			context.sendMessage(context.getAsMention(true) + context.locale.MUSICINFO_NOMUSIC.msgFormat())
		} else {
			val embed = LorittaUtilsKotlin.createTrackInfoEmbed(context)
			val message = context.sendMessage(embed)
			context.metadata.put("currentTrack", manager.scheduler.currentTrack) // Salvar a track atual
			message.addReaction("\uD83E\uDD26").complete()
			message.addReaction("\uD83D\uDD22").complete();
		}
	}

	override fun onCommandReactionFeedback(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		LorittaUtilsKotlin.handleMusicReaction(context, e, msg)
	}
}