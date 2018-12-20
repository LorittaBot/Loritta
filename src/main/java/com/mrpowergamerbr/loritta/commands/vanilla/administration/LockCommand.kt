package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission

class LockCommand : AbstractCommand("lock", listOf("trancar", "fechar"), CommandCategory.ADMIN){
	override fun getDescription(locale: BaseLocale): String {
		return locale.format { commands.moderation.lock.description }
	}
	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_SERVER)
	}
	override fun canUseInPrivateChannel(): Boolean {
		return false
	}
	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS)
	}
	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		/* if (context.args.isNotEmpty()) {
			var args = context.rawArgs
			val channelId = context.rawArgs[0]

			// Pegando canal de texto, via menções, ID ou nada
			val textChannel = if (args.size >= 2) {
				if (channelId.startsWith("<#") && channelId.endsWith(">")) {
					try {
						val ch = context.guild.getTextChannelById(channelId.substring(2, channelId.length - 1))
						args = args.remove(0)
						ch
					} catch (e: Exception) {
						null
					}
				} else {
					try {
						val ch = context.guild.getTextChannelById(channelId)
						args = args.remove(0)
						ch
					} catch (e: Exception) {
						null
					}
				}
			} else { null } ?: context.event.textChannel!! // Text Channel sempre será not null

			val everyoneRole = context.guild.publicRole
			val permissionOverride = textChannel.getPermissionOverride(everyoneRole)

			if (permissionOverride == null) {
			} else {
				    	textChannel.createPermissionOverride(everyoneRole)
						.setDeny(Permission.MESSAGE_WRITE)
						.queue()

					context.reply(
						LoriReply(
								locale.format { commands.moderation.lock.denied }
						)
					)
				}
			}
		} else {
			context.explain()
		} */
	}
}
