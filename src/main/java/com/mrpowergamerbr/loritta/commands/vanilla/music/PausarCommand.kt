package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission

class PausarCommand : CommandBase("pause") {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("PAUSAR_DESCRIPTION")
	}

	override fun getAliases(): List<String> {
		return listOf("pausar", "parar")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MUSIC
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.VOICE_MUTE_OTHERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val manager = LorittaLauncher.loritta.getGuildAudioPlayer(context.guild)

		if (manager.player.isPaused) {
			context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.get("PAUSAR_ALREADY_PAUSED", context.config.commandPrefix))
		} else {
			manager.player.isPaused = true
			context.sendMessage("\u23F8 **|** " + context.getAsMention(true) + context.locale.get("PAUSAR_PAUSADO", context.config.commandPrefix))
		}
	}
}