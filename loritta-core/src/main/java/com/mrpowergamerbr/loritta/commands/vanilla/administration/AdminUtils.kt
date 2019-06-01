package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.remove
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import java.awt.Color
import java.time.Instant

object AdminUtils {
	fun createPunishmentEmbedBuilderSentViaDirectMessage(guild: Guild, locale: LegacyBaseLocale, punisher: User, punishmentAction: String, reason: String): EmbedBuilder {
		val embed = EmbedBuilder()

		embed.setTimestamp(Instant.now())
		embed.setColor(Color(221, 0, 0))

		embed.setThumbnail(guild.iconUrl)
		embed.setAuthor(punisher.name + "#" + punisher.discriminator, null, punisher.avatarUrl)
		embed.setTitle("\uD83D\uDEAB ${locale["BAN_YouAreBanned", punishmentAction.toLowerCase(), guild.name]}!")
		embed.addField("\uD83D\uDC6E ${locale["BAN_PunishedBy"]}", punisher.name + "#" + punisher.discriminator, false)
		embed.addField("\uD83D\uDCDD ${locale["BAN_PunishmentReason"]}", reason, false)

		return embed
	}

	fun createPunishmentMessageSentViaDirectMessage(guild: Guild, locale: LegacyBaseLocale, punisher: User, punishmentAction: String, reason: String): MessageEmbed {
		return createPunishmentEmbedBuilderSentViaDirectMessage(guild, locale, punisher, punishmentAction, reason).build()
	}

	suspend fun getOptions(context: CommandContext): AdministrationOptions? {
		var rawArgs = context.rawArgs
		rawArgs = rawArgs.remove(0) // remove o usuário

		var reason = rawArgs.joinToString(" ")

		val pipedReason = reason.split("|")

		var usingPipedArgs = false
		var skipConfirmation = context.config.getUserData(context.userHandle.idLong).quickPunishment
		var delDays = 7

		var silent = false

		if (pipedReason.size > 1) {
			val pipedArgs=  pipedReason.toMutableList()
			val _reason = pipedArgs[0]
			pipedArgs.removeAt(0)

			pipedArgs.forEach {
				val arg = it.trim()
				if (arg == "force" || arg == "f") {
					skipConfirmation = true
					usingPipedArgs = true
				}
				if (arg == "s" || arg == "silent") {
					skipConfirmation = true
					usingPipedArgs = true
					silent = true
				}
				if (arg.endsWith("days") || arg.endsWith("dias") || arg.endsWith("day") || arg.endsWith("dia")) {
					delDays = it.split(" ")[0].toIntOrNull() ?: 0

					if (delDays > 7) {
						context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.legacyLocale["SOFTBAN_FAIL_MORE_THAN_SEVEN_DAYS"])
						return null
					}
					if (0 > delDays) {
						context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.legacyLocale["SOFTBAN_FAIL_LESS_THAN_ZERO_DAYS"])
						return null
					}

					usingPipedArgs = true
				}
			}

			if (usingPipedArgs)
				reason = _reason
		}

		val attachment = context.message.attachments.firstOrNull { it.isImage }

		if (attachment != null)
			reason += " " + attachment.url

		return AdministrationOptions(reason, skipConfirmation, silent, delDays)
	}

	data class AdministrationOptions(val reason: String, val skipConfirmation: Boolean, val silent: Boolean, val delDays: Int)
}