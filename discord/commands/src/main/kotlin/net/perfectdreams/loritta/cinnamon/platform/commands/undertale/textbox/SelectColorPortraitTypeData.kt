package net.perfectdreams.loritta.cinnamon.platform.commands.undertale.textbox

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.platform.components.data.SingleUserComponentData

/**
 * Stored in the buttons menu, we don't store the data in [TextBoxOptionsData] here because it won't fit in the Custom ID! (due to the arbitrary text field)
 */
@Serializable
data class SelectColorPortraitTypeData(
    override val userId: Snowflake,
    val type: ColorPortraitType,
    val interactionDataId: Long
) : SingleUserComponentData