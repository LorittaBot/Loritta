package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Mutes
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.User
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class UnmuteCommand : AbstractCommand("unmute", listOf("desmutar", "desilenciar", "desilenciar"), CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["UNMUTE_DESCRIPTION"]
	}

	override fun getUsage(locale: BaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.USER) {
				optional = false
			}
		}
	}

	override fun getExamples(): List<String> {
		return listOf("159985870458322944")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_ROLES, Permission.MANAGE_PERMISSIONS)
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val user = context.getUserAt(0)

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

			if (member != null) {
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
			}

			val (reason, skipConfirmation, silent, delDays) = AdminUtils.getOptions(context) ?: return

			val banCallback: suspend (Message?, Boolean) -> (Unit) = { message, isSilent ->
				UnmuteCommand.unmute(context.config, context.guild, context.userHandle, locale, user, reason, isSilent)

				message?.delete()?.queue()

				context.reply(
						LoriReply(
								locale.format { commands.moderation.unmute.sucessfullyUnmuted },
								"\uD83C\uDF89"
						)
				)
			}

			if (skipConfirmation) {
				banCallback.invoke(null, false)
				return
			}

			var str = locale["BAN_ReadyToPunish", locale.format { commands.moderation.unmute.punishName }, user.asMention, user.name + "#" + user.discriminator, user.id]

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
				if (it.reactionEmote.name == "✅" || it.reactionEmote.name == "\uD83D\uDE4A") {
					banCallback.invoke(message, it.reactionEmote.name == "\uD83D\uDE4A")
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
		fun unmute(serverConfig: ServerConfig, guild: Guild, punisher: User, locale: BaseLocale, user: User, reason: String, isSilent: Boolean) {
			if (!isSilent) {
				if (serverConfig.moderationConfig.sendToPunishLog) {
					val textChannel = guild.getTextChannelById(serverConfig.moderationConfig.punishmentLogChannelId)

					if (textChannel != null && textChannel.canTalk()) {
						val message = MessageUtils.generateMessage(
								serverConfig.moderationConfig.punishmentLogMessage,
								listOf(user),
								guild,
								mutableMapOf(
										"reason" to reason,
										"punishment" to locale.format { commands.moderation.unmute.punishAction },
										"staff" to punisher.name,
										"@staff" to punisher.asMention,
										"staff-discriminator" to punisher.discriminator,
										"staff-avatar-url" to punisher.avatarUrl,
										"staff-id" to punisher.id
								)
						)

						textChannel.sendMessage(message).queue()
					}
				}
			}

			val member = guild.getMember(user)

			if (member != null) {
				val mutedRoles = MuteCommand.getMutedRole(guild, locale)

				val thread = MuteCommand.roleRemovalJobs[member.guild.id + "#" + member.user.id]
				thread?.cancel()
				MuteCommand.roleRemovalJobs.remove(member.guild.id + "#" + member.user.id)

				if (mutedRoles != null) {
					member.guild.controller.removeSingleRoleFromMember(member, mutedRoles).queue()
				}
			}

			transaction(Databases.loritta) {
				Mutes.deleteWhere {
					(Mutes.guildId eq guild.idLong) and (Mutes.userId eq member.user.idLong)
				}
			}
		}
	}
}