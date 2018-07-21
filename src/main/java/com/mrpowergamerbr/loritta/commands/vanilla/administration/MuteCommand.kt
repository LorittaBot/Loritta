package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaGuildUserData
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.exceptions.HierarchyException
import java.awt.Color
import java.time.Instant
import kotlin.concurrent.thread

class MuteCommand : AbstractCommand("mute", listOf("mutar", "silenciar"), CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["MUTE_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return listOf("159985870458322944", "159985870458322944 Algum motivo bastante aleatório");
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

	override fun run(context: CommandContext, locale: BaseLocale) {
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

			fun punishUser(time: Long?) {
				var rawArgs = context.rawArgs
				rawArgs = rawArgs.remove(0) // remove o usuário

				val reason = rawArgs.joinToString(" ")

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
						var isSilent = it.reactionEmote.name == "\uD83D\uDE4A"

						message.delete().complete()

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

				message.addReaction("✅").complete()
				if (hasSilent) {
					message.addReaction("\uD83D\uDE4A").complete()
				}
			}

			setHour.onResponseByAuthor(context) {
				setHour.delete().complete()
				val time = it.message.contentDisplay.convertToEpochMillis()
				punishUser(time)
			}

			setHour.onReactionAddByAuthor(context) {
				if (it.reactionEmote.name == "\uD83D\uDD04") {
					setHour.delete().complete()
					punishUser(null)
				}
			}

			setHour.addReaction("\uD83D\uDD04").complete()
		} else {
			this.explain(context);
		}
	}

	companion object {
		// Para guardar as threads, a key deverá ser...
		// ID da guild#ID do usuário
		// Exemplo:
		// 297732013006389252#123170274651668480
		val roleRemovalThreads = mutableMapOf<String, Thread>()

		fun muteUser(context: CommandContext, member: Member, time: Long?, locale: BaseLocale, user: User, reason: String, isSilent: Boolean): Boolean {
			if (!isSilent) {
				if (context.config.moderationConfig.sendPunishmentViaDm && context.guild.isMember(user)) {
					try {
						val embed = EmbedBuilder()

						embed.setTimestamp(Instant.now())
						embed.setColor(Color(221, 0, 0))

						embed.setThumbnail(context.guild.iconUrl)
						embed.setAuthor(context.userHandle.name + "#" + context.userHandle.discriminator, null, context.userHandle.avatarUrl)
						embed.setTitle("\uD83D\uDEAB ${locale["BAN_YouAreBanned", locale["MUTE_PunishAction"].toLowerCase(), context.guild.name]}!")
						embed.addField("\uD83D\uDC6E ${locale["BAN_PunishedBy"]}", context.userHandle.name + "#" + context.userHandle.discriminator, false)
						embed.addField("\uD83D\uDCDD ${locale["BAN_PunishmentReason"]}", reason, false)

						user.openPrivateChannel().complete().sendMessage(embed.build()).complete()
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

						textChannel.sendMessage(message).complete()
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
			var mutedRoles = context.guild.getRolesByName(context.locale["MUTE_ROLE_NAME"], false)
			var mutedRole: Role?
			if (mutedRoles.isEmpty()) {
				// Se não existe, vamos criar ela!
				mutedRole = context.guild.controller.createRole()
						.setName(context.locale["MUTE_ROLE_NAME"])
						.setColor(Color.BLACK)
						.complete()
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
								.complete()
					} else {
						if (permissionOverride.denied.contains(Permission.MESSAGE_WRITE)) {
							permissionOverride.manager
									.deny(Permission.MESSAGE_WRITE) // kk eae men, daora ficar mutado né
									.complete()
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

				addRole.complete()

				val serverConfig = loritta.getServerConfigForGuild(context.guild.id)
				val userData = serverConfig.getUserData(member.user.id)

				userData.isMuted = true
				if (time != null) {
					userData.temporaryMute = true
					userData.expiresIn = time
				} else {
					userData.temporaryMute = false
				}

				loritta save serverConfig
				if (delay != null) {
					// Ao enviar um role change, iremos esperar alguns segundos para ver se o mute foi realmente "aplicado"
					for (x in 0..9) {
						if (member.roles.contains(mutedRole))
							break
						Thread.sleep(100)
					}
					spawnRoleRemovalThread(context.guild, context.locale, serverConfig, userData)
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

		fun spawnRoleRemovalThread(guild: Guild, locale: BaseLocale, serverConfig: ServerConfig, userData: LorittaGuildUserData) {
			logger.info("Criando role removal thread para usuário ${userData.userId} na guild ${guild.id}!")

			val previousThread = roleRemovalThreads["${guild.id}#${userData.userId}"]
			if (previousThread != null) {
				roleRemovalThreads.remove("${guild.id}#${userData.userId}")
				logger.info("Interrompendo thread de ${userData.userId} na guild ${guild.id}! Criar outra removal thread enquanto uma já está ativa é feio!")
				previousThread.interrupt() // lol nope
			}

			// Vamos pegar se a nossa role existe
			var mutedRoles = guild.getRolesByName(locale["MUTE_ROLE_NAME"], false)
			val mutedRole = mutedRoles.getOrNull(0)

			val member = guild.getMemberById(userData.userId)

			val time = userData.expiresIn
			val delay = time - System.currentTimeMillis()
			if (0 > delay) {
				logger.info("Removendo cargo silenciado de ${userData.userId} na guild ${guild.id} - Motivo: Já expirou!")

				// Tempo menor que 0 = já expirou!
				userData.temporaryMute = false
				userData.isMuted = false
				userData.expiresIn = 0

				loritta save serverConfig

				if (mutedRole != null && member != null) {
					val removeRole = guild.controller.removeSingleRoleFromMember(member, mutedRole)

					removeRole.complete()
				}
				return
			}

			if (member == null) {
				logger.info("Ignorando role removal de ${userData.userId} - Motivo: Ela não está mais no servidor!")
				return
			}

			if (mutedRole == null || !member.roles.contains(mutedRole)) {
				if (mutedRole == null) {
					logger.info("Removendo status de silenciado de ${userData.userId} na guild ${guild.id} - Motivo: Cargo não existe mais!")
				} else {
					logger.info("Removendo status de silenciado de ${userData.userId} na guild ${guild.id} - Motivo: Usuário não possui mais o cargo!")
				}
				// Se não existe, então quer dizer que o cargo foi deletado e isto deve ser ignorado!
				userData.temporaryMute = false
				userData.isMuted = false
				userData.expiresIn = 0

				loritta save serverConfig
			} else {
				// Se existe, vamos carregar a atual
				roleRemovalThreads.put("${guild.id}#${userData.userId}",
						thread {
							logger.info("Criado role removal thread de ${member.user.id} na guild ${guild.id}, irá expirar em ${time}")
							try {
								Thread.sleep(delay)

								logger.info("Removendo cargo silenciado de ${member.user.id} na guild ${guild.id}")

								val serverConfig = loritta.getServerConfigForGuild(serverConfig.guildId)
								val userData = serverConfig.getUserData(userData.userId)
								userData.temporaryMute = false
								userData.isMuted = false
								userData.expiresIn = 0

								loritta save serverConfig

								val removeRole = guild.controller.removeSingleRoleFromMember(member, mutedRole)

								removeRole.complete()
							} catch (e: InterruptedException) {
								logger.info("Role removal thread de ${member.user.id} na guild ${guild.id} foi interrompida!")
							}
						}
				)
			}
		}
	}
}