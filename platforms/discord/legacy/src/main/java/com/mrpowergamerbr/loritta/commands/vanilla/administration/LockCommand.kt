package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.Emotes

class LockCommand : AbstractCommand("lock", listOf("trancar", "fechar"), CommandCategory.MODERATION) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.lock.description")
	
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
		val channel = getTextChannel(context, context.args.getOrNull(0)) ?: context.event.textChannel!! // Já que o comando não será executado via DM, podemos assumir que textChannel nunca será nulo
		
		val publicRole = context.guild.publicRole
		val override = channel.getPermissionOverride(publicRole)
		
		if (override != null) {
			if (Permission.MESSAGE_WRITE !in override.denied) {
				override.manager
						.deny(Permission.MESSAGE_WRITE)
						.queue()

				context.reply(
					LorittaReply(
						locale["commands.command.lock.denied", context.config.commandPrefix],
						"\uD83C\uDF89"
					)
				)
			} else {
				context.reply(
					LorittaReply(
						locale["commands.command.lock.channelAlreadyIsLocked", context.config.commandPrefix],
						Emotes.LORI_CRYING
					)
				)
			}
		} else {
			channel.createPermissionOverride(publicRole)
					.setDeny(Permission.MESSAGE_WRITE)
					.queue()

			context.reply(
				LorittaReply(
					locale["commands.command.lock.denied", context.config.commandPrefix],
					"\uD83C\uDF89"
				)
			)
		}
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