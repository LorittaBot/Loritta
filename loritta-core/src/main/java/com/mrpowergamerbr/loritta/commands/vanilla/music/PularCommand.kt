package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.CommandCategory

class PularCommand : AbstractCommand("skip", listOf("pular"), category = CommandCategory.MUSIC) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["PULAR_DESCRIPTION"]
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		val channel = context.guild.selfMember.voiceState?.channel

		if (channel != null) {
			// Só tem uma pessoa escutando no canal de música?
			val usersInChannel = channel.members.filter { !it.user.isBot }
			// Se tiver, vamos deixar ela pular a música, afinal, só tem ela!

			if (usersInChannel.size > 1 || (usersInChannel.size == 1 && usersInChannel.first().user.idLong != context.userHandle.idLong)) { // Mas se tiver mais de uma, vamos verificar se ela tem permissão de DJ!
				// E foi o usuário que colocou essa música?
				val musicManager = loritta.audioManager.getGuildAudioPlayer(context.guild)

				if (musicManager.scheduler.currentTrack?.user?.idLong != context.userHandle.idLong) {
					val missingPermissions = listOf(LorittaPermission.DJ).filterNot { context.lorittaUser.hasPermission(it) }

					if (missingPermissions.isNotEmpty()) {
						// oh no
						val required = missingPermissions.joinToString(", ", transform = { "`" + locale["LORIPERMISSION_${it.name}"] + "`" })
						var message = locale["LORIPERMISSION_MissingPermissions", required]

						if (context.handle.hasPermission(Permission.ADMINISTRATOR) || context.handle.hasPermission(Permission.MANAGE_SERVER)) {
							message += " ${locale["LORIPERMISSION_MissingPermCanConfigure", loritta.instanceConfig.loritta.website.url]}"
						}
						context.reply(
								message,
								Constants.ERROR
						)
						return
					}
				}
			}
		}

		loritta.audioManager.skipTrack(context)
	}
}