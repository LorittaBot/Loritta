package net.perfectdreams.loritta.morenitta.commands.vanilla.discord

import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.string
import net.perfectdreams.loritta.cinnamon.discord.utils.RawToFormated.toLocalized
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.edit
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import net.perfectdreams.loritta.morenitta.utils.substringIfNeeded
import net.perfectdreams.loritta.deviousfun.EmbedBuilder
import net.perfectdreams.loritta.deviousfun.entities.Member
import net.perfectdreams.loritta.deviousfun.entities.Message
import net.perfectdreams.loritta.deviousfun.DeviousEmbed
import net.perfectdreams.loritta.deviousfun.entities.User
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.platform.discord.utils.UserFlagBadgeEmotes.getBadges
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.deviousfun.queue
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.LorittaBot

class UserInfoCommand(loritta: LorittaBot) : AbstractCommand(loritta, "userinfo", listOf("memberinfo"), net.perfectdreams.loritta.common.commands.CommandCategory.DISCORD) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.userinfo.description")

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		var user = context.getUserAt(0)

		if (user == null) {
			if (context.args.getOrNull(0) != null) {
				context.reply(
                        LorittaReply(
                                locale["commands.command.userinfo.unknownUser", context.args[0].stripCodeMarks()],
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

		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "user info")

		showQuickGlanceInfo(null, context, user, member)
	}

	fun getEmbedBase(context: CommandContext, user: User, member: Member?): EmbedBuilder {
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

			var title = "$ownerEmote$typeEmote${getBadges(user).joinToString("")} $nickname"
			var addBadgesToField = false

			// If the title contains more than the maximum length, move them to a field
			// ID that has issues: 53905483156684800
			if (title.length > DeviousEmbed.TITLE_MAX_LENGTH) {
				title = "$ownerEmote$typeEmote $nickname"
				addBadgesToField = true
			}

			if (addBadgesToField)
				addField("\uD83D\uDCDB ${context.locale["commands.command.userinfo.badges"]}", getBadges(user).joinToString(""), true)

			setTitle(title)

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
		val embed = getEmbedBase(context, user, member)

		embed.apply {
			addField("\uD83D\uDD16 ${context.locale["commands.command.userinfo.discordTag"]}", "`${user.name}#${user.discriminator}`", true)
			addField("\uD83D\uDCBB ${context.locale["commands.command.userinfo.discordId"]}", "`${user.id}`", true)

			addField("\uD83D\uDCC5 ${context.locale["commands.command.userinfo.accountCreated"]}", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifference(user.timeCreated, context.locale), true)
			if (member != null) {
				addField("\uD83C\uDF1F ${context.locale["commands.command.userinfo.accountJoined"]}", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifference(member.timeJoined, context.locale), true)

				val timeBoosted = member.timeBoosted
				if (timeBoosted != null)
					addField("${Emotes.LORI_NITRO_BOOST} ${context.locale["commands.command.userinfo.boostingSince"]}", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifference(timeBoosted, context.locale), true)
			}

			if (context.message.channel.idLong == 358774895850815488L || context.message.channel.idLong == 547119872568459284L) {
				var sharedServersFieldTitle = context.locale["commands.command.userinfo.sharedServers"]
				var servers: String?

				val sharedServersResults = loritta.lorittaShards.queryMutualGuildsInAllLorittaClusters(user.id)
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
		val embed = getEmbedBase(context, user, member)

		embed.apply {
			if (member != null) {
				val roles = member.roles.joinToString(separator = ", ", transform = { "`${it.name}`" })
				addField("\uD83D\uDCBC " + context.locale["commands.command.userinfo.roles"] + " (${member.roles.size})", if (roles.isNotEmpty()) roles.substringIfNeeded(0 until 1024) else context.locale["commands.command.userinfo.noRoles"] + " \uD83D\uDE2D", true)

				// TODO - DeviousFun
				val permissions = member.getPermissions(context.message.textChannel).values.toLocalized()?.joinToString(", ", transform = { "`${context.i18nContext.get(it)}`" })
				addField("\uD83D\uDEE1 Permissões", permissions ?: "", false)
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