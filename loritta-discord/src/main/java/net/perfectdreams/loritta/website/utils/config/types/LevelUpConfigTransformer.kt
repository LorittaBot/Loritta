package net.perfectdreams.loritta.website.utils.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.dao.servers.moduleconfigs.LevelConfig
import net.perfectdreams.loritta.tables.servers.moduleconfigs.ExperienceRoleRates
import net.perfectdreams.loritta.tables.servers.moduleconfigs.LevelAnnouncementConfigs
import net.perfectdreams.loritta.tables.servers.moduleconfigs.RolesByExperience
import net.perfectdreams.loritta.utils.levels.LevelUpAnnouncementType
import net.perfectdreams.loritta.utils.levels.RoleGiveType
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object LevelUpConfigTransformer : ConfigTransformer {
    override val payloadType: String = "level"
    override val configKey: String = "levelUpConfig"

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        loritta.newSuspendedTransaction {
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

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        return loritta.newSuspendedTransaction {
            val levelConfig = serverConfig.levelConfig
            val announcements = LevelAnnouncementConfigs.select {
                LevelAnnouncementConfigs.levelConfig eq (levelConfig?.id?.value ?: -1L)
            }

            val announcementArray = jsonArray()
            for (announcement in announcements) {
                announcementArray.add(
                        jsonObject(
                                "type" to announcement[LevelAnnouncementConfigs.type].toString(),
                                "channelId" to announcement[LevelAnnouncementConfigs.channelId]?.toString(),
                                "onlyIfUserReceivedRoles" to announcement[LevelAnnouncementConfigs.onlyIfUserReceivedRoles],
                                "message" to announcement[LevelAnnouncementConfigs.message].toString()
                        )
                )
            }

            val rolesByExperience = RolesByExperience.select {
                RolesByExperience.guildId eq guild.idLong
            }
            val rolesByExperienceArray = jsonArray()
            for (roleByExperience in rolesByExperience) {
                rolesByExperienceArray.add(
                        jsonObject(
                                "requiredExperience" to roleByExperience[RolesByExperience.requiredExperience].toString(),
                                "roles" to roleByExperience[RolesByExperience.roles].map { it.toString() }.toList().toJsonArray()
                        )
                )
            }

            val experienceRoleRates = ExperienceRoleRates.select {
                ExperienceRoleRates.guildId eq guild.idLong
            }
            val experienceRoleRatesArray = jsonArray()
            for (experienceRoleRate in experienceRoleRates) {
                experienceRoleRatesArray.add(
                        jsonObject(
                                "role" to experienceRoleRate[ExperienceRoleRates.role].toString(),
                                "rate" to experienceRoleRate[ExperienceRoleRates.rate].toDouble()
                        )
                )
            }

            jsonObject(
                    "roleGiveType" to (levelConfig?.roleGiveType ?: RoleGiveType.STACK).toString(),
                    "noXpChannels" to (levelConfig?.noXpChannels?.toList()?.toJsonArray() ?: jsonArray()),
                    "noXpRoles" to (levelConfig?.noXpRoles?.toList()?.toJsonArray() ?: jsonArray()),
                    "announcements" to announcementArray,
                    "rolesByExperience" to rolesByExperienceArray,
                    "experienceRoleRates" to experienceRoleRatesArray
            )
        }
    }
}