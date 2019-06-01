package net.perfectdreams.loritta.socket.network.commands

import com.fasterxml.jackson.databind.JsonNode
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.socket.SocketWrapper
import net.perfectdreams.loritta.socket.network.SocketOpCode
import net.perfectdreams.loritta.socket.network.commands.config.patch.PatchGeneralConfigCommand
import net.perfectdreams.loritta.utils.extensions.objectNode
import net.perfectdreams.loritta.utils.extensions.textValueOrNull

class UpdateGuildConfigByIdCommand : SocketCommand(SocketOpCode.Discord.UPDATE_GUILD_CONFIG_BY_ID) {
    val commands = mutableListOf(
            PatchGeneralConfigCommand()
    )

    override suspend fun process(socketWrapper: SocketWrapper, payload: JsonNode): JsonNode {
        val patchCode = payload["patchCode"].intValue()
        val guildId = payload["guildId"].textValue()
        val userId = payload["userId"].textValueOrNull()

        val guild = lorittaShards.getGuildById(guildId) ?: return objectNode(
                "status" to "UnknownGuild"
        )

        if (userId != null) {
            val member = guild.getMemberById(userId) ?: return objectNode(
                    "status" to "UserNotInGuild"
            )


            if (!member.hasPermission(Permission.ADMINISTRATOR) && !member.hasPermission(Permission.MANAGE_SERVER)) {
                return objectNode(
                        "status" to "MissingPerms"
                )
            }
        }

        val serverConfig: MongoServerConfig by lazy {
            loritta.getServerConfigForGuild(guildId)
        }

        commands.forEach {
            if (payload.has(it.sectionName)) {
                val section = payload[it.sectionName]

                it.process(serverConfig, section)
            }
        }

        return objectNode()
    }
}