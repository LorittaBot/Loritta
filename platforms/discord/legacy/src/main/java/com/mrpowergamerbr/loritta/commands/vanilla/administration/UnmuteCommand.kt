package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Mutes
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.extensions.retrieveMemberOrNull
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.PunishmentAction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class UnmuteCommand : AbstractCommand("unmute", listOf("desmutar", "desilenciar", "desilenciar"), CommandCategory.MODERATION) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.unmute.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.unmute.examples")
	override fun getUsage() = AdminUtils.PUNISHMENT_USAGES

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
                                locale["commands.command.unmute.successfullyUnmuted"],
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
		fun unmute(settings: AdminUtils.ModerationConfigSettings, guild: Guild, punisher: User, locale: BaseLocale, user: User, reason: String, isSilent: Boolean) {
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
										"duration" to locale["commands.command.mute.forever"]
								) + AdminUtils.getStaffCustomTokens(punisher)
										+ AdminUtils.getPunishmentCustomTokens(locale, reason, "commands.command.unmute")
						)

						message?.let {
							textChannel.sendMessage(it).queue()
						}
					}
				}
			}

			// Cancel the roleRemovalJob (if it exists, it may not exist at all!)
			val roleRemovalKey = guild.id + "#" + user.id
			val thread = MuteCommand.roleRemovalJobs[roleRemovalKey]
			thread?.cancel()
			MuteCommand.roleRemovalJobs.remove(roleRemovalKey)

			// Delete the mute from the database, this avoids the MutedUserTask rechecking the mute again even after it was deleted
			transaction(Databases.loritta) {
				Mutes.deleteWhere {
					(Mutes.guildId eq guild.idLong) and (Mutes.userId eq user.idLong)
				}
			}

			// And now remove the "Muted" role if needed!
			val member = guild.getMember(user)

			if (member != null) {
				val mutedRoles = MuteCommand.getMutedRole(guild, locale)
				if (mutedRoles != null)
					guild.removeRoleFromMember(member, mutedRoles).queue()
			}
		}
	}
}