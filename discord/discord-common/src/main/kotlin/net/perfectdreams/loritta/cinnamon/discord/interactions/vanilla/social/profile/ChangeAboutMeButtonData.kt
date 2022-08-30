package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.profile

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.SingleUserComponentData

@Serializable
data class ChangeAboutMeButtonData(
    override val userId: Snowflake,
    val currentAboutMe: String
) : SingleUserComponentData