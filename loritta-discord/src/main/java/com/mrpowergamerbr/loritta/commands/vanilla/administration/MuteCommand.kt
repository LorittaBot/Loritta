package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Mute
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Mutes
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.locale.getLegacyBaseLocale
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.exceptions.HierarchyException
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.PunishmentAction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class MuteCommand : AbstractCommand("mute", listOf("mutar", "silenciar"), CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["$LOCALE_PREFIX.mute.description"]
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

	override fun getExamples(locale: LegacyBaseLocale): List<String> {
		return listOf("159985870458322944", "159985870458322944 ${locale.toNewLocale()["$LOCALE_PREFIX.ban.randomReason"]}")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_ROLES, Permission.MANAGE_PERMISSIONS, Permission.MANAGE_CHANNEL)
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		if (context.args.isEmpty()) return this.explain(context)

		val (users, rawReason) = AdminUtils.checkAndRetrieveAllValidUsersFromMessages(context) ?: return
		val members = context.getValidMembersForPunishment(users)

		if (members.isEmpty()) return

		val setHour = context.reply(
				LorittaReply(
						context.locale["$LOCALE_PREFIX.setPunishmentTime"],
						"⏰"
				)
		)

		val settings = AdminUtils.retrieveModerationInfo(context.config)

		suspend fun punishUser(time: Long?) {
			val (reason, skipConfirmation, silent) = AdminUtils.getOptions(context, rawReason) ?: return

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
				if (it.reactionEmote.isEmote("✅") || it.reactionEmote.isEmote("\uD83D\uDE4A")) {
					val isSilent = it.reactionEmote.isEmote("\uD83D\uDE4A")

					message.delete().queue()

					for (member in members) {
						val result = muteUser(context, settings, member, time, locale, member.user, reason, isSilent)

						if (!result)
							continue
					}

					context.reply(
							LorittaReply(
									locale.toNewLocale()["${LOCALE_PREFIX}.successfullyPunished"] + " ${Emotes.LORI_RAGE}",
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
			val time = it.message.contentDisplay.convertToEpochMillisRelativeToNow()
			punishUser(time)
		}

		setHour.onReactionAddByAuthor(context) {
			if (it.reactionEmote.isEmote("\uD83D\uDD04")) {
				setHour.delete().queue()
				punishUser(null)
			}
		}

		setHour.addReaction("\uD83D\uDD04").queue()
	}

	companion object {
		private const val LOCALE_PREFIX = "commands.moderation"
		private val logger = KotlinLogging.logger {}

		// Para guardar as threads, a key deverá ser...
		// ID da guild#ID do usuário
		// Exemplo:
		// 297732013006389252#123170274651668480
		val roleRemovalJobs = ConcurrentHashMap<String, Job>()

		suspend fun muteUser(context: CommandContext, settings: AdminUtils.ModerationConfigSettings, member: Member, time: Long?, locale: LegacyBaseLocale, user: User, reason: String, isSilent: Boolean): Boolean {
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
				val userLocale = user.getLorittaProfile()?.getLegacyBaseLocale(loritta, locale)
						?: context.guildLegacyLocale
				val guildLocale = context.guildLocale

				if (settings.sendPunishmentViaDm && context.guild.isMember(user)) {
					try {
						val embed = AdminUtils.createPunishmentEmbedBuilderSentViaDirectMessage(context.guild, userLocale, context.userHandle, userLocale.toNewLocale()["$LOCALE_PREFIX.mute.punishAction"], reason)

						val timePretty = if (time != null)
							DateUtils.formatDateDiff(System.currentTimeMillis(), time, context.guildLegacyLocale)
						else guildLocale["commands.moderation.mute.forever"]

						embed.addField(
								guildLocale["commands.moderation.mute.duration"],
								timePretty,
								false
						)

						user.openPrivateChannel().await().sendMessage(embed.build()).queue()
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}

				val punishLogMessage = AdminUtils.getPunishmentForMessage(
						settings,
						context.guild,
						PunishmentAction.MUTE
				)

				if (settings.sendPunishmentToPunishLog && settings.punishLogChannelId != null && punishLogMessage != null) {
					val textChannel = context.guild.getTextChannelById(settings.punishLogChannelId)

					if (textChannel != null && textChannel.canTalk()) {
						val message = MessageUtils.generateMessage(
								punishLogMessage,
								listOf(user, context.guild),
								context.guild,
								mutableMapOf(
										"duration" to if (delay != null) {
											DateUtils.formatMillis(delay, locale)
										} else {
											guildLocale["commands.moderation.mute.forever"]
										}
								) + AdminUtils.getStaffCustomTokens(context.userHandle)
										+ AdminUtils.getPunishmentCustomTokens(guildLocale, reason, "${LOCALE_PREFIX}.mute")
						)

						message?.let {
							textChannel.sendMessage(it).queue()
						}
					}
				}
			}

			// Vamos pegar se a nossa role existe
			val mutedRoleName = context.guildLocale["$LOCALE_PREFIX.mute.roleName"]
			val mutedRoles = context.guild.getRolesByName(mutedRoleName, false)
			val mutedRole: Role?
			mutedRole = if (mutedRoles.isEmpty()) {
				// Se não existe, vamos criar ela!
				context.guild.createRole()
						.setName(mutedRoleName)
						.setColor(Color.BLACK)
						.await()
			} else {
				// Se existe, vamos carregar a atual
				mutedRoles[0]
			}

			val couldntEditChannels = mutableListOf<GuildChannel>()

			// E agora vamos pegar todos os canais de texto do servidor
			run {
				var processedRequests = 0
				for (textChannel in context.guild.textChannels) {
					if (context.guild.selfMember.hasPermission(textChannel, Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS)) {
						val permissionOverride = textChannel.getPermissionOverride(mutedRole)
						if (permissionOverride == null) { // Se é null...
							textChannel.createPermissionOverride(mutedRole)
									.setDeny(Permission.MESSAGE_WRITE) // kk eae men, daora ficar mutado né
									.queueAfter(processedRequests * 2L, TimeUnit.SECONDS)
							processedRequests++
						} else {
							if (!permissionOverride.denied.contains(Permission.MESSAGE_WRITE)) {
								permissionOverride.manager
										.deny(Permission.MESSAGE_WRITE) // kk eae men, daora ficar mutado né
										.queueAfter(processedRequests * 2L, TimeUnit.SECONDS)
								processedRequests++
							}
						}
					} else {
						couldntEditChannels.add(textChannel)
					}
				}
			}

			// E agora os canais de voz
			run {
				var processedRequests = 0
				for (voiceChannel in context.guild.voiceChannels) {
					if (context.guild.selfMember.hasPermission(voiceChannel, Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS)) {
						val permissionOverride = voiceChannel.getPermissionOverride(mutedRole)
						if (permissionOverride == null) { // Se é null...
							voiceChannel.createPermissionOverride(mutedRole)
									.setDeny(Permission.VOICE_SPEAK) // kk eae men, daora ficar mutado né
									.queueAfter(processedRequests * 2L, TimeUnit.SECONDS)
							processedRequests++
						} else {
							if (!permissionOverride.denied.contains(Permission.VOICE_SPEAK)) {
								permissionOverride.manager
										.deny(Permission.VOICE_SPEAK) // kk eae men, daora ficar mutado né
										.queueAfter(processedRequests * 2L, TimeUnit.SECONDS)
								processedRequests++
							}
						}
					} else {
						couldntEditChannels.add(voiceChannel)
					}
				}
			}

			// E... finalmente... iremos dar (ou remover) a role para o carinha
			if (!context.guild.isMember(member.user)) {
				context.reply(
                        LorittaReply(
                                context.locale["commands.userNotOnTheGuild", "${user.asMention} (`${user.name.stripCodeMarks()}#${user.discriminator} (${user.idLong})`)"],
                                Emotes.LORI_HM
                        )
				)
				return false
			}

			if (couldntEditChannels.isNotEmpty()) {
				context.reply(
                        LorittaReply(
                                context.locale["$LOCALE_PREFIX.mute.couldntEditChannel", couldntEditChannels.joinToString(", ", transform = { "`" + it.name.stripCodeMarks() + "`" })],
                                Constants.ERROR
                        )
				)
			}

			try {
				val addRole = context.guild.addRoleToMember(member, mutedRole)

				addRole.await()

				transaction(Databases.loritta) {
					Mutes.deleteWhere {
						(Mutes.guildId eq context.guild.idLong) and (Mutes.userId eq member.user.idLong)
					}

					Mute.new {
						guildId = context.guild.idLong
						userId = member.user.idLong
						punishedById = context.userHandle.idLong
						receivedAt = System.currentTimeMillis()
						content = reason

						if (time != null) {
							isTemporary = true
							expiresAt = time
						} else {
							isTemporary = false
						}
					}
				}

				if (delay != null) {
					// Ao enviar um role change, iremos esperar alguns segundos para ver se o mute foi realmente "aplicado"
					for (x in 0..9) {
						if (member.roles.contains(mutedRole))
							break
						delay(250)
					}
					spawnRoleRemovalThread(context.guild, context.guildLegacyLocale, user, time!!)
				}
			} catch (e: HierarchyException) {
				return AdminUtils.checkForPermissions(context, member)
			}

			return true
		}

		fun getMutedRole(guild: Guild, locale: BaseLocale) = guild.getRolesByName(locale["$LOCALE_PREFIX.mute.roleName"], false).getOrNull(0)

		fun spawnRoleRemovalThread(guild: Guild, locale: LegacyBaseLocale, user: User, expiresAt: Long) = spawnRoleRemovalThread(guild.idLong, locale, user.idLong, expiresAt)

		fun spawnRoleRemovalThread(guildId: Long, locale: LegacyBaseLocale, userId: Long, expiresAt: Long) {
			val jobId = "$guildId#$userId"
			logger.info("Criando role removal thread para usuário $userId na guild $guildId!")

			val previousJob = roleRemovalJobs[jobId]
			if (previousJob != null) {
				logger.info("Interrompendo job de $userId na guild $guildId! Criar outra removal job enquanto uma já está ativa é feio!")
				roleRemovalJobs.remove("$guildId#$userId")
				previousJob.cancel() // lol nope
			}

			val currentGuild = lorittaShards.getGuildById(guildId)

			if (currentGuild == null) {
				logger.warn("Bem... na verdade a guild $guildId não existe, então não iremos remover o estado de silenciado de $userId por enquanto...")
				return
			}

			// Vamos pegar se a nossa role existe
			val mutedRole = getMutedRole(currentGuild, locale.toNewLocale())

			if (System.currentTimeMillis() > expiresAt) {
				logger.info("Removendo cargo silenciado de $userId na guild $guildId - Motivo: Já expirou!")

				val guild = lorittaShards.getGuildById(guildId.toString())

				if (guild == null) {
					logger.warn("Bem... na verdade a guild $guildId não existe mais, então não iremos remover o estado de silenciado de $userId por enquanto...")
					return
				}

				// Maybe the user is not in the guild, but we want to remove the mute anyway, just get the member (or null)
				val member = runBlocking { guild.retrieveMemberOrNullById(userId) }

				transaction(Databases.loritta) {
					Mutes.deleteWhere {
						(Mutes.guildId eq guildId) and (Mutes.userId eq userId)
					}
				}

				if (mutedRole != null && member != null) {
					val removeRole = guild.removeRoleFromMember(member, mutedRole)
					removeRole.queue()
				}
				return
			}

			val currentMember = runBlocking { currentGuild.retrieveMemberOrNullById(userId) }

			if (currentMember == null) {
				logger.warn("Ignorando job removal de $userId em $guildId - Motivo: Ela não está mais no servidor!")
				return
			}

			if (mutedRole == null || !currentMember.roles.contains(mutedRole)) {
				if (mutedRole == null) {
					logger.info("Removendo status de silenciado de $userId na guild $guildId - Motivo: Cargo não existe mais!")
				} else {
					logger.info("Removendo status de silenciado de $userId na guild $guildId - Motivo: Usuário não possui mais o cargo!")
				}

				// Se não existe, então quer dizer que o cargo foi deletado e isto deve ser ignorado!
				transaction(Databases.loritta) {
					Mutes.deleteWhere {
						(Mutes.guildId eq guildId) and (Mutes.userId eq userId)
					}
				}
			} else {
				roleRemovalJobs["$guildId#$userId"] = GlobalScope.launch(loritta.coroutineDispatcher) {
					logger.info("Criado role removal thread de $userId na guild $guildId, irá expirar em $expiresAt")
					val delay = expiresAt - System.currentTimeMillis()
					delay(delay)
					roleRemovalJobs.remove(jobId)
					if (!this.isActive) {
						logger.warn("Então... era para retirar o status de silenciado de $userId na guild $guildId, mas pelo visto esta task já tinha sido cancelada, whoops!!")
						return@launch
					}

					val guild = lorittaShards.getGuildById(guildId)
					if (guild == null) {
						logger.warn("Então... era para retirar o status de silenciado de $userId na guild $guildId, mas a guild não existe mais!")
						return@launch
					}

					val settings = AdminUtils.retrieveModerationInfo(loritta.getOrCreateServerConfig(guildId))

					UnmuteCommand.unmute(
							settings,
							guild,
							guild.selfMember.user,
							locale,
							currentMember.user,
							locale.toNewLocale()["commands.moderation.unmute.automaticallyExpired", "<:lori_owo:417813932380520448>"],
							false
					)
				}
			}
		}
	}
}