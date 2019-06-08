package net.perfectdreams.loritta.socket.network.commands

import com.fasterxml.jackson.databind.JsonNode
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.socket.SocketWrapper
import net.perfectdreams.loritta.socket.network.SocketOpCode
import net.perfectdreams.loritta.socket.network.commands.config.get.GetDonationConfigCommand
import net.perfectdreams.loritta.socket.network.commands.config.get.GetGeneralConfigCommand
import net.perfectdreams.loritta.utils.extensions.objectNode
import net.perfectdreams.loritta.utils.extensions.textValueOrNull

class GetGuildConfigByIdCommand : SocketCommand(SocketOpCode.Discord.GET_GUILD_CONFIG_BY_ID) {
    val commands = mutableListOf(
            GetGeneralConfigCommand(),
            GetDonationConfigCommand()
    )

    override suspend fun process(socketWrapper: SocketWrapper, payload: JsonNode): JsonNode {
        val guildId = payload["guildId"].textValue()
        val userId = payload["userId"].textValueOrNull()
        val sections = payload["sections"]

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

        val config = objectNode()
        val serverConfig by lazy {
            loritta.getServerConfigForGuild(guildId)
        }

        sections.forEach {
            val type = it.textValue()

            commands.firstOrNull { it.sectionName == type }?.let {
                config[it.sectionName] = it.process(guildId.toLong(), serverConfig)
            } ?: logger.warn("Unknown section with ID $type")
        }

        return config
    }
}