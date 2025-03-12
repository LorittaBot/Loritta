package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.google.gson.JsonParser
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.loritta.cinnamon.pudding.tables.Warns
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.dao.Warn
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.TimeUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.addReaction
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.extensions.isEmote
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNull
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class WarnCommand(loritta: LorittaBot) : AbstractCommand(loritta, "warn", listOf("aviso", "avisar"), net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {
	companion object {
		private val LOCALE_PREFIX = "commands.command"
	}

	override fun getDescriptionKey() = LocaleKeyData("commands.command.warn.description")
	override fun getExamplesKey() = AdminUtils.PUNISHMENT_EXAMPLES_KEY
	override fun getUsage() = AdminUtils.PUNISHMENT_USAGES

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS, Permission.BAN_MEMBERS)
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

			val settings = AdminUtils.retrieveModerationInfo(loritta, context.config)
			val punishmentActions = AdminUtils.retrieveWarnPunishmentActions(loritta, context.config)
			val (reason, skipConfirmation, silent, delDays) = AdminUtils.getOptions(context, rawReason) ?: return

			val warnCallback: (suspend (Message?, Boolean) -> Unit) = { message, isSilent ->
				for (user in users) {
					val member = context.guild.retrieveMemberOrNull(user)
					if (!isSilent) {
						if (settings.sendPunishmentViaDm && context.guild.isMember(user)) {
							try {
								val embed = AdminUtils.createPunishmentMessageSentViaDirectMessage(context.guild, locale, context.userHandle, locale["commands.command.warn.punishAction"], reason)

								user.openPrivateChannel().queue {
									it.sendMessageEmbeds(embed).queue()
								}
							} catch (e: Exception) {
								e.printStackTrace()
							}
						}

						val punishLogMessage = AdminUtils.getPunishmentForMessage(
							context.loritta,
							settings,
							context.guild,
							PunishmentAction.WARN
						)

						if (settings.sendPunishmentToPunishLog && settings.punishLogChannelId != null && punishLogMessage != null) {
							val textChannel = context.guild.getGuildMessageChannelById(settings.punishLogChannelId)

							if (textChannel != null && textChannel.canTalk()) {
								val message = MessageUtils.generateMessageOrFallbackIfInvalid(
									context.i18nContext,
									punishLogMessage,
									listOf(user, context.guild),
									context.guild,
									mutableMapOf(
										"duration" to locale["$LOCALE_PREFIX.mute.forever"]
									) + AdminUtils.getStaffCustomTokens(context.userHandle)
											+ AdminUtils.getPunishmentCustomTokens(locale, reason, "$LOCALE_PREFIX.warn"),
									generationErrorMessageI18nKey = I18nKeysData.InvalidMessages.MemberModerationWarn
								)

								textChannel.sendMessage(message).queue()
							}
						}
					}

					val warnCount = (
							loritta.newSuspendedTransaction {
								Warns.selectAll().where { (Warns.guildId eq context.guild.idLong) and (Warns.userId eq user.idLong) }.count()
							} + 1
							).toInt()

					val punishments = punishmentActions.filter { it.warnCount == warnCount }

					loop@ for (punishment in punishments) {
						when {
							punishment.punishmentAction == PunishmentAction.BAN -> BanCommand.ban(loritta, context.i18nContext, settings, context.guild, context.userHandle, locale, user, reason, isSilent, 0)
							member != null && punishment.punishmentAction == PunishmentAction.KICK -> KickCommand.kick(context, settings, locale, member, user, reason, isSilent)
							member != null && punishment.punishmentAction == PunishmentAction.MUTE -> {
								val metadata = punishment.metadata ?: continue@loop
								val obj = JsonParser.parseString(metadata).obj
								val time = obj["time"].nullString?.let { TimeUtils.convertToMillisRelativeToNow(it) }
								MuteCommand.muteUser(context, settings, member, time, locale, user, reason, isSilent)
							}
						}
					}

					loritta.newSuspendedTransaction {
						Warn.new {
							this.guildId = context.guild.idLong
							this.userId = user.idLong
							this.receivedAt = System.currentTimeMillis()
							this.punishedById = context.userHandle.idLong
							this.content = reason
						}
					}
				}

				message?.delete()?.queue()

				AdminUtils.sendSuccessfullyPunishedMessage(context, reason, true)
			}

			if (skipConfirmation) {
				warnCallback.invoke(null, silent)
				return
			}

			val hasSilent = settings.sendPunishmentViaDm || settings.sendPunishmentToPunishLog
			val message = AdminUtils.sendConfirmationMessage(context, users, hasSilent, "warn")

			message.onReactionAddByAuthor(context) {
				if (it.emoji.isEmote("✅") || it.emoji.isEmote("\uD83D\uDE4A")) {
					warnCallback.invoke(message, it.emoji.isEmote("\uD83D\uDE4A"))
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
}
