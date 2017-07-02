package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.core.Permission

class TocarCommand : CommandBase() {
	override fun getLabel(): String {
		return "tocar"
	}

	override fun getDescription(): String {
		return "Toca uma música, experimental."
	}

	override fun getExample(): List<String> {
		return listOf("https://youtu.be/wn4Ju5-vMQ4")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun run(context: CommandContext) {
		if (!context.config.musicConfig.isEnabled) {
			context.sendMessage(LorittaUtils.ERROR + " | " + context.getAsMention(true) + " O meu sistema de músicas está desativado nesta guild... Pelo visto não teremos a `DJ Loritta` por aqui... \uD83D\uDE1E")
			return
		}
		if (context.guild.selfMember.voiceState.inVoiceChannel()) { // Se eu estou em um canal de voz...
			if (context.guild.selfMember.voiceState.isGuildMuted) { // E eu estou mutada?!? Como pode!
				context.sendMessage(LorittaUtils.ERROR + " | " + context.getAsMention(true) + "Alguém me mutou no canal de voz... \uD83D\uDE1E Por favor, peça para alguém da administração para desmutar!")
				return
			}
			if (!context.guild.selfMember.hasPermission(context.guild.selfMember.voiceState.channel, Permission.VOICE_SPEAK)) {
				context.sendMessage(LorittaUtils.ERROR + " | " + context.getAsMention(true) + "Eu não tenho permissão para falar no canal de voz... \uD83D\uDE1E Por favor, peça para alguém da administração dar permissão para eu poder soltar alguns batidões!")
				return
			}
		}
		if (context.args.size >= 1) {
			val music = context.args.joinToString(" ")

			if (music.equals("pular", ignoreCase = true) && context.handle.hasPermission(Permission.MANAGE_SERVER)) {
				LorittaLauncher.getInstance().skipTrack(context.event.textChannel)
				return
			}

			if (music.equals("reset", ignoreCase = true) && context.handle.hasPermission(Permission.MANAGE_SERVER)) {
				LorittaLauncher.getInstance().musicManagers.remove(context.guild.idLong)
				return
			}

			if (music.equals("limpar", ignoreCase = true) && context.handle.hasPermission(Permission.MANAGE_SERVER)) {
				LorittaLauncher.getInstance().getGuildAudioPlayer(context.guild).scheduler.queue.clear()
				return
			}
			LorittaLauncher.getInstance().loadAndPlay(context, context.config, context.event.textChannel, music)
		} else {
			context.explain()
		}
	}
}