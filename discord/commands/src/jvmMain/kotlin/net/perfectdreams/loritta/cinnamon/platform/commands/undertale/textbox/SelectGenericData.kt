package net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.platform.components.data.SingleUserComponentData

/**
 * Stored in the select menu, we don't store the data in [TextBoxOptionsData] here because it won't fit in the Custom ID! (due to the arbitrary text field)
 *
 * This is a "generic" class for stuff that we only need to get from the select menu value
 */
@Serializable
data class SelectGenericData(
    override val userId: Snowflake,
    val interactionDataId: Long
) : SingleUserComponentData