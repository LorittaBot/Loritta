package net.perfectdreams.loritta.website.utils.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.dao.servers.moduleconfigs.AutoroleConfig

object AutoroleConfigTransformer : ConfigTransformer {
    override val payloadType: String = "autorole"
    override val configKey: String = "autoroleConfig"

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        val autoroleConfig = loritta.newSuspendedTransaction {
            serverConfig.autoroleConfig
        }

        return jsonObject(
                "enabled" to (autoroleConfig?.enabled ?: false),
                "roles" to (autoroleConfig?.roles ?: arrayOf()).toList().toJsonArray(),
                "giveRolesAfter" to (autoroleConfig?.giveRolesAfter),
                "giveOnlyAfterMessageWasSent" to (autoroleConfig?.giveOnlyAfterMessageWasSent ?: false)
        )
    }

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        loritta.newSuspendedTransaction {
            val autoroleConfig = serverConfig.autoroleConfig ?: AutoroleConfig.new {
                this.enabled = false
                this.roles = arrayOf()
                this.giveRolesAfter = null
                this.giveOnlyAfterMessageWasSent = false
            }

            autoroleConfig.enabled = payload["enabled"].bool
            autoroleConfig.roles = payload["roles"].array.map { it.long }.toTypedArray()
            autoroleConfig.giveRolesAfter = payload["giveRolesAfter"].nullLong
            autoroleConfig.giveOnlyAfterMessageWasSent = payload["giveOnlyAfterMessageWasSent"].bool

            serverConfig.autoroleConfig = autoroleConfig
        }
    }
}