package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import com.google.common.collect.Sets
import kotlinx.coroutines.*
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.HierarchyException
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.tables.Mutes
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.ModerationLogAction
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.dao.Mute
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class MuteCommand(loritta: LorittaBot) : AbstractCommand(loritta, "mute", listOf("mutar", "silenciar"), net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.mute.description")
	override fun getExamplesKey() = AdminUtils.PUNISHMENT_EXAMPLES_KEY
	override fun getUsage() = AdminUtils.PUNISHMENT_USAGES

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_ROLES, Permission.MANAGE_PERMISSIONS, Permission.MANAGE_CHANNEL)
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

			val setHour = context.reply(
				LorittaReply(
					context.locale["commands.category.moderation.setPunishmentTime"],
					"⏰"
				)
			)

			val settings = AdminUtils.retrieveModerationInfo(loritta, context.config)

			suspend fun punishUser(time: Long?) {
				val (reason, skipConfirmation, silent, delDays) = AdminUtils.getOptions(context, rawReason) ?: return

				if (skipConfirmation) {
					for (member in members) {
						val result = muteUser(context, settings, member, time, locale, member.user, reason, silent)

						if (!result)
							continue
					}

					AdminUtils.sendSuccessfullyPunishedMessage(context, reason, true)
					return
				}

				val hasSilent = settings.sendPunishmentViaDm || settings.sendPunishmentToPunishLog
				val message = AdminUtils.sendConfirmationMessage(context, users, hasSilent, "mute")

				message.onReactionAddByAuthor(context) {
					if (it.emoji.isEmote("✅") || it.emoji.isEmote("\uD83D\uDE4A")) {
						val isSilent = it.emoji.isEmote("\uD83D\uDE4A")

						message.delete().queue()

						for (member in members) {
							val result = muteUser(context, settings, member, time, locale, member.user, reason, isSilent)

							if (!result)
								continue
						}

						context.reply(
							LorittaReply(
								locale["commands.category.moderation.successfullyPunished"] + " ${Emotes.LORI_RAGE}",
								"\uD83C\uDF89"
							)
						)
					}
				}

				message.addReaction("✅").queue()
				if (hasSilent) {
					message.addReaction("\uD83D\uDE4A").queue()
				}
			}

			setHour.onResponseByAuthor(context) {
				setHour.delete().queue()
				val time = TimeUtils.convertToMillisRelativeToNow(it.message.contentDisplay)
				punishUser(time)
			}

			setHour.onReactionAddByAuthor(context) {
				if (it.emoji.isEmote("\uD83D\uDD04")) {
					setHour.delete().queue()
					punishUser(null)
				}
			}

			setHour.addReaction("\uD83D\uDD04").queue()
		} else {
			this.explain(context)
		}
	}

	companion object {
		private val LOCALE_PREFIX = "commands.command"
		private val logger by HarmonyLoggerFactory.logger {}

		// Para guardar as threads, a key deverá ser...
		// ID da guild#ID do usuário
		// Exemplo:
		// 297732013006389252#123170274651668480
		val roleRemovalJobs = ConcurrentHashMap<String, Job>()

		// This is used to avoid spamming Discord with retrieveMemberById requests, by storing the not in server IDs and NEVER CHECKING IT AGAIN (until they join the server again owo)
		val notInTheServerUserIds = Sets.newConcurrentHashSet<Long>()

		suspend fun muteUser(context: CommandContext, settings: AdminUtils.ModerationConfigSettings, member: Member, time: Long?, locale: BaseLocale, user: User, reason: String, isSilent: Boolean): Boolean {
			val delay = if (time != null) {
				time - System.currentTimeMillis()
			} else {
				null
			}

			if (delay != null && 0 > delay) {
				// :whatdog:
				context.reply(
					LorittaReply(
						context.locale["$LOCALE_PREFIX.mute.negativeTime"],
						Constants.ERROR
					)
				)
				return false
			}

			if (!isSilent) {
				if (settings.sendPunishmentViaDm && context.guild.isMember(user)) {
					try {
						val embed = AdminUtils.createPunishmentEmbedBuilderSentViaDirectMessage(context.guild, locale, context.userHandle, locale["commands.command.mute.punishAction"], reason)

						val timePretty = if (time != null)
							DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(time)
						else context.locale["commands.command.mute.forever"]

						embed.addField(
							context.locale["commands.command.mute.duration"],
							timePretty,
							false
						)

						context.loritta.getOrRetrievePrivateChannelForUser(user).sendMessageEmbeds(embed.build()).queue()
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}

				val punishLogMessage = AdminUtils.getPunishmentForMessage(
					context.loritta,
					settings,
					context.guild,
					PunishmentAction.MUTE
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
								"duration" to if (delay != null) {
									DateUtils.formatMillis(delay, locale)
								} else {
									locale["commands.command.mute.forever"]
								}
							) + AdminUtils.getStaffCustomTokens(context.userHandle)
									+ AdminUtils.getPunishmentCustomTokens(locale, reason, "${LOCALE_PREFIX}.mute"),
							I18nKeysData.InvalidMessages.MemberModerationMute
						)

						textChannel.sendMessage(message).queue()
					}
				}
			}

			try {
				// When adding a timeout, we can't set the timeout to *exactly* the expiration time
				// So we will play around a bit with it
				val userWasTimedOutForDuration = if (delay != null) {
					val howMuchTimeTheUserWillBeTimedOut = Duration.ofMillis(delay)

					// Discord allows you to timeout someone for max 28 days, so we will coerce it for at most 28 days
					howMuchTimeTheUserWillBeTimedOut.coerceAtMost(Duration.ofDays(28))
				} else {
					Duration.ofDays(28)
				}

				member.timeoutFor(userWasTimedOutForDuration).await()
				val userTimedOutUntil = Instant.now().plus(userWasTimedOutForDuration)

				val mute = context.loritta.pudding.transaction {
					Mutes.deleteWhere {
						(Mutes.guildId eq context.guild.idLong) and (Mutes.userId eq member.user.idLong)
					}

					val mute = Mute.new {
						guildId = context.guild.idLong
						userId = member.user.idLong
						punishedById = context.userHandle.idLong
						receivedAt = System.currentTimeMillis()
						content = reason
						// We will store for how long the user was timed out for, so Loritta can automatically update the timeout time when the timeout expires
						this.userTimedOutUntil = userTimedOutUntil

						if (time != null) {
							isTemporary = true
							expiresAt = time
						} else {
							isTemporary = false
						}
					}

					context.loritta.pudding.moderationLogs.logPunishment(
						context.guild.idLong,
						member.user.idLong,
						context.userHandle.idLong,
						ModerationLogAction.MUTE,
						reason,
						time?.let { Instant.ofEpochMilli(it) }
					)

					mute
				}

				spawnTimeOutUpdaterThread(context.loritta, context.guild, context.locale, context.i18nContext, user, mute)
			} catch (e: HierarchyException) {
				val reply = buildString {
					this.append(context.locale[AdminUtils.ROLE_TOO_LOW_KEY])

					if (context.handle.hasPermission(Permission.MANAGE_ROLES)) {
						this.append(" ")
						this.append(context.locale[AdminUtils.ROLE_TOO_LOW_HOW_TO_FIX_KEY])
					}
				}

				context.reply(
					LorittaReply(
						reply,
						Constants.ERROR
					)
				)
				return false
			}

			return true
		}

		// The same thing as above but ooo now it uses UnleashedContext
		// WE REALLY NEED TO REMOVE THE OLD MUTEUSER AFTER ALL CALLERS ARE MIGRATED TO THIS VERSION!!
		suspend fun muteUser(context: UnleashedContext, settings: AdminUtils.ModerationConfigSettings, member: Member, time: Long?, locale: BaseLocale, user: User, reason: String, isSilent: Boolean): Boolean {
			val delay = if (time != null) {
				time - System.currentTimeMillis()
			} else {
				null
			}

			if (delay != null && 0 > delay) {
				// :whatdog:
				context.reply(true) {
					styled(
						context.locale["$LOCALE_PREFIX.mute.negativeTime"],
						Constants.ERROR
					)
				}
				return false
			}

			if (!isSilent) {
				if (settings.sendPunishmentViaDm && context.guild.isMember(user)) {
					try {
						val embed = AdminUtils.createPunishmentEmbedBuilderSentViaDirectMessage(context.guild, locale, context.user, locale["commands.command.mute.punishAction"], reason)

						val timePretty = if (time != null)
							DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(time)
						else context.locale["commands.command.mute.forever"]

						embed.addField(
							context.locale["commands.command.mute.duration"],
							timePretty,
							false
						)

						context.loritta.getOrRetrievePrivateChannelForUser(user).sendMessageEmbeds(embed.build()).queue()
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}

				val punishLogMessage = AdminUtils.getPunishmentForMessage(
					context.loritta,
					settings,
					context.guild,
					PunishmentAction.MUTE
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
								"duration" to if (delay != null) {
									DateUtils.formatMillis(delay, locale)
								} else {
									locale["commands.command.mute.forever"]
								}
							) + AdminUtils.getStaffCustomTokens(context.user) + AdminUtils.getPunishmentCustomTokens(locale, reason, "${LOCALE_PREFIX}.mute"),
							I18nKeysData.InvalidMessages.MemberModerationMute
						)

						textChannel.sendMessage(message).queue()
					}
				}
			}

			try {
				// When adding a timeout, we can't set the timeout to *exactly* the expiration time
				// So we will play around a bit with it
				val userWasTimedOutForDuration = if (delay != null) {
					val howMuchTimeTheUserWillBeTimedOut = Duration.ofMillis(delay)

					// Discord allows you to timeout someone for max 28 days, so we will coerce it for at most 28 days
					howMuchTimeTheUserWillBeTimedOut.coerceAtMost(Duration.ofDays(28))
				} else {
					Duration.ofDays(28)
				}

				member.timeoutFor(userWasTimedOutForDuration).await()
				val userTimedOutUntil = Instant.now().plus(userWasTimedOutForDuration)

				val mute = context.loritta.pudding.transaction {
					Mutes.deleteWhere {
						(Mutes.guildId eq context.guild.idLong) and (Mutes.userId eq member.user.idLong)
					}

					val mute = Mute.new {
						guildId = context.guild.idLong
						userId = member.user.idLong
						punishedById = context.user.idLong
						receivedAt = System.currentTimeMillis()
						content = reason
						// We will store for how long the user was timed out for, so Loritta can automatically update the timeout time when the timeout expires
						this.userTimedOutUntil = userTimedOutUntil

						if (time != null) {
							isTemporary = true
							expiresAt = time
						} else {
							isTemporary = false
						}
					}

					context.loritta.pudding.moderationLogs.logPunishment(
						context.guild.idLong,
						member.user.idLong,
						context.user.idLong,
						ModerationLogAction.MUTE,
						reason,
						time?.let { Instant.ofEpochMilli(it) }
					)

					mute
				}

				spawnTimeOutUpdaterThread(context.loritta, context.guild, context.locale, context.i18nContext, user, mute)
			} catch (e: HierarchyException) {
				val reply = buildString {
					this.append(context.locale[AdminUtils.ROLE_TOO_LOW_KEY])

					if (context.member.hasPermission(Permission.MANAGE_ROLES)) {
						this.append(" ")
						this.append(context.locale[AdminUtils.ROLE_TOO_LOW_HOW_TO_FIX_KEY])
					}
				}

				context.reply(true) {
					styled(
						reply,
						Constants.ERROR
					)
				}
				return false
			}

			return true
		}

		fun getMutedRole(loritta: LorittaBot, guild: Guild, locale: BaseLocale) = guild.getRolesByName(locale["$LOCALE_PREFIX.mute.roleName"], false).getOrNull(0)

		fun spawnTimeOutUpdaterThread(loritta: LorittaBot, guild: Guild, locale: BaseLocale, i18nContext: I18nContext, user: User, mute: Mute) = spawnTimeOutUpdaterThread(loritta, guild.idLong, locale, i18nContext, user.idLong, mute)

		fun spawnTimeOutUpdaterThread(
			loritta: LorittaBot,
			guildId: Long,
			locale: BaseLocale,
			i18nContext: I18nContext,
			userId: Long,
			mute: Mute
		) {
			val jobId = "$guildId#$userId"
			logger.info { "Criando timeout updater thread para usuário $userId na guild $guildId!" }

			val previousJob = roleRemovalJobs[jobId]
			if (previousJob != null) {
				logger.info { "Interrompendo job de $userId na guild $guildId! Criar outra removal job enquanto uma já está ativa é feio!" }
				roleRemovalJobs.remove("$guildId#$userId")
				previousJob.cancel() // lol nope
			}

			val currentGuild = loritta.lorittaShards.getGuildById(guildId)

			if (currentGuild == null) {
				logger.warn { "Bem... na verdade a guild $guildId não existe, então não iremos remover o estado de silenciado de $userId por enquanto..." }
                return
			}

			val muteExpiresAt = mute.expiresAt
			if (muteExpiresAt != null && System.currentTimeMillis() > muteExpiresAt) {
				logger.info { "Removendo cargo silenciado de $userId na guild $guildId - Motivo: Já expirou!" }

                val guild = loritta.lorittaShards.getGuildById(guildId.toString())

				if (guild == null) {
					logger.warn { "Bem... na verdade a guild $guildId não existe mais, então não iremos remover o estado de silenciado de $userId por enquanto..." }
                    return
				}

				runBlocking {
					loritta.pudding.transaction {
						Mutes.deleteWhere {
							(Mutes.guildId eq guildId) and (Mutes.userId eq userId)
						}
					}
				}

				// We don't need to do anything here, Discord already handles the timeout time for us
				return
			}

			roleRemovalJobs["$guildId#$userId"] = GlobalScope.launch(loritta.coroutineDispatcher) {
				logger.info { "Criado timeout updater thread de $userId na guild $guildId, irá expirar em ${mute.expiresAt}, time out irá expirar em ${mute.userTimedOutUntil}" }

				// Wait until the timeout expires...
				while (true) {
					val timeOutExpirationDelay = (mute.userTimedOutUntil?.toEpochMilli() ?: 0L) - System.currentTimeMillis()
					delay(timeOutExpirationDelay)

					val guild = loritta.lorittaShards.getGuildById(guildId)
					if (guild == null) {
						logger.warn { "Então... era para atualizar o timeout de $userId na guild $guildId, mas a guild não existe mais!" }
						return@launch
					}

					val settings = AdminUtils.retrieveModerationInfo(loritta, loritta.getOrCreateServerConfig(guildId))

					val currentMember = if (userId in notInTheServerUserIds) null else runBlocking {
						currentGuild.retrieveMemberOrNullById(userId)
					}

					if (currentMember == null) {
						logger.warn { "Ignorando job removal de $userId em $guildId - Motivo: Ela não está mais no servidor!" }
						notInTheServerUserIds.add(userId)

						runBlocking {
							loritta.pudding.transaction {
								Mutes.deleteWhere {
									(Mutes.guildId eq guildId) and (Mutes.userId eq userId)
								}
							}
						}
						return@launch
					}

					// Update the timeout if needed
					val muteExpiresAt = mute.expiresAt
					if (muteExpiresAt != null && System.currentTimeMillis() >= muteExpiresAt) {
						// Mute has already expired!
						logger.info { "$userId mute in $guildId expired, removing their mute..." }

						roleRemovalJobs.remove(jobId)
						if (!this.isActive) {
							logger.warn { "Então... era para retirar o status de silenciado de $userId na guild $guildId, mas pelo visto esta task já tinha sido cancelada, whoops!!" }
							return@launch
						}

						UnmuteCommand.unmute(
							loritta,
							i18nContext,
							settings,
							guild,
							guild.selfMember.user,
							locale,
							currentMember.user,
							locale["commands.command.unmute.automaticallyExpired", "<:lori_owo:417813932380520448>"],
							false
						)
						return@launch
					}

					val userWasTimedOutForDuration = if (muteExpiresAt != null) {
						val howMuchTimeTheUserWillBeTimedOut =
							Duration.ofMillis(muteExpiresAt - System.currentTimeMillis())

						// Discord allows you to timeout someone for max 28 days, so we will coerce it for at most 28 days
						howMuchTimeTheUserWillBeTimedOut.coerceAtMost(Duration.ofDays(28))
					} else {
						Duration.ofDays(28)
					}

					logger.info { "Updating $currentMember timeout..." }
					currentMember.timeoutFor(userWasTimedOutForDuration).await()

					loritta.transaction {
						// Update the timed out time
						mute.userTimedOutUntil = Instant.now().plus(userWasTimedOutForDuration)
					}
				}
			}
		}
	}
}
