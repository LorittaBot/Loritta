package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.edit
import com.mrpowergamerbr.loritta.utils.extensions.localized
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.transactions.transaction

class UserInfoCommand : AbstractCommand("userinfo", listOf("memberinfo"), CommandCategory.DISCORD) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.get("USERINFO_DESCRIPTION")
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
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

		showQuickGlanceInfo(null, context, user, member)
	}

	fun getEmbedBase(user: User, member: Member?): EmbedBuilder {
		return EmbedBuilder().apply {
			setThumbnail(user.effectiveAvatarUrl)
			var nickname = user.name

			if (member != null) {
				nickname = member.effectiveName
			}

			val ownerEmote = when {
				member?.isOwner == true -> "\uD83D\uDC51"
				else -> ""
			}

			val typeEmote = when {
				user.isBot -> Emotes.BOT_TAG
				else -> Emotes.WUMPUS_BASIC
			}

			val statusEmote = when (member?.onlineStatus) {
				OnlineStatus.ONLINE -> Emotes.ONLINE
				OnlineStatus.IDLE -> Emotes.IDLE
				OnlineStatus.DO_NOT_DISTURB -> Emotes.DO_NOT_DISTURB
				else -> Emotes.OFFLINE
			}

			setTitle("$ownerEmote$typeEmote$statusEmote $nickname", null)
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
		val locale = context.legacyLocale

		embed.apply {
			val lorittaProfile = loritta.getOrCreateLorittaProfile(user.id)
			val settings = transaction(Databases.loritta) { lorittaProfile.settings }

			addField("\uD83D\uDD16 ${context.legacyLocale.get("USERINFO_TAG_DO_DISCORD")}", "`${user.name}#${user.discriminator}`", true)
			addField("\uD83D\uDCBB ${context.legacyLocale.get("USERINFO_ID_DO_DISCORD")}", "`${user.id}`", true)

			val accountCreatedDiff = DateUtils.formatDateDiff(user.timeCreated.toInstant().toEpochMilli(), context.legacyLocale)
			addField("\uD83D\uDCC5 ${context.legacyLocale.toNewLocale()["commands.discord.userInfo.accountCreated"]}", accountCreatedDiff, true)

			if (member != null) {
				val accountJoinedDiff = DateUtils.formatDateDiff(member.timeJoined.toInstant().toEpochMilli(), context.legacyLocale)
				addField("\uD83C\uDF1F ${context.legacyLocale.toNewLocale()["commands.discord.userInfo.accountJoined"]}", accountJoinedDiff, true)

				addField(
						"\uD83D\uDC81 ${locale.toNewLocale()["commands.discord.userInfo.joinPosition"]}",
						locale.toNewLocale()["commands.discord.userInfo.joinPlace", "${member.guild.members.sortedBy { it.timeJoined }.indexOf(member) + 1}º"],
						true
				)
			}

			if (lorittaProfile.lastMessageSentAt != 0L) {
				val lastSeenDiff = DateUtils.formatDateDiff(lorittaProfile.lastMessageSentAt, context.legacyLocale)
				addField("\uD83D\uDC40 ${context.legacyLocale["USERINFO_LAST_SEEN"]}", lastSeenDiff, true)
			}
		}

		val _message = message?.edit(context.getAsMention(true), embed.build()) ?: context.sendMessage(context.getAsMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
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
				val permissions = member.getPermissions(context.message.textChannel).joinToString(", ", transform = { "`${it.localized(context.locale)}`" })
				addField("\uD83D\uDEE1 Permissões", permissions, true)

			 	val roles = member.roles.joinToString(separator = ", ", transform = { "`${it.name}`" })
				addField("\uD83D\uDCBC " + context.legacyLocale["USERINFO_ROLES"] + " (${member.roles.size})", if (roles.isNotEmpty()) roles.substringIfNeeded(0 until 1024) else context.legacyLocale.get("USERINFO_NO_ROLE") + " \uD83D\uDE2D", true)
			}
		}

		val _message = message?.edit(context.getAsMention(true), embed.build()) ?: context.sendMessage(context.getAsMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
		_message.onReactionAddByAuthor(context) {
			showQuickGlanceInfo(_message, context, user, member)
		}
		_message.addReaction("◀").queue()
		return _message
	}
}