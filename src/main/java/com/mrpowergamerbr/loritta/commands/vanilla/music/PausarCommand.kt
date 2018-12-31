package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta

class PausarCommand : AbstractCommand("pause", listOf("pausar"), CommandCategory.MUSIC, listOf(LorittaPermission.DJ)) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.get("PAUSAR_DESCRIPTION")
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val manager = loritta.audioManager.getGuildAudioPlayer(context.guild)

		if (manager.player.isPaused) {
			context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.legacyLocale.get("PAUSAR_ALREADY_PAUSED", context.config.commandPrefix))
		} else {
			manager.player.isPaused = true
			context.sendMessage("\u23F8 **|** " + context.getAsMention(true) + context.legacyLocale.get("PAUSAR_PAUSADO", context.config.commandPrefix))
		}
	}
}