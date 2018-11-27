package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.UsernameChange
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.UsernameChanges
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.extensions.localized
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId

class UserInfoCommand : AbstractCommand("userinfo", listOf("memberinfo"), CommandCategory.DISCORD) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("USERINFO_DESCRIPTION")
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		var user = context.getUserAt(0)

		if (user == null) {
			if (context.args.getOrNull(0) != null) {
				context.reply(
						LoriReply(
								locale["USERINFO_UnknownUser", context.args[0].stripCodeMarks()],
								Constants.ERROR
						)
				)
				return
			}
			user = context.userHandle
		}

		val member = if (context.guild.isMember(user)) {
			context.guild.getMember(user)
		} else {
			null
		}

		showQuickGlanceInfo(context, user, member)
	}

	fun getEmbedBase(user: User, member: Member?): EmbedBuilder {
		return EmbedBuilder().apply {
			setThumbnail(user.effectiveAvatarUrl)
			var nickname = user.name

			if (member != null) {
				nickname = member.effectiveName
			}

			val typeEmote = when {
				user.isBot -> Emotes.DISCORD_BOT_TAG
				else -> Emotes.DISCORD_WUMPUS_BASIC
			}

			val statusEmote = when (member?.onlineStatus) {
				OnlineStatus.ONLINE -> Emotes.DISCORD_ONLINE
				OnlineStatus.IDLE -> Emotes.DISCORD_IDLE
				OnlineStatus.DO_NOT_DISTURB -> Emotes.DISCORD_DO_NOT_DISTURB
				else -> Emotes.DISCORD_OFFLINE
			}

			setTitle("$typeEmote$statusEmote $nickname", null)
			setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)

			if (member != null) {
				val highestRole = member.roles.sortedByDescending { it.positionRaw }.firstOrNull()
				if (highestRole != null) {
					setColor(highestRole.color)
				}
			}
		}
	}

	suspend fun showQuickGlanceInfo(context: CommandContext, user: User, member: Member?) {
		val embed = getEmbedBase(user, member)

		embed.apply {
			val lorittaProfile = loritta.getOrCreateLorittaProfile(user.id)
			val settings = transaction(Databases.loritta) { lorittaProfile.settings }

			addField("\uD83D\uDD16 ${context.locale.get("USERINFO_TAG_DO_DISCORD")}", "`${user.name}#${user.discriminator}`", true)
			addField("\uD83D\uDCBB ${context.locale.get("USERINFO_ID_DO_DISCORD")}", "`${user.id}`", true)

			/* var genericInformation = """
				🔖 **${context.locale.get("USERINFO_TAG_DO_DISCORD")}:** `${user.name}#${user.discriminator}`
				💻 **${context.locale.get("USERINFO_ID_DO_DISCORD")}:** `${user.id}`

			""".trimIndent()

			if (member != null) {
				val statusEmote = when (member.onlineStatus) {
					OnlineStatus.ONLINE -> Emotes.DISCORD_ONLINE
					OnlineStatus.IDLE -> Emotes.DISCORD_IDLE
					OnlineStatus.DO_NOT_DISTURB -> Emotes.DISCORD_DO_NOT_DISTURB
					else -> Emotes.DISCORD_OFFLINE
				}
				val statusName = locale.format { when (member.onlineStatus) {
					OnlineStatus.ONLINE -> discord.status.online
					OnlineStatus.IDLE -> discord.status.idle
					OnlineStatus.DO_NOT_DISTURB -> discord.status.doNotDisturb
					else -> discord.status.offline
				} }

				genericInformation += "$statusEmote **${context.locale["USERINFO_STATUS"]}:** $statusName\n"
			} */

			val accountCreatedDiff = DateUtils.formatDateDiff(user.creationTime.toInstant().toEpochMilli(), context.locale)
			addField("\uD83D\uDCC5 ${context.locale.format { commands.discord.userInfo.accountCreated }}", accountCreatedDiff, true)
			if (member != null) {
				val accountJoinedDiff = DateUtils.formatDateDiff(member.joinDate.toInstant().toEpochMilli(), context.locale)
				addField("\uD83C\uDF1F ${context.locale.format { commands.discord.userInfo.accountJoined }}", accountJoinedDiff, true)
			}

			val offset = Instant.ofEpochMilli(lorittaProfile.lastMessageSentAt).atZone(ZoneId.systemDefault()).toOffsetDateTime()

			if (lorittaProfile.lastMessageSentAt != 0L) {
				val lastSeenDiff = DateUtils.formatDateDiff(lorittaProfile.lastMessageSentAt, context.locale)
				addField("\uD83D\uDC40 ${context.locale["USERINFO_LAST_SEEN"]}", lastSeenDiff, true)
				// genericInformation += "\uD83D\uDC40 **${context.locale["USERINFO_LAST_SEEN"]}:** ${offset.humanize(locale)}\n"
				// genericInformation += "⬆ Ou seja, *$lastSeenDiff* atrás!\n"
			}

			// embed.addField("ℹ️ Informações", genericInformation, false)

			var sharedServersFieldTitle = context.locale.format { commands.discord.userInfo.sharedServers }
			var servers: String?
			val sharedServers = lorittaShards.getMutualGuilds(user)
					.sortedByDescending { it.members.size }

			if (settings.hideSharedServers) {
				servers = "*${context.locale["USERINFO_PrivacyOn"]}*"
			} else {
				servers = sharedServers.joinToString(separator = ", ", transform = { "`${it.name}`" })
				sharedServersFieldTitle = "$sharedServersFieldTitle (${sharedServers.size})"
			}

			if (servers.length >= 1024) {
				servers = servers.substring(0..1020) + "..."
			}

			embed.addField("\uD83C\uDF0E $sharedServersFieldTitle", servers, true)
			// if (member != null) {
			// 	val roles = member.roles.joinToString(separator = ", ", transform = { it.name })

			// 	addField("\uD83D\uDCBC " + context.locale["USERINFO_ROLES"], if (roles.isNotEmpty()) roles.substringIfNeeded(0 until 1024) else context.locale.get("USERINFO_NO_ROLE") + " \uD83D\uDE2D", true)
			// }

			embed.setFooter(context.locale["USERINFO_PrivacyInfo"], null)
		}

		val message = context.sendMessage(context.getAsMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
		message.onReactionAddByAuthor(context) {
			message.delete().queue()
			showExtendedInfo(context, user, member)
		}
		message.addReaction("▶").queue()
	}

	suspend fun showExtendedInfo(context: CommandContext, user: User, member: Member?) {
		val embed = getEmbedBase(user, member)
		val locale = context.locale

		embed.apply {
			val lorittaProfile = loritta.getOrCreateLorittaProfile(user.id)
			val settings = transaction(Databases.loritta) { lorittaProfile.settings }

			if (settings.hidePreviousUsernames) {
				val alsoKnownAs = "**" + context.locale.get("USERINFO_ALSO_KNOWN_AS") + "**\n*${locale["USERINFO_PrivacyOn"]}*"
				setDescription(alsoKnownAs)
			} else {
				val usernameChanges = transaction(Databases.loritta) { UsernameChange.find { UsernameChanges.userId eq user.idLong }.sortedBy { it.changedAt }.toMutableList() }

				if (usernameChanges.isNotEmpty() && usernameChanges[0].discriminator == user.discriminator && usernameChanges[0].username == user.name) {
					usernameChanges.removeAt(0)
				}

				if (usernameChanges.isNotEmpty()) {
					val alsoKnownAs = "**" + context.locale.get("USERINFO_ALSO_KNOWN_AS") + "**\n" + usernameChanges.joinToString(separator = "\n", transform = {
						"${it.username}#${it.discriminator} (" + Instant.ofEpochMilli(it.changedAt).atZone(ZoneId.systemDefault()).toOffsetDateTime().humanize(locale) + ")"
					})
					// Verificar tamanho do "alsoKnownAs" e, se necessário, cortar
					val alsoKnownAsLines = alsoKnownAs.split("\n").reversed()

					val aux = mutableListOf<String>()

					var length = 0
					for (line in alsoKnownAsLines) {
						if (length + line.length >= 2000) {
							break
						}
						aux.add(line)
						length += line.length
					}
					setDescription(aux.reversed().joinToString(separator = "\n"))
				}
			}

			if (member != null) {
				val memberIndex = member.guild.members.sortedBy { it.joinDate }.indexOf(member)
				addField("\uD83D\uDC81 Posição de Entrada", "${memberIndex + 1}", true)

				val permissions = member.getPermissions(context.message.textChannel).joinToString(", ", transform = { "`${it.localized(locale)}`" })
				addField("\uD83D\uDEE1️ Permissões", permissions, true)

			 	val roles = member.roles.joinToString(separator = ", ", transform = { "`${it.name}`" })
				addField("\uD83D\uDCBC " + context.locale["USERINFO_ROLES"], if (roles.isNotEmpty()) roles.substringIfNeeded(0 until 1024) else context.locale.get("USERINFO_NO_ROLE") + " \uD83D\uDE2D", true)
			}
		}

		val message = context.sendMessage(context.getAsMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
		message.onReactionAddByAuthor(context) {
			message.delete().queue()
			showQuickGlanceInfo(context, user, member)
		}
		message.addReaction("◀").queue()
	}
}