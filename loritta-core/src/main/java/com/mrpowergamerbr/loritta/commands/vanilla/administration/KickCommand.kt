package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.extensions.getTextChannelByNullableId
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
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
			val user = AdminUtils.checkForUser(context) ?: return

			val member = context.guild.getMember(user)

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

			val (reason, skipConfirmation, silent, delDays) = AdminUtils.getOptions(context) ?: return

			val kickCallback: suspend (Message?, Boolean) -> (Unit) = { message, isSilent ->
				kick(context, locale, member, user, reason, isSilent)

				message?.delete()?.queue()

				AdminUtils.sendSuccessfullyPunishedMessage(context)
			}

			if (skipConfirmation) {
				kickCallback.invoke(null, silent)
				return
			}

			val hasSilent = context.config.moderationConfig.sendPunishmentViaDm || context.config.moderationConfig.sendToPunishLog
			val message = AdminUtils.sendConfirmationMessage(context, user, hasSilent, "kick")

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

		fun kick(context: CommandContext, locale: LegacyBaseLocale, member: Member, user: User, reason: String, isSilent: Boolean) {
			if (!isSilent) {
				if (context.config.moderationConfig.sendPunishmentViaDm && context.guild.isMember(user)) {
					try {
						val embed = AdminUtils.createPunishmentMessageSentViaDirectMessage(context.guild, locale, context.userHandle, locale["KICK_PunishAction"], reason)

						user.openPrivateChannel().queue {
							it.sendMessage(embed).queue()
						}
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}

				if (context.config.moderationConfig.sendToPunishLog) {
					val textChannel = context.guild.getTextChannelByNullableId(context.config.moderationConfig.punishmentLogChannelId)

					if (textChannel != null && textChannel.canTalk()) {
						val message = MessageUtils.generateMessage(
								context.config.moderationConfig.punishmentLogMessage,
								listOf(user),
								context.guild,
								mutableMapOf(
										"reason" to reason,
										"punishment" to locale.toNewLocale()["$LOCALE_PREFIX.kick.punishAction"],
										"staff" to context.userHandle.name,
										"@staff" to context.userHandle.asMention,
										"staff-discriminator" to context.userHandle.discriminator,
										"staff-avatar-url" to context.userHandle.effectiveAvatarUrl,
										"staff-id" to context.userHandle.id,
										"duration" to locale.toNewLocale()["commands.moderation.mute.forever"]
								)
						)

						textChannel.sendMessage(message!!).queue()
					}
				}
			}

			context.guild.kick(member, AdminUtils.generateAuditLogMessage(locale.toNewLocale(), context.userHandle, reason))
					.queue()
		}
	}
}
