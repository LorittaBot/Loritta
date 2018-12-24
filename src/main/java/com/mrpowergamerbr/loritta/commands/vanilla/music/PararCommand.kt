package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta

class PararCommand : AbstractCommand("stop", listOf("parar"), CommandCategory.MUSIC, listOf(LorittaPermission.DJ)) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["PARAR_Description"]
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		loritta.audioManager.musicManagers.remove(context.guild.idLong) // Remover o music manager da guild atual
		val link = loritta.audioManager.lavalink.getLink(context.guild)
		link.disconnect()
		link.destroy()

		// e avisar que o batidão acabou!
		context.reply(
				locale["PARAR_Success"],
				"\u23F9"
		)
	}
}