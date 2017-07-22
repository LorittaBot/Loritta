package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.Permission

class TocarCommand : CommandBase() {
	override fun getLabel(): String {
		return "tocar"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.TOCAR_DESCRIPTION
	}

	override fun getExample(): List<String> {
		return listOf("https://youtu.be/wn4Ju5-vMQ4",
				"https://soundcloud.com/itsreach/sonicmashup",
				"https://soundcloud.com/shokkbutt/ruining-songs-forever",
				"https://www.youtube.com/watch?v=BaUwnmncsrc",
				"Perfect Strangers")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MUSIC
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun run(context: CommandContext) {
		if (context.guild.selfMember.voiceState.inVoiceChannel()) { // Se eu estou em um canal de voz...
			val selfMember = context.guild.selfMember;
			if (selfMember.voiceState.isGuildMuted) { // E eu estou mutada?!? Como pode!
				context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.TOCAR_MUTED.msgFormat())
				return
			}
			if (selfMember.voiceState.isSuppressed) {
				context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.TOCAR_CANTTALK.msgFormat())
				return
			}
		}
		if (!context.handle.voiceState.inVoiceChannel() || context.handle.voiceState.channel.id != context.config.musicConfig.musicGuildId) {
			// Se o cara não estiver no canal de voz ou se não estiver no canal de voz correto...
			context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.TOCAR_NOTINCHANNEL)
			return
		}
		if (context.args.size >= 1) {
			val music = context.args.joinToString(" ")

			if (music.equals("reset", ignoreCase = true) && context.handle.hasPermission(Permission.MANAGE_SERVER)) {
				LorittaLauncher.getInstance().musicManagers.remove(context.guild.idLong)
				return
			}

			if (music.equals("limpar", ignoreCase = true) && context.handle.hasPermission(Permission.MANAGE_SERVER)) {
				LorittaLauncher.getInstance().getGuildAudioPlayer(context.guild).scheduler.queue.clear()
				return
			}
			LorittaLauncher.getInstance().loadAndPlay(context, music)
		} else {
			context.explain()
		}
	}
}