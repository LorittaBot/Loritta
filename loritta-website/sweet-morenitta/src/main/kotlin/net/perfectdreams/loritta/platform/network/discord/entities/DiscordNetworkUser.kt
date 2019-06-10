package net.perfectdreams.loritta.platform.network.discord.entities

import com.fasterxml.jackson.databind.node.ObjectNode
import net.perfectdreams.loritta.platform.discord.entities.DiscordUser
import net.perfectdreams.loritta.utils.extensions.objectNode
import net.perfectdreams.loritta.utils.extensions.textValueOrNull

class DiscordNetworkUser constructor(
        override val id: Long,
        override val name: String,
        override val discriminator: String,
        override val avatar: String?,
        override val isBot: Boolean
) : DiscordUser {
    companion object {
        fun from(node: ObjectNode) = DiscordNetworkUser(
                node["id"].asLong(),
                node["name"].textValue(),
                node["discriminator"].textValue(),
                node["avatar"].textValueOrNull(),
                node["isBot"].booleanValue()
        )

        fun toObjectNode(user: DiscordUser): ObjectNode {
            return objectNode(
                    "id" to user.id.toString(),
                    "name" to user.name,
                    "discriminator" to user.discriminator,
                    "avatar" to user.avatar,
                    "isBot" to user.isBot
            )
        }
    }

    override val asMention: String
        get() = "<@${id}>"

    override val avatarUrl: String?
        get() {
            return avatar?.let {
                val ext = if (avatar.startsWith("a_"))
                    "gif"
                else
                    "png"
                "https://cdn.discordapp.com/avatars/$id/$avatar.$ext"
            }
        }

    override val effectiveAvatarUrl: String
        get() = avatarUrl ?: "https://cdn.discordapp.com/emojis/523176710439567392.png?v=1"

    override val defaultAvatarUrl: String
        get() = "https://cdn.discordapp.com/emojis/523176710439567392.png?v=1"
}