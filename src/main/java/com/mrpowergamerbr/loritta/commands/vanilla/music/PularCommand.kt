package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission

class PularCommand : AbstractCommand("pular", category = CommandCategory.MUSIC) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["PULAR_DESCRIPTION"]
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
		LorittaLauncher.loritta.skipTrack(context)
	}
}