package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.Permission
import java.util.*

class VolumeCommand : CommandBase() {
	override fun getLabel(): String {
		return "volume"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.get("VOLUME_DESCRIPTION")
	}

	override fun getExample(): List<String> {
		return Arrays.asList("100", "66", "33")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MUSIC
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.VOICE_MUTE_OTHERS)
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun run(context: CommandContext) {
		val manager = LorittaLauncher.getInstance().getGuildAudioPlayer(context.guild)
		if (context.args.size >= 1) {
			try {
				val vol = Integer.valueOf(context.args[0])
				if (vol > 100) {
					context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.VOLUME_TOOHIGH.msgFormat())
					return
				}
				if (0 > vol) {
					context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.VOLUME_TOOLOW.msgFormat())
					return
				}
				if (manager.player.volume > vol) {
					context.sendMessage(context.getAsMention(true) + context.locale.VOLUME_LOWER.msgFormat())
				} else {
					context.sendMessage(context.getAsMention(true) + context.locale.VOLUME_HIGHER.msgFormat())
				}
				manager.player.volume = Integer.valueOf(context.args[0])!!
			} catch (e: Exception) {
				context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.VOLUME_EXCEPTION.msgFormat())
			}
		} else {
			context.explain()
		}
	}
}