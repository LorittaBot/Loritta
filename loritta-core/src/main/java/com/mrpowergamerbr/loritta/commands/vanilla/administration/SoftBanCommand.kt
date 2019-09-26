package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.getTextChannelByNullableId
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments

class SoftBanCommand : AbstractCommand("softban", category = CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["SOFTBAN_DESCRIPTION"]
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

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("menção/ID" to "ID ou menção do usuário que será banido",
				"dias" to "(Opcional) Quantos dias serão deletados, no máximo 7",
				"motivo" to "(Opcional) Motivo do Softban")
	}

	override fun getExamples(): List<String> {
		return listOf("@Fulano", "@Fulano Algum motivo bastante aleatório", "@Fulano 1 Limpar mensagens do último dia")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.BAN_MEMBERS)
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.BAN_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			try {
				val user = context.getUserAt(0)
				var rawArgs = context.rawArgs
				rawArgs = rawArgs.remove(0) // remove o usuário

				var days = 7
				if (context.args.size > 1 && context.args[1].toIntOrNull() != null) {
					days = context.args[1].toInt()
					rawArgs = rawArgs.remove(0) // remove o tempo
				}

				if (days > 7) {
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["SOFTBAN_FAIL_MORE_THAN_SEVEN_DAYS"])
					return
				}
				if (0 > days) {
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["SOFTBAN_FAIL_LESS_THAN_ZERO_DAYS"])
					return
				}

				var reason = rawArgs.joinToString(" ")
				if (user == null) {
					context.reply(
							LoriReply(
									locale["BAN_UserDoesntExist"],
									Constants.ERROR
							)
					)
					return
				}

				val member = context.guild.getMember(user)

				if (member == null) {
					context.reply(
							LoriReply(
									locale["BAN_UserNotInThisServer"],
									Constants.ERROR
							)
					)
					return
				}

				if (!context.guild.selfMember.canInteract(member)) {
					context.reply(
							LoriReply(
									locale["BAN_RoleTooLow"],
									Constants.ERROR
							)
					)
					return
				}

				if (!context.handle.canInteract(member)) {
					context.reply(
							LoriReply(
									locale["BAN_PunisherRoleTooLow"],
									Constants.ERROR
							)
					)
					return
				}

				var str = locale["BAN_ReadyToPunish", locale["SOFTBAN_PunishName"], member.asMention, member.user.name + "#" + member.user.discriminator, member.user.id]

				val hasSilent = context.config.moderationConfig.sendPunishmentViaDm || context.config.moderationConfig.sendToPunishLog
				if (context.config.moderationConfig.sendPunishmentViaDm || context.config.moderationConfig.sendToPunishLog) {
					str += " ${locale["BAN_SilentTip"]}"
				}

				val message = context.reply(
						LoriReply(
								message = str,
								prefix = "⚠"
						)
				)

				message.onReactionAddByAuthor(context) {
					if (it.reactionEmote.isEmote("✅") || it.reactionEmote.isEmote("\uD83D\uDE4A")) {
						var isSilent = it.reactionEmote.isEmote("\uD83D\uDE4A")

						SoftBanCommand.softBan(context, locale, member, 7, user, reason, isSilent)

						message.delete().queue()

						context.reply(
								LoriReply(
										locale["BAN_SuccessfullyPunished"],
										"\uD83C\uDF89"
								)
						)
					}
					return@onReactionAddByAuthor
				}

				message.addReaction("✅").queue()
				if (hasSilent) {
					message.addReaction("\uD83D\uDE4A").queue()
				}
			} catch (e: Exception) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["SOFTBAN_NO_PERM"])
			}
		} else {
			this.explain(context)
		}
	}

	companion object {
		fun softBan(context: CommandContext, locale: LegacyBaseLocale, member: Member, days: Int, user: User, reason: String, isSilent: Boolean) {
			if (!isSilent) {
				if (context.config.moderationConfig.sendPunishmentViaDm && context.guild.isMember(user)) {
					try {
						val embed = AdminUtils.createPunishmentMessageSentViaDirectMessage(context.guild, locale, context.userHandle, locale["SOFTBAN_PunishAction"], reason)

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
										"punishment" to locale["SOFTBAN_PunishAction"],
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

			context.guild.ban(member, days, locale["BAN_PunishedBy"] + " ${context.userHandle.name}#${context.userHandle.discriminator} — ${locale["BAN_PunishmentReason"]}: $reason".substringIfNeeded(0 until 512))
					.queue {
						context.guild.unban(user).queue()
					}
		}
	}
}