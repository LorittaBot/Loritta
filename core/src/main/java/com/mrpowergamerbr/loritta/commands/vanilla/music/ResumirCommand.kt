package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta

class ResumirCommand : AbstractCommand("unpause", listOf("resumir", "despausar", "unpause", "continuar"), CommandCategory.MUSIC, lorittaPermissions = listOf(LorittaPermission.DJ)) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.get("UNPAUSE_DESCRIPTION")
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val manager = loritta.audioManager.getGuildAudioPlayer(context.guild)

		if (!manager.player.isPaused) {
			context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.legacyLocale.get("UNPAUSE_UNPAUSED", context.config.commandPrefix))
		} else {
			manager.player.isPaused = false
			context.sendMessage("▶ **|** " + context.getAsMention(true) + context.legacyLocale.get("UNPAUSE_CONTINUANDO", context.config.commandPrefix))
		}
	}
}