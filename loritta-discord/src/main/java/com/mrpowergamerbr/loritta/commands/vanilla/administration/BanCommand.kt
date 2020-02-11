package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.extensions.getTextChannelByNullableId
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments

class BanCommand : AbstractCommand("ban", listOf("banir", "hackban", "forceban"), CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["BAN_Description"]
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
		return listOf(Permission.BAN_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.BAN_MEMBERS)
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val user = AdminUtils.checkForUser(context) ?: return

			val member = context.guild.getMember(user)

			if (member != null) {
				if (!AdminUtils.checkForPermissions(context, member))
					return
			}

			val (reason, skipConfirmation, silent, delDays) = AdminUtils.getOptions(context) ?: return

			val banCallback: suspend (Message?, Boolean) -> (Unit) = { message, isSilent ->
				ban(context.legacyConfig, context.guild, context.userHandle, locale, user, reason, isSilent, delDays)

				message?.delete()?.queue()

				AdminUtils.sendSuccessfullyPunishedMessage(context)
			}

			if (skipConfirmation) {
				banCallback.invoke(null, silent)
				return
			}

			val hasSilent = context.legacyConfig.moderationConfig.sendPunishmentViaDm || context.legacyConfig.moderationConfig.sendToPunishLog
			val message = AdminUtils.sendConfirmationMessage(context, user, hasSilent, "ban")

			message.onReactionAddByAuthor(context) {
				if (it.reactionEmote.isEmote("✅") || it.reactionEmote.isEmote("\uD83D\uDE4A")) {
					banCallback.invoke(message, it.reactionEmote.isEmote("\uD83D\uDE4A"))
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

		fun ban(serverConfig: MongoServerConfig, guild: Guild, punisher: User, locale: LegacyBaseLocale, user: User, reason: String, isSilent: Boolean, delDays: Int) {
			if (!isSilent) {
				if (serverConfig.moderationConfig.sendPunishmentViaDm && guild.isMember(user)) {
					try {
						val embed =  AdminUtils.createPunishmentMessageSentViaDirectMessage(guild, locale, punisher, locale.toNewLocale()["$LOCALE_PREFIX.ban.punishAction"], reason)

						user.openPrivateChannel().queue {
							it.sendMessage(embed).queue()
						}
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}

				if (serverConfig.moderationConfig.sendToPunishLog) {
					val textChannel = guild.getTextChannelByNullableId(serverConfig.moderationConfig.punishmentLogChannelId)

					if (textChannel != null && textChannel.canTalk()) {
						val message = MessageUtils.generateMessage(
								serverConfig.moderationConfig.punishmentLogMessage,
								listOf(user),
								guild,
								mutableMapOf(
										"reason" to reason,
										"punishment" to locale.toNewLocale()["$LOCALE_PREFIX.ban.punishAction"],
										"staff" to punisher.name,
										"@staff" to punisher.asMention,
										"staff-discriminator" to punisher.discriminator,
										"staff-avatar-url" to punisher.effectiveAvatarUrl,
										"staff-id" to punisher.id,
										"duration" to locale.toNewLocale()["$LOCALE_PREFIX.mute.forever"]
								)
						)

						textChannel.sendMessage(message!!).queue()
					}
				}
			}

			guild.ban(user, delDays, AdminUtils.generateAuditLogMessage(locale.toNewLocale(), punisher, reason))
					.queue()
		}
	}
}
