package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.*
import net.dv8tion.jda.core.Permission
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class LockCommand : AbstractCommand("lock", listOf("trancar", "fechar"), CommandCategory.ADMIN){
	override fun getDescription(locale: BaseLocale): String {
		return locale.format { commands.administration.lock.description }
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
	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val textChannel = context.event.textChannel
			if (textChannel != null) {
					val everyoneRole = context.guild.publicRole
					val permissionOverride = textChannel.getPermissionOverride(everyoneRole)
					if (permissionOverride == null) {
						textChannel.createPermissionOverride(everyoneRole)
						.setDeny(Permission.MESSAGE_WRITE)
						.queue()
						contex.reply(
							LoriReply(
								locale.format { commands.administration.lock.denied }
							)
						)
					} else {
						if (permissionOverride.denied.contains(Permission.MESSAGE_WRITE)) {
							permissionOverride.manager
							.allow(Permission.MESSAGE_WRITE)
							.queue()
							contex.reply(
								LoriReply(
									locale.format { commands.administration.lock.allowed }
								)
							)
						}
					}
				}
		} else {
			context.explain()
		}
	}
}
