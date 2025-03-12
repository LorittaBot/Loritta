package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.addReaction
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.extensions.isEmote
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNull
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks

class KickCommand(loritta: LorittaBot) : AbstractCommand(loritta, "kick", listOf("expulsar", "kickar"), net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.kick.description")
	override fun getExamplesKey() = AdminUtils.PUNISHMENT_EXAMPLES_KEY
	override fun getUsage() = AdminUtils.PUNISHMENT_USAGES

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val (users, rawReason) = AdminUtils.checkAndRetrieveAllValidUsersFromMessages(context) ?: return

			val members = mutableListOf<Member>()
			for (user in users) {
				val member = context.guild.retrieveMemberOrNull(user)

				if (member == null) {
					context.reply(
						LorittaReply(
							context.locale["commands.userNotOnTheGuild", "${user.asMention} (`${user.name.stripCodeMarks()}#${user.discriminator} (${user.idLong})`)"],
							Emotes.LORI_HM
						)
					)
					return
				}

				if (!AdminUtils.checkForPermissions(context, member))
					return

				members.add(member)
			}

			val settings = AdminUtils.retrieveModerationInfo(loritta, context.config)
			val (reason, skipConfirmation, silent, delDays) = AdminUtils.getOptions(context, rawReason) ?: return

			val kickCallback: suspend (Message?, Boolean) -> (Unit) = { message, isSilent ->
				for (member in members)
					kick(context, settings, locale, member, member.user, reason, isSilent)

				message?.delete()?.queue()

				AdminUtils.sendSuccessfullyPunishedMessage(context, reason, true)
			}

			if (skipConfirmation) {
				kickCallback.invoke(null, silent)
				return
			}

			val hasSilent = settings.sendPunishmentViaDm || settings.sendPunishmentToPunishLog
			val message = AdminUtils.sendConfirmationMessage(context, users, hasSilent, "kick")

			message.onReactionAddByAuthor(context) {
				if (it.emoji.isEmote("✅") || it.emoji.isEmote("\uD83D\uDE4A")) {
					kickCallback.invoke(message, it.emoji.isEmote("\uD83D\uDE4A"))
				}
				return@onReactionAddByAuthor
			}

			message.addReaction("✅").queue()
			if (hasSilent) {
				message.addReaction("\uD83D\uDE4A").queue()
			}
		} else {
			this.explain(context)
		}
	}

	companion object {
		private val LOCALE_PREFIX = "commands.command"

		fun kick(context: CommandContext, settings: AdminUtils.ModerationConfigSettings, locale: BaseLocale, member: Member, user: User, reason: String, isSilent: Boolean) {
			if (!isSilent) {
				if (settings.sendPunishmentViaDm && context.guild.isMember(user)) {
					try {
						val embed = AdminUtils.createPunishmentMessageSentViaDirectMessage(context.guild, locale, context.userHandle, locale["commands.command.kick.punishAction"], reason)

						GlobalScope.launch {
							context.loritta.getOrRetrievePrivateChannelForUser(user).sendMessageEmbeds(embed).queue()
						}
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}

				val punishLogMessage = runBlocking {
					AdminUtils.getPunishmentForMessage(
						context.loritta,
						settings,
						context.guild,
						PunishmentAction.KICK
					)
				}

				if (settings.sendPunishmentToPunishLog && settings.punishLogChannelId != null && punishLogMessage != null) {
					val textChannel = context.guild.getGuildMessageChannelById(settings.punishLogChannelId)

					if (textChannel != null && textChannel.canTalk()) {
						val message = MessageUtils.generateMessageOrFallbackIfInvalid(
							context.i18nContext,
							punishLogMessage,
							listOf(user, context.guild),
							context.guild,
							mutableMapOf(
								"duration" to locale["commands.command.mute.forever"]
							) + AdminUtils.getStaffCustomTokens(context.userHandle)
									+ AdminUtils.getPunishmentCustomTokens(locale, reason, "${LOCALE_PREFIX}.kick"),
							generationErrorMessageI18nKey = I18nKeysData.InvalidMessages.MemberModerationKick
						)

						message?.let {
							textChannel.sendMessage(it).queue()
						}
					}
				}
			}

			context.guild.kick(member, AdminUtils.generateAuditLogMessage(locale, context.userHandle, reason))
				.queue()
		}
	}
}