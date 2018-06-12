package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta

class PularCommand : AbstractCommand("skip", listOf("pular"), category = CommandCategory.MUSIC, lorittaPermissions = listOf(LorittaPermission.DJ)) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["PULAR_DESCRIPTION"]
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		loritta.audioManager.skipTrack(context)
	}
}