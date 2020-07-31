package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.MessageUtils
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
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.PunishmentAction

class UnbanCommand : AbstractCommand("unban", listOf("desbanir"), CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["UNBAN_Description"]
	}

	override fun getUsage(locale: LegacyBaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.USER) {
				optional = false
			}
		}
	}

	override fun getExamples(): List<String> {
		return listOf("159985870458322944", "159985870458322944 Pediu desculpas pelo o que aconteceu, e prometeu que o que aconteceu não irá se repetir.", "395935916952256523 Bani sem querer, desculpa!")
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
			val (users, rawReason) = AdminUtils.checkAndRetrieveAllValidUsersFromMessages(context) ?: return

			for (user in users) {
				val member = context.guild.getMember(user)

				if (member != null) {
					context.reply(
							LoriReply(
									locale.toNewLocale()["$LOCALE_PREFIX.unban.userIsInTheGuild"],
									Constants.ERROR
							)
					)
					return
				}
			}

			val (reason, skipConfirmation, silent, delDays) = AdminUtils.getOptions(context, rawReason) ?: return
			val settings = AdminUtils.retrieveModerationInfo(context.config)

			val banCallback: suspend (Message?, Boolean) -> (Unit) = { message, isSilent ->
				for (user in users)
					unban(settings, context.guild, context.userHandle, locale, user, reason, isSilent)

				message?.delete()?.queue()

				context.reply(
						LoriReply(
								locale.toNewLocale()["$LOCALE_PREFIX.unban.successfullyUnbanned"] + " ${Emotes.LORI_HMPF}",
								"\uD83C\uDF89"
						)
				)
			}

			if (skipConfirmation) {
				banCallback.invoke(null, false)
				return
			}

			val hasSilent = settings.sendPunishmentViaDm || settings.sendPunishmentToPunishLog
			val message = AdminUtils.sendConfirmationMessage(context, users, hasSilent, "unban")

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
		private const val LOCALE_PREFIX = "commands.moderation"

		fun unban(settings: AdminUtils.ModerationConfigSettings, guild: Guild, punisher: User, locale: LegacyBaseLocale, user: User, reason: String, isSilent: Boolean) {
			if (!isSilent) {
				val punishLogMessage = AdminUtils.getPunishmentForMessage(
						settings,
						guild,
						PunishmentAction.UNBAN
				)

				if (settings.sendPunishmentToPunishLog && settings.punishLogChannelId != null && punishLogMessage != null) {
					val textChannel = guild.getTextChannelById(settings.punishLogChannelId)

					if (textChannel != null && textChannel.canTalk()) {
						val message = MessageUtils.generateMessage(
								punishLogMessage,
								listOf(user, guild),
								guild,
								mutableMapOf(
										"duration" to locale.toNewLocale()["$LOCALE_PREFIX.mute.forever"]
								) + AdminUtils.getStaffCustomTokens(punisher)
										+ AdminUtils.getPunishmentCustomTokens(locale.toNewLocale(), reason, "${LOCALE_PREFIX}.unban")
						)

						message?.let {
							textChannel.sendMessage(it).queue()
						}
					}
				}
			}

			guild.unban(user).reason(AdminUtils.generateAuditLogMessage(locale.toNewLocale(), punisher, reason))
					.queue()
		}
	}
}