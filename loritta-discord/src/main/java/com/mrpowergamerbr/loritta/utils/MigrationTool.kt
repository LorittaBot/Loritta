package com.mrpowergamerbr.loritta.utils

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralDiscordConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralDiscordInstanceConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralInstanceConfig
import net.perfectdreams.loritta.dao.servers.moduleconfigs.*
import net.perfectdreams.loritta.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.tables.servers.ServerRolePermissions
import net.perfectdreams.loritta.tables.servers.moduleconfigs.WarnActions
import net.perfectdreams.loritta.utils.CustomCommandCodeType
import net.perfectdreams.loritta.utils.PunishmentAction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class MigrationTool(val discordConfig: GeneralDiscordConfig, val discordInstanceConfig: GeneralDiscordInstanceConfig, val config: GeneralConfig, val instanceConfig: GeneralInstanceConfig) {
	companion object {
	}

	fun mongo() {
		val loritta = Loritta(discordConfig, discordInstanceConfig, config, instanceConfig)

		loritta.initPostgreSql()
		loritta.initMongo()

		println("Starting MongoDB -> PostgreSQL migration...")

		val count = loritta.serversColl.countDocuments()
		val iter = loritta.serversColl.find().iterator()

		var idx = 0
		while (iter.hasNext()) {
			val mongoConfig = iter.next()
			val guildId = mongoConfig.guildId.toLongOrNull()
			if (guildId != null && guildId != -1L) {
				val config = transaction(Databases.loritta) {
					ServerConfig.findById(mongoConfig.guildId.toLong())
				}

				if (idx % 100 == 0) {
					println("$idx/$count done")
				}

				idx++
				if (config == null) {
					println("ID ${guildId} exists in MongoDB, but doesn't exist in PostgreSQL!")
					continue
				}

				// ===[ DISABLED COMMANDS ]===
				if (mongoConfig.disabledCommands.isNotEmpty())
					transaction(Databases.loritta) {
						config.disabledCommands = mongoConfig.disabledCommands.toTypedArray()
					}

				// ===[ CUSTOM COMMANDS ]===
				for (command in mongoConfig.nashornCommands) {
					transaction(Databases.loritta) {
						CustomGuildCommands.insert {
							it[CustomGuildCommands.guild] = config.id
							it[CustomGuildCommands.label] = command.jsLabel
							it[CustomGuildCommands.codeType] = CustomCommandCodeType.JAVASCRIPT
							it[CustomGuildCommands.enabled] = true
							it[CustomGuildCommands.code] = command.javaScript
						}
					}
				}

				// ===[ WELCOMER ]===
				val welcomerConfig = mongoConfig.joinLeaveConfig

				if (welcomerConfig.isEnabled && (welcomerConfig.tellOnBan || welcomerConfig.tellOnJoin || welcomerConfig.tellOnLeave || welcomerConfig.tellOnPrivate)) {
					transaction(Databases.loritta) {
						config.welcomerConfig = WelcomerConfig.new {
							this.tellOnJoin = welcomerConfig.tellOnJoin
							this.tellOnRemove = welcomerConfig.tellOnLeave
							this.joinMessage = welcomerConfig.joinMessage
							this.removeMessage = welcomerConfig.leaveMessage
							this.channelJoinId = welcomerConfig.canalJoinId?.toLongOrNull()
							this.channelRemoveId = welcomerConfig.canalLeaveId?.toLongOrNull()
							this.tellOnPrivateJoin = welcomerConfig.tellOnPrivate
							this.joinPrivateMessage = welcomerConfig.joinPrivateMessage
							this.tellOnBan = welcomerConfig.tellOnBan
							this.bannedMessage = welcomerConfig.banMessage
							this.deleteJoinMessagesAfter = welcomerConfig.deleteJoinMessagesAfter
							this.deleteRemoveMessagesAfter = welcomerConfig.deleteLeaveMessagesAfter
						}
					}
				}

				// ===[ STARBOARD ]===
				val starboardConfig = mongoConfig.starboardConfig

				if (starboardConfig.isEnabled) {
					val id = starboardConfig.starboardId?.toLongOrNull()

					if (id != null) {
						transaction(Databases.loritta) {
							config.starboardConfig = StarboardConfig.new {
								this.enabled = starboardConfig.isEnabled
								this.requiredStars = starboardConfig.requiredStars
								this.starboardChannelId = id
							}
						}
					} else {
						println("Starboard Channel ID $id in $guildId is invalid!")
					}
				}

				// ===[ EVENT LOG ]===
				val eventLogConfig = mongoConfig.eventLogConfig

				if (eventLogConfig.isEnabled) {
					val id = eventLogConfig.eventLogChannelId?.toLongOrNull()

					if (id != null) {
						transaction(Databases.loritta) {
							config.eventLogConfig = EventLogConfig.new {
								this.enabled = eventLogConfig.isEnabled
								this.avatarChanges = eventLogConfig.avatarChanges
								this.memberBanned = eventLogConfig.memberBanned
								this.memberUnbanned = eventLogConfig.memberUnbanned
								this.messageDeleted = eventLogConfig.messageDeleted
								this.messageEdited = eventLogConfig.messageEdit
								this.nicknameChanges = eventLogConfig.nicknameChanges
								this.voiceChannelJoins = eventLogConfig.voiceChannelJoins
								this.voiceChannelLeaves = eventLogConfig.voiceChannelLeaves
								this.eventLogChannelId = id
							}
						}
					} else {
						println("Event Log Channel ID $id in $guildId is invalid!")
					}
				}

				// ===[ AUTOROLE ]===
				val autoroleConfig = mongoConfig.autoroleConfig

				if (autoroleConfig.isEnabled) {
					val roles = autoroleConfig.roles.mapNotNull { it.toLongOrNull() }

					if (roles.isNotEmpty()) {
						transaction(Databases.loritta) {
							config.autoroleConfig = AutoroleConfig.new {
								this.enabled = autoroleConfig.isEnabled
								this.roles = roles.toTypedArray()
								this.giveOnlyAfterMessageWasSent = autoroleConfig.giveOnlyAfterMessageWasSent
								this.giveRolesAfter = autoroleConfig.giveRolesAfter
							}
						}
					} else {
						println("Autorole role list in $guildId is empty!")
					}
				}

				// ===[ INVITE BLOCKER ]===
				val inviteBlockerConfig = mongoConfig.inviteBlockerConfig

				if (inviteBlockerConfig.isEnabled) {
					transaction(Databases.loritta) {
						config.inviteBlockerConfig = InviteBlockerConfig.new {
							this.enabled = inviteBlockerConfig.isEnabled
							this.deleteMessage = inviteBlockerConfig.deleteMessage
							this.tellUser = inviteBlockerConfig.tellUser
							this.warnMessage = inviteBlockerConfig.warnMessage
							this.whitelistServerInvites = inviteBlockerConfig.whitelistServerInvites
							this.whitelistedChannels = inviteBlockerConfig.whitelistedChannels.mapNotNull { it.toLongOrNull() }.toTypedArray()
						}
					}
				}

				// ===[ PERMISSIONS ]===
				val roles = mongoConfig.permissionsConfig

				for ((roleIdAsString, permissions) in roles.roles) {
					val roleId = roleIdAsString.toLongOrNull()

					if (roleId != null) {
						for (permission in permissions.permissions) {
							transaction(Databases.loritta) {
								ServerRolePermissions.insert {
									it[ServerRolePermissions.guild] = config.id
									it[ServerRolePermissions.roleId] = roleId
									it[ServerRolePermissions.permission] = permission
								}
							}
						}
					} else {
						println("Role ID $roleId is invalid!")
					}
				}

				// ===[ MODERATION ]===
				val moderationConfig = mongoConfig.moderationConfig

				transaction(Databases.loritta) {
					val newConfig = ModerationConfig.new {
						this.sendPunishmentViaDm = moderationConfig.sendPunishmentViaDm
						this.sendPunishmentToPunishLog = moderationConfig.sendToPunishLog
						this.punishLogChannelId = moderationConfig.punishmentLogChannelId?.toLongOrNull()
						this.punishLogMessage = moderationConfig.punishmentLogMessage
					}

					config.moderationConfig = newConfig

					for (punishmentAction in moderationConfig.punishmentActions) {
						WarnActions.insert {
							it[WarnActions.config] = newConfig.id
							it[WarnActions.warnCount] = punishmentAction.warnCount
							it[WarnActions.punishmentAction] = PunishmentAction.valueOf(punishmentAction.punishmentAction.name)
							val customMetadata = punishmentAction.customMetadata0

							if (customMetadata != null) {
								it[WarnActions.metadata] = jsonObject(
										"time" to customMetadata
								)
							}
						}
					}
				}

				// ===[ MISCELLANEOUS ]===
				val miscellaneousConfig = mongoConfig.miscellaneousConfig

				if (miscellaneousConfig.enableBomDiaECia || miscellaneousConfig.enableQuirky) {
					transaction(Databases.loritta) {
						config.miscellaneousConfig = MiscellaneousConfig.new {
							this.enableBomDiaECia = miscellaneousConfig.enableBomDiaECia
							this.enableQuirky = miscellaneousConfig.enableQuirky
						}
					}
				}

				// ===[ MEMBER COUNTER ]===
				val textChannelConfigs = mongoConfig.textChannelConfigs

				for (textChannelConfig in textChannelConfigs) {
					val memberCounterConfig = textChannelConfig.memberCounterConfig
					if (memberCounterConfig != null) {
						val id = textChannelConfig.id?.toLongOrNull()

						if (id != null) {
							transaction(Databases.loritta) {
								MemberCounterChannelConfig.new {
									this.guild = config.id
									this.channelId = id
									this.padding = memberCounterConfig.padding
									this.theme = memberCounterConfig.theme
									this.topic = memberCounterConfig.topic
								}
							}
						} else {
							println("Member Counter ${textChannelConfig.id} ID is invalid!")
						}
					}
				}
			} else {
				println("ID $guildId is invalid!")
			}
		}
	}
}