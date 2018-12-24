package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.dao.Mute
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Mutes
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import kotlinx.coroutines.*
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.exceptions.HierarchyException
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color

class MuteCommand : AbstractCommand("mute", listOf("mutar", "silenciar"), CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["MUTE_DESCRIPTION"]
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
		return listOf(Permission.MANAGE_ROLES, Permission.MANAGE_PERMISSIONS, Permission.MANAGE_CHANNEL)
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
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

			val setHour = context.reply(
					LoriReply(
							locale["MUTE_SetHour"],
							"⏰"
					)
			)

			suspend fun punishUser(time: Long?) {
				val (reason, skipConfirmation, silent, delDays) = AdminUtils.getOptions(context) ?: return

				if (skipConfirmation) {
					val result = muteUser(context, member, time, locale, user, reason, silent)

					if (!result) {
						return
					}

					context.reply(
							LoriReply(
									locale["BAN_SuccessfullyPunished"],
									"\uD83C\uDF89"
							)
					)
					return
				}

				var str = locale["BAN_ReadyToPunish", locale["MUTE_PunishName"], member.asMention, member.user.name + "#" + member.user.discriminator, member.user.id]

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
						val isSilent = it.reactionEmote.name == "\uD83D\uDE4A"

						message.delete().queue()

						val result = muteUser(context, member, time, locale, user, reason, isSilent)

						if (!result) {
							return@onReactionAddByAuthor
						}

						context.reply(
								LoriReply(
										locale["BAN_SuccessfullyPunished"],
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
				val time = it.message.contentDisplay.convertToEpochMillis()
				punishUser(time)
			}

			setHour.onReactionAddByAuthor(context) {
				if (it.reactionEmote.name == "\uD83D\uDD04") {
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
		// Para guardar as threads, a key deverá ser...
		// ID da guild#ID do usuário
		// Exemplo:
		// 297732013006389252#123170274651668480
		val roleRemovalJobs = mutableMapOf<String, Job>()

		suspend fun muteUser(context: CommandContext, member: Member, time: Long?, locale: LegacyBaseLocale, user: User, reason: String, isSilent: Boolean): Boolean {
			if (!isSilent) {
				if (context.config.moderationConfig.sendPunishmentViaDm && context.guild.isMember(user)) {
					try {
						val embed = AdminUtils.createPunishmentMessageSentViaDirectMessage(context.guild, locale, context.userHandle, locale["MUTE_PunishAction"], reason)

						user.openPrivateChannel().await().sendMessage(embed).queue()
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}

				if (context.config.moderationConfig.sendToPunishLog) {
					val textChannel = context.guild.getTextChannelById(context.config.moderationConfig.punishmentLogChannelId)

					if (textChannel != null && textChannel.canTalk()) {
						val message = MessageUtils.generateMessage(
								context.config.moderationConfig.punishmentLogMessage,
								listOf(user),
								context.guild,
								mutableMapOf(
										"reason" to reason,
										"punishment" to locale["MUTE_PunishAction"],
										"staff" to context.userHandle.name,
										"@staff" to context.userHandle.asMention,
										"staff-discriminator" to context.userHandle.discriminator,
										"staff-avatar-url" to context.userHandle.avatarUrl,
										"staff-id" to context.userHandle.id
								)
						)

						textChannel.sendMessage(message).queue()
					}
				}
			}

			val delay = if (time != null) {
				time - System.currentTimeMillis()
			} else {
				null
			}

			if (delay != null && 0 > delay) {
				// :whatdog:
				context.reply(
						LoriReply(
								locale["MUTE_NegativeTime"],
								Constants.ERROR
						)
				)
				return false
			}

			// Vamos pegar se a nossa role existe
			val mutedRoles = context.guild.getRolesByName(context.locale["MUTE_ROLE_NAME"], false)
			val mutedRole: Role?
			if (mutedRoles.isEmpty()) {
				// Se não existe, vamos criar ela!
				mutedRole = context.guild.controller.createRole()
						.setName(context.locale["MUTE_ROLE_NAME"])
						.setColor(Color.BLACK)
						.await()
			} else {
				// Se existe, vamos carregar a atual
				mutedRole = mutedRoles[0]
			}

			val couldntEditChannels = mutableListOf<TextChannel>()

			// E agora vamos pegar todos os canais de texto do servidor
			for (textChannel in context.guild.textChannels) {
				if (context.guild.selfMember.hasPermission(textChannel, Permission.MANAGE_CHANNEL)) {
					val permissionOverride = textChannel.getPermissionOverride(mutedRole)
					if (permissionOverride == null) { // Se é null...
						textChannel.createPermissionOverride(mutedRole)
								.setDeny(Permission.MESSAGE_WRITE) // kk eae men, daora ficar mutado né
								.queue()
					} else {
						if (permissionOverride.denied.contains(Permission.MESSAGE_WRITE)) {
							permissionOverride.manager
									.deny(Permission.MESSAGE_WRITE) // kk eae men, daora ficar mutado né
									.queue()
						}
					}
				} else {
					couldntEditChannels.add(textChannel)
				}
			}

			// E... finalmente... iremos dar (ou remover) a role para o carinha
			if (!context.guild.isMember(member.user)) {
				context.reply(
						LoriReply(
								context.locale["BAN_UserNotInThisServer"],
								Constants.ERROR
						)
				)
				return false
			}

			if (couldntEditChannels.isNotEmpty()) {
				context.reply(
						LoriReply(
								context.locale["MUTE_CouldntEditChannels", couldntEditChannels.joinToString(", ", transform = { "`" + it.name.stripCodeMarks() + "`" })],
								Constants.ERROR
						)
				)
			}

			try {
				val addRole = context.guild.controller.addSingleRoleToMember(member, mutedRole)

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
						Thread.sleep(250)
					}
					spawnRoleRemovalThread(context.guild, context.locale, user, time!!)
				}
			} catch (e: HierarchyException) {
				context.reply(
						LoriReply(
								context.locale["BAN_RoleTooLow"],
								Constants.ERROR
						)
				)
				return false
			}
			return true
		}

		fun getMutedRole(guild: Guild, locale: LegacyBaseLocale) = guild.getRolesByName(locale["MUTE_ROLE_NAME"], false).getOrNull(0)

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
			val mutedRole = getMutedRole(currentGuild, locale)

			if (System.currentTimeMillis() > expiresAt) {
				logger.info("Removendo cargo silenciado de $userId na guild ${guildId} - Motivo: Já expirou!")

				val guild = lorittaShards.getGuildById(guildId.toString())

				if (guild == null) {
					logger.warn("Bem... na verdade a guild $guildId não existe mais, então não iremos remover o estado de silenciado de $userId por enquanto...")
					return
				}

				val member = guild.getMemberById(userId)

				transaction(Databases.loritta) {
					Mutes.deleteWhere {
						(Mutes.guildId eq guildId) and (Mutes.userId eq userId)
					}
				}

				if (mutedRole != null && member != null) {
					val removeRole = guild.controller.removeSingleRoleFromMember(member, mutedRole)
					removeRole.queue()
				}
				return
			}

			val currentMember = currentGuild.getMemberById(userId)

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

					val member = guild.getMemberById(userId) ?: return@launch

					UnmuteCommand.unmute(
							loritta.getServerConfigForGuild(guild.id),
							guild,
							guild.selfMember.user,
							locale,
							member.user,
							locale.format("<:lori_owo:417813932380520448>") { locale.commands.moderation.unmute.automaticallyExpired },
							false
					)
				}
			}
		}
	}
}