package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.edit
import com.mrpowergamerbr.loritta.utils.extensions.localized
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.utils.UserFlagBadgeEmotes.getBadges
import net.perfectdreams.loritta.utils.Emotes

class UserInfoCommand : AbstractCommand("userinfo", listOf("memberinfo"), CommandCategory.DISCORD) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["commands.discord.userinfo.description"]
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		var user = context.getUserAt(0)

		if (user == null) {
			if (context.args.getOrNull(0) != null) {
				context.reply(
                        LorittaReply(
                                locale["commands.discord.userinfo.unknownUser", context.args[0].stripCodeMarks()],
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

		showQuickGlanceInfo(null, context, user, member)
	}

	fun getEmbedBase(user: User, member: Member?): EmbedBuilder {
		return EmbedBuilder().apply {
			setThumbnail(user.effectiveAvatarUrl)
			var nickname = user.name

			if (member != null)
				nickname = member.effectiveName

			val ownerEmote = when {
				member?.isOwner == true -> "\uD83D\uDC51"
				else -> ""
			}

			val typeEmote = when {
				user.isBot -> Emotes.BOT_TAG
				else -> Emotes.WUMPUS_BASIC
			}

			setTitle("$ownerEmote$typeEmote${getBadges(user).joinToString("")} $nickname", null)
			setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)

			if (member != null) {
				val highestRole = member.roles.sortedByDescending { it.positionRaw }.firstOrNull()
				if (highestRole != null) {
					setColor(highestRole.color)
				}
			}
		}
	}

	suspend fun showQuickGlanceInfo(message: Message?, context: CommandContext, user: User, member: Member?): Message {
		val embed = getEmbedBase(user, member)

		embed.apply {
			addField("\uD83D\uDD16 ${context.locale["commands.discord.userinfo.discordTag"]}", "`${user.name}#${user.discriminator}`", true)
			addField("\uD83D\uDCBB ${context.locale["commands.discord.userinfo.discordId"]}", "`${user.id}`", true)

			val accountCreatedDiff = DateUtils.formatDateDiff(user.timeCreated.toInstant().toEpochMilli(), context.locale)
			addField("\uD83D\uDCC5 ${context.locale["commands.discord.userinfo.accountCreated"]}", accountCreatedDiff, true)
			if (member != null) {
				val accountJoinedDiff = DateUtils.formatDateDiff(member.timeJoined.toInstant().toEpochMilli(), context.locale)
				addField("\uD83C\uDF1F ${context.locale["commands.discord.userinfo.accountJoined"]}", accountJoinedDiff, true)

				if (member.timeBoosted != null) {
					val timeBoosted = DateUtils.formatDateDiff(member.timeBoosted!!.toInstant().toEpochMilli(), context.locale)
					addField("${Emotes.LORI_NITRO_BOOST} ${context.locale["commands.discord.userinfo.boostingSince"]}", timeBoosted, true)
				}
			}

			if (context.message.channel.idLong == 358774895850815488L || context.message.channel.idLong == 547119872568459284L) {
				var sharedServersFieldTitle = context.locale["commands.discord.userinfo.sharedServers"]
				var servers: String?

				val sharedServersResults = lorittaShards.queryMutualGuildsInAllLorittaClusters(user.id)
				val sharedServers = sharedServersResults.sortedByDescending {
					it["memberCount"].int
				}

				servers = sharedServers.joinToString(separator = ", ", transform = { "`${it["name"].string}`" })
				sharedServersFieldTitle = "$sharedServersFieldTitle (${sharedServers.size})"

				if (servers.length >= 1024) {
					servers = servers.substring(0..1020) + "..."
				}

				embed.addField("\uD83C\uDF0E $sharedServersFieldTitle", servers, false)
			}
		}

		val _message = message?.edit(context.getAsMention(true), embed.build())
				?: context.sendMessage(context.getAsMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
		if (member != null) {
			_message.onReactionAddByAuthor(context) {
				showExtendedInfo(_message, context, user, member)
			}
			_message.addReaction("▶").queue()
		}
		return _message
	}

	suspend fun showExtendedInfo(message: Message?, context: CommandContext, user: User, member: Member?): Message {
		val embed = getEmbedBase(user, member)

		embed.apply {
			if (member != null) {
				val roles = member.roles.joinToString(separator = ", ", transform = { "`${it.name}`" })
				addField("\uD83D\uDCBC " + context.locale["commands.discord.userinfo.roles"] + " (${member.roles.size})", if (roles.isNotEmpty()) roles.substringIfNeeded(0 until 1024) else context.locale["commands.discord.userinfo.noRoles"] + " \uD83D\uDE2D", true)

				val permissions = member.getPermissions(context.message.textChannel).joinToString(", ", transform = { "`${it.localized(context.locale)}`" })
				addField("\uD83D\uDEE1 Permissões", permissions, false)
			}
		}

		val _message = message?.edit(context.getAsMention(true), embed.build())
				?: context.sendMessage(context.getAsMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
		_message.onReactionAddByAuthor(context) {
			showQuickGlanceInfo(_message, context, user, member)
		}
		_message.addReaction("◀").queue()
		return _message
	}

}
