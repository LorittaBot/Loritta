package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import java.util.*

class VolumeCommand : AbstractCommand("volume", category = CommandCategory.MUSIC) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("VOLUME_DESCRIPTION")
	}

	override fun getExample(): List<String> {
		return Arrays.asList("100", "66", "33")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.VOICE_MUTE_OTHERS)
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val manager = LorittaLauncher.getInstance().getGuildAudioPlayer(context.guild)
		if (context.args.size >= 1) {
			try {
				val vol = Integer.valueOf(context.args[0])
				if (vol > 100) {
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["VOLUME_TOOHIGH"])
					return
				}
				if (0 > vol) {
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["VOLUME_TOOLOW"])
					return
				}
				if (manager.player.volume > vol) {
					context.sendMessage(context.getAsMention(true) + locale["VOLUME_LOWER"])
				} else {
					context.sendMessage(context.getAsMention(true) + locale["VOLUME_HIGHER"])
				}
				manager.player.volume = Integer.valueOf(context.args[0])!!
			} catch (e: Exception) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["VOLUME_EXCEPTION"])
			}
		} else {
			context.explain()
		}
	}
}