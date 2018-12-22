package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import java.awt.Color
import java.time.Instant

object AdminUtils {
	fun createPunishmentMessageSentViaDirectMessage(guild: Guild, locale: LegacyBaseLocale, punisher: User, punishmentAction: String, reason: String): MessageEmbed {
		val embed = EmbedBuilder()

		embed.setTimestamp(Instant.now())
		embed.setColor(Color(221, 0, 0))

		embed.setThumbnail(guild.iconUrl)
		embed.setAuthor(punisher.name + "#" + punisher.discriminator, null, punisher.avatarUrl)
		embed.setTitle("\uD83D\uDEAB ${locale["BAN_YouAreBanned", punishmentAction.toLowerCase(), guild.name]}!")
		embed.addField("\uD83D\uDC6E ${locale["BAN_PunishedBy"]}", punisher.name + "#" + punisher.discriminator, false)
		embed.addField("\uD83D\uDCDD ${locale["BAN_PunishmentReason"]}", reason, false)

		return embed.build()
	}

	fun sendSuspectInfo(channel: TextChannel, user: User, profile: Profile?) {
		val locale = loritta.getLegacyLocaleById("default")
		val builder = EmbedBuilder()
				.setTitle("<:blobcatgooglypolice:525643372317114383> Usuário suspeito")
				.setAuthor(user.name + "#" + user.discriminator)
				.setThumbnail(user.effectiveAvatarUrl)
				.setFooter("ID do usuário: ${user.id}", null)

		if (profile != null) {
			val lastSeenDiff = DateUtils.formatDateDiff(profile.lastMessageSentAt, locale)
			builder.addField("\uD83D\uDC40 ${locale["USERINFO_LAST_SEEN"]}", lastSeenDiff, true)
		} else {
			builder.addField("\uD83D\uDC40 ${locale["USERINFO_LAST_SEEN"]}", "**Nunca criei um perfil para esse meliante**", true)
		}

		var sharedServersFieldTitle = locale.format { commands.discord.userInfo.sharedServers }
		var servers: String?
		val sharedServers = lorittaShards.getMutualGuilds(user)
				.sortedByDescending { it.members.size }

		servers = sharedServers.joinToString(separator = ", ", transform = { "`${it.name}` *(${DateUtils.formatDateDiff(it.getMember(user).joinDate.toInstant().toEpochMilli(), locale)})*" })
		sharedServersFieldTitle = "$sharedServersFieldTitle (${sharedServers.size})"

		if (servers.length >= 1024) {
			servers = servers.substring(0..1020) + "..."
		}

		builder.addField("\uD83C\uDF0E $sharedServersFieldTitle", servers, true)

		channel.sendMessage(builder.build()).queue()
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
						context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["SOFTBAN_FAIL_MORE_THAN_SEVEN_DAYS"])
						return null
					}
					if (0 > delDays) {
						context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["SOFTBAN_FAIL_LESS_THAN_ZERO_DAYS"])
						return null
					}

					usingPipedArgs = true
				}
			}

			if (usingPipedArgs)
				reason = _reason
		}

		return AdministrationOptions(reason, skipConfirmation, silent, delDays)
	}

	data class AdministrationOptions(val reason: String, val skipConfirmation: Boolean, val silent: Boolean, val delDays: Int)
}