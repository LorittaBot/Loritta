package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.core.Permission

class TocarCommand : AbstractCommand("play", listOf("tocar", "adicionar"), CommandCategory.MUSIC) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("TOCAR_DESCRIPTION")
	}

	override fun getExample(): List<String> {
		return listOf("https://youtu.be/wn4Ju5-vMQ4",
				"https://soundcloud.com/itsreach/sonicmashup",
				"https://soundcloud.com/shokkbutt/ruining-songs-forever",
				"https://www.youtube.com/watch?v=BaUwnmncsrc",
				"Perfect Strangers")
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.guild.selfMember.voiceState.inVoiceChannel()) { // Se eu estou em um canal de voz...
			val selfMember = context.guild.selfMember;
			if (selfMember.voiceState.isGuildMuted) { // E eu estou mutada?!? Como pode!
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["TOCAR_MUTED"])
				return
			}
			if (selfMember.voiceState.isSuppressed) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["TOCAR_CANTTALK"])
				return
			}
		}
		if (context.args.isNotEmpty()) {
			val music = context.args.joinToString(" ")

			if (music.equals("reset", ignoreCase = true) && context.handle.hasPermission(Permission.MANAGE_SERVER)) {
				loritta.audioManager.musicManagers.remove(context.guild.idLong)
				return
			}

			if (music.equals("limpar", ignoreCase = true) && context.handle.hasPermission(Permission.MANAGE_SERVER)) {
				loritta.audioManager.getGuildAudioPlayer(context.guild).scheduler.queue.clear()
				return
			}
			loritta.audioManager.loadAndPlay(context, music)
		} else {
			context.explain()
		}
	}
}