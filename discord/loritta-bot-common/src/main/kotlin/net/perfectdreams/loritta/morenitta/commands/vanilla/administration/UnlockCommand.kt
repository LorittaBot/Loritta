package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.isValidSnowflake
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot

class UnlockCommand(loritta: LorittaBot) : AbstractCommand(loritta, "unlock", listOf("destrancar"), net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.unlock.description")

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
		val channel = getTextChannel(context, context.args.getOrNull(0)) ?: context.event.textChannel!! // Já que o comando não será executado via DM, podemos assumir que textChannel nunca será nulo

		val publicRole = context.guild.publicRole
		val override = channel.getPermissionOverride(publicRole)

		if (override != null) {
			if (Permission.MESSAGE_SEND in override.denied) {
				override.manager
					.grant(Permission.MESSAGE_SEND)
					.queue()

				context.reply(
					LorittaReply(
						locale["commands.command.unlock.allowed", context.config.commandPrefix],
						"\uD83C\uDF89"
					)
				)
			} else {
				context.reply(
					LorittaReply(
						locale["commands.command.unlock.channelAlreadyIsUnlocked", context.config.commandPrefix],
						Emotes.LORI_CRYING
					)
				)
			}
		} else { // Bem, na verdade não seria totalmente necessário este else, mas vamos supor que o cara usou o "+unlock" com o chat destravado sem ter travado antes :rolling_eyes:
			channel.permissionContainer.upsertPermissionOverride(publicRole)
				.grant(Permission.MESSAGE_SEND)
				.queue()

			context.reply(
				LorittaReply(
					locale["commands.command.unlock.allowed", context.config.commandPrefix],
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