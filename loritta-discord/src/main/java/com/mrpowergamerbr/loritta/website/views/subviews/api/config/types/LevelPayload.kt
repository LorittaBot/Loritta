package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.dao.LevelConfig
import net.perfectdreams.loritta.tables.ExperienceRoleRates
import net.perfectdreams.loritta.tables.LevelAnnouncementConfigs
import net.perfectdreams.loritta.tables.RolesByExperience
import net.perfectdreams.loritta.utils.levels.LevelUpAnnouncementType
import net.perfectdreams.loritta.utils.levels.RoleGiveType
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class LevelPayload : ConfigPayloadType("level") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, guild: Guild) {
		transaction(Databases.loritta) {
			val levelConfig = serverConfig.levelConfig ?: LevelConfig.new {
				this.roleGiveType = RoleGiveType.STACK
				this.noXpChannels = arrayOf()
				this.noXpRoles = arrayOf()
			}

			// Main
			levelConfig.roleGiveType = RoleGiveType.valueOf(payload["roleGiveType"].string)
			levelConfig.noXpChannels = payload["noXpChannels"].array.map { it.long }.toTypedArray()
			levelConfig.noXpRoles = payload["noXpRoles"].array.map { it.long }.toTypedArray()

			// Announcements
			// Deletar todas que já existem
			LevelAnnouncementConfigs.deleteWhere {
				LevelAnnouncementConfigs.levelConfig eq levelConfig.id
			}

			val announcements = payload["announcements"].array

			for (announcement in announcements.map { it.obj }) {
				val type = announcement["type"].string
				val channelId = announcement["channelId"].nullLong
				val message = announcement["message"].string
				val onlyIfUserReceivedRoles = announcement["onlyIfUserReceivedRoles"].bool

				LevelAnnouncementConfigs.insert {
					it[LevelAnnouncementConfigs.levelConfig] = levelConfig.id
					it[LevelAnnouncementConfigs.type] = LevelUpAnnouncementType.valueOf(type)
					it[LevelAnnouncementConfigs.channelId] = channelId
					it[LevelAnnouncementConfigs.onlyIfUserReceivedRoles] = onlyIfUserReceivedRoles
					it[LevelAnnouncementConfigs.message] = message
				}
			}

			// Cargos por experiência
			val rolesByExperience = payload["rolesByExperience"].array
			// Deletar todas que já existem
			RolesByExperience.deleteWhere {
				RolesByExperience.guildId eq serverConfig.guildId
			}

			for (roleByExperience in rolesByExperience.map { it.obj }) {
				val requiredExperience = roleByExperience["requiredExperience"].long
				val roles = roleByExperience["roles"].array.map { it.long }.toTypedArray()

				RolesByExperience.insert {
					it[RolesByExperience.guildId] = serverConfig.guildId
					it[RolesByExperience.requiredExperience] = Math.max(Math.min(10000000, requiredExperience), 0)
					it[RolesByExperience.roles] = roles
				}
			}

			// Rates personalizados por experiência
			val experienceRoleRates = payload["experienceRoleRates"].array
			// Deletar todas que já existem
			ExperienceRoleRates.deleteWhere {
				ExperienceRoleRates.guildId eq serverConfig.guildId
			}

			for (experienceRoleRate in experienceRoleRates.map { it.obj }) {
				val rate = experienceRoleRate["rate"].double
				val roleId = experienceRoleRate["role"].long

				ExperienceRoleRates.insert {
					it[ExperienceRoleRates.guildId] = serverConfig.guildId
					it[ExperienceRoleRates.role] = roleId
					it[ExperienceRoleRates.rate] = Math.max(Math.min(100.0, rate), 0.0)
				}
			}


			serverConfig.levelConfig = levelConfig // Yay!!
		}
	}
}