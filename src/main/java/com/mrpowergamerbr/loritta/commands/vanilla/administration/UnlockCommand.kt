package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.*
import net.dv8tion.jda.core.Permission
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class UnlockCommand : AbstractCommand("unlock", listOf("destrancar"), CommandCategory.ADMIN){
	override fun getDescription(locale: BaseLocale): String {
		return locale.format { commands.moderation.unlock.description }
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
						.setAllow(Permission.MESSAGE_WRITE)
						.queue()
						context.reply(
							LoriReply(
								locale.format { commands.moderation.lock.allowed }
							)
						)
					} 
				}
		} else {
			context.explain()
		}
	}
}
