package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import net.perfectdreams.loritta.api.commands.CommandCategory

class UnlockCommand : AbstractCommand("unlock", listOf("destrancar"), CommandCategory.ADMIN) {
	
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.moderation.unlock.description"]
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
	
	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val channel = getTextChannel(context, context.args.getOrNull(0)) ?: context.event.textChannel!! // Já que o comando não será executado via DM, podemos assumir que textChannel nunca será nulo
		
		val publicRole = context.guild.publicRole
		val override = channel.getPermissionOverride(publicRole)
		
		if (override != null) {
			if (Permission.MESSAGE_WRITE in override.denied) {
				override.manager
						.grant(Permission.MESSAGE_WRITE)
						.queue()
			}
		} else { // Bem, na verdade não seria totalmente necessário este else, mas vamos supor que o cara usou o "+unlock" com o chat destravado sem ter travado antes :rolling_eyes:
			channel.createPermissionOverride(publicRole)
					.setAllow(Permission.MESSAGE_WRITE)
					.queue()
		}
		
		context.reply(
				LoriReply(
						locale.toNewLocale()["commands.moderation.unlock.allowed", context.config.commandPrefix],
						"\uD83C\uDF89"
				)
		)
	}
	
	fun getTextChannel(context: CommandContext, input: String?): TextChannel? {
		if (input == null)
			return null
		
		val guild = context.guild
		
		val channels = guild.getTextChannelsByName(input, false)
		if (channels.isNotEmpty()) {
			return channels[0]
		}
		
		val id = input
				.replace("<", "")
				.replace("#", "")
				.replace(">", "")
		
		if (!id.isValidSnowflake())
			return null
		
		val channel = guild.getTextChannelById(id)
		if (channel != null) {
			return channel
		}
		
		return null
	}
}
