package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta

class TocarAgoraCommand : AbstractCommand("playnow", listOf("tocaragora"), CommandCategory.MUSIC, lorittaPermissions = listOf(LorittaPermission.DJ)) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("TOCARAGORA_Description")
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
			loritta.audioManager.loadAndPlay(context, music, override = true)
		} else {
			context.explain()
		}
	}
}