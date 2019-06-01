package net.perfectdreams.loritta.platform.network.discord.entities

import com.fasterxml.jackson.databind.node.ObjectNode
import net.perfectdreams.loritta.api.entities.Guild
import net.perfectdreams.loritta.api.entities.Member
import net.perfectdreams.loritta.api.entities.MessageChannel
import net.perfectdreams.loritta.platform.discord.entities.DiscordGuild
import net.perfectdreams.loritta.utils.extensions.objectNode
import net.perfectdreams.loritta.utils.extensions.textValueOrNull

class DiscordNetworkGuild constructor(
    override val id: Long,
    override val name: String,
    override val members: List<Member>,
    override val icon: String?,
    override val messageChannels: List<MessageChannel>
) : DiscordGuild {
    companion object {
        fun from(node: ObjectNode) = DiscordNetworkGuild(
            node["id"].asLong(),
            node["name"].textValue(),
            listOf(), // TODO
            node["icon"].textValueOrNull(),
            listOf() // TODO
        )

        fun toObjectNode(guild: Guild): ObjectNode {
            return objectNode(
                    "id" to guild.id.toString(),
                    "name" to guild.name,
                    "members" to guild.members,
                    "messageChannels" to guild.messageChannels
            )
        }
    }
}