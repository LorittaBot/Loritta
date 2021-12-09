package net.perfectdreams.loritta.cinnamon.platform.commands.social.marriage

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.platform.components.data.SingleUserComponentData

@Serializable
data class ProposalData(
    override val userId: Snowflake,
    val user2: Snowflake
) : SingleUserComponentData