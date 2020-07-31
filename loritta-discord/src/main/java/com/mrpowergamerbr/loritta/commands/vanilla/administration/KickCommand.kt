package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.extensions.retrieveMemberOrNull
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.PunishmentAction

class KickCommand : AbstractCommand("kick", listOf("expulsar", "kickar"), CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["KICK_Description"]
	}

	override fun getUsage(locale: LegacyBaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.USER) {
				optional = false
			}
			argument(ArgumentType.TEXT) {
				optional = true
			}
		}
	}

	override fun getExamples(): List<String> {
		return listOf("159985870458322944", "159985870458322944 Algum motivo bastante aleatório")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val (users, rawReason) = AdminUtils.checkAndRetrieveAllValidUsersFromMessages(context) ?: return

			val members = mutableListOf<Member>()
			for (user in users) {
				val member = context.guild.retrieveMemberOrNull(user)

				if (member == null) {
					context.reply(
							LoriReply(
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

			val settings = AdminUtils.retrieveModerationInfo(context.config)
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
				if (it.reactionEmote.isEmote("✅") || it.reactionEmote.isEmote("\uD83D\uDE4A")) {
					kickCallback.invoke(message, it.reactionEmote.isEmote("\uD83D\uDE4A"))
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
		private val LOCALE_PREFIX = "commands.moderation"

		fun kick(context: CommandContext, settings: AdminUtils.ModerationConfigSettings, locale: LegacyBaseLocale, member: Member, user: User, reason: String, isSilent: Boolean) {
			if (!isSilent) {
				if (settings.sendPunishmentViaDm && context.guild.isMember(user)) {
					try {
						val embed = AdminUtils.createPunishmentMessageSentViaDirectMessage(context.guild, locale, context.userHandle, locale["KICK_PunishAction"], reason)

						user.openPrivateChannel().queue {
							it.sendMessage(embed).queue()
						}
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}

				val punishLogMessage = AdminUtils.getPunishmentForMessage(
						settings,
						context.guild,
						PunishmentAction.KICK
				)

				if (settings.sendPunishmentToPunishLog && settings.punishLogChannelId != null && punishLogMessage != null) {
					val textChannel = context.guild.getTextChannelById(settings.punishLogChannelId)

					if (textChannel != null && textChannel.canTalk()) {
						val message = MessageUtils.generateMessage(
								punishLogMessage,
								listOf(user, context.guild),
								context.guild,
								mutableMapOf(
										"duration" to locale.toNewLocale()["commands.moderation.mute.forever"]
								) + AdminUtils.getStaffCustomTokens(context.userHandle)
										+ AdminUtils.getPunishmentCustomTokens(locale.toNewLocale(), reason, "${LOCALE_PREFIX}.kick")
						)

						message?.let {
							textChannel.sendMessage(it).queue()
						}
					}
				}
			}

			context.guild.kick(member, AdminUtils.generateAuditLogMessage(locale.toNewLocale(), context.userHandle, reason))
					.queue()
		}
	}
}
