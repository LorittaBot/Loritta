package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.extensions.getTextChannel
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.CommandCategory

class LockCommand : AbstractCommand("lock", listOf("trancar", "fechar"), CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.moderation.lock.description"]
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
	
	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		val channel = context.getTextChannel(context.args.getOrNull(0), executedIfNull = true) ?: context.event.textChannel!! // Já que o comando não será executado via DM, podemos assumir que textChannel nunca será nulo
		
		val publicRole = context.guild.publicRole
		val override = channel.getPermissionOverride(publicRole)
		
		if (override != null) {
			if (Permission.MESSAGE_WRITE !in override.denied) {
				override.manager
						.deny(Permission.MESSAGE_WRITE)
						.queue()
			}
		} else {
			channel.createPermissionOverride(publicRole)
					.setDeny(Permission.MESSAGE_WRITE)
					.queue()
		}
		
		context.reply(
                LorittaReply(
                        locale.toNewLocale()["commands.moderation.lock.denied", context.config.commandPrefix],
                        "\uD83C\uDF89"
                )
		)
	}
}
