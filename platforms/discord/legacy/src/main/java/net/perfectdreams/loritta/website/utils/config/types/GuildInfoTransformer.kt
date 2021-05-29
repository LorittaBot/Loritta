package net.perfectdreams.loritta.website.utils.config.types

import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.dao.ServerConfig
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession

object GuildInfoTransformer : ConfigTransformer {
    override val payloadType: String = "guildinfo"
    override val configKey: String = "guildInfo"

    override suspend fun toJson(userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig): JsonElement {
        return jsonObject(
                "id" to guild.idLong,
                "name" to guild.name,
                "iconUrl" to guild.iconUrl
        )
    }
}