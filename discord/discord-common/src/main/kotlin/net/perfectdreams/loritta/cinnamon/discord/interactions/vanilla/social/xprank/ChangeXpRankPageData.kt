package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.xprank

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.SingleUserComponentData

@Serializable
data class ChangeXpRankPageData(
    override val userId: Snowflake,
    val button: Button,
    val page: Long
) : SingleUserComponentData {
    enum class Button {
        LEFT_ARROW,
        RIGHT_ARROW
    }
}