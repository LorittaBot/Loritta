package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Mutes
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.extensions.retrieveMemberOrNull
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
import net.perfectdreams.loritta.utils.PunishmentAction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class UnmuteCommand : AbstractCommand("unmute", listOf("desmutar", "desilenciar", "desilenciar"), CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["UNMUTE_DESCRIPTION"]
	}

	override fun getUsage(locale: LegacyBaseLocale): CommandArguments {
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

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val (users, rawReason) = AdminUtils.checkAndRetrieveAllValidUsersFromMessages(context) ?: return

			for (user in users) {
				val member = context.guild.retrieveMemberOrNull(user)

				if (member != null) {
					if (!AdminUtils.checkForPermissions(context, member))
						return
				}
			}

			val (reason, skipConfirmation, silent, delDays) = AdminUtils.getOptions(context, rawReason) ?: return
			val settings = AdminUtils.retrieveModerationInfo(context.config)

			val banCallback: suspend (Message?, Boolean) -> (Unit) = { message, isSilent ->
				for (user in users)
					unmute(settings, context.guild, context.userHandle, locale, user, reason, isSilent)

				message?.delete()?.queue()

				context.reply(
                        LorittaReply(
                                locale.toNewLocale()["commands.moderation.unmute.successfullyUnmuted"],
                                "\uD83C\uDF89"
                        )
				)
			}

			if (skipConfirmation) {
				banCallback.invoke(null, false)
				return
			}

			val hasSilent = settings.sendPunishmentViaDm || settings.sendPunishmentToPunishLog
			val message = AdminUtils.sendConfirmationMessage(context, users, hasSilent, "unmute")

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
		fun unmute(settings: AdminUtils.ModerationConfigSettings, guild: Guild, punisher: User, locale: LegacyBaseLocale, user: User, reason: String, isSilent: Boolean) {
			if (!isSilent) {
				val punishLogMessage = AdminUtils.getPunishmentForMessage(
						settings,
						guild,
						PunishmentAction.UNMUTE
				)

				if (settings.sendPunishmentToPunishLog && settings.punishLogChannelId != null && punishLogMessage != null) {
					val textChannel = guild.getTextChannelById(settings.punishLogChannelId)

					if (textChannel != null && textChannel.canTalk()) {
						val message = MessageUtils.generateMessage(
								punishLogMessage,
								listOf(user, guild),
								guild,
								mutableMapOf(
										"duration" to locale.toNewLocale()["commands.moderation.mute.forever"]
								) + AdminUtils.getStaffCustomTokens(punisher)
										+ AdminUtils.getPunishmentCustomTokens(locale.toNewLocale(), reason, "commands.moderation.unmute")
						)

						message?.let {
							textChannel.sendMessage(it).queue()
						}
					}
				}
			}

			val member = guild.getMember(user)

			if (member != null) {
				val mutedRoles = MuteCommand.getMutedRole(guild, locale.toNewLocale())

				val thread = MuteCommand.roleRemovalJobs[member.guild.id + "#" + member.user.id]
				thread?.cancel()
				MuteCommand.roleRemovalJobs.remove(member.guild.id + "#" + member.user.id)

				if (mutedRoles != null) {
					member.guild.removeRoleFromMember(member, mutedRoles).queue()
				}

				transaction(Databases.loritta) {
					Mutes.deleteWhere {
						(Mutes.guildId eq guild.idLong) and (Mutes.userId eq member.user.idLong)
					}
				}
			}
		}
	}
}