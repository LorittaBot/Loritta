package net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.textbox

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.textbox.characters.CharacterType
import net.perfectdreams.loritta.morenitta.interactions.vanilla.undertale.textbox.characters.UniverseType

/**
 * Text Box Options Data, stored in the database because we need to access the data within components, and it wouldn't fit in a Custom ID
 */
@Serializable
sealed class TextBoxOptionsData {
    abstract val text: String
    abstract val dialogBoxType: DialogBoxType
}

/**
 * A [TextBoxOptionsData] with a [universeType], used for no portrait and game portrait options
 */
@Serializable
sealed class TextBoxWithUniverseOptionsData : TextBoxOptionsData() {
    abstract val universeType: UniverseType
}

/**
 * A [TextBoxOptionsData] that doesn't have any portraits. The Universe Type is always [UniverseType.NONE].
 */
@Serializable
data class TextBoxWithNoPortraitOptionsData(
    override val text: String,
    override val dialogBoxType: DialogBoxType
) : TextBoxWithUniverseOptionsData() {
    override val universeType = UniverseType.NONE
}

/**
 * A [TextBoxWithGamePortraitOptionsData] that uses Undertale/DELTARUNE/etc character portraits.
 */
@Serializable
data class TextBoxWithGamePortraitOptionsData(
    override val text: String,
    override val dialogBoxType: DialogBoxType,
    override val universeType: UniverseType,
    val character: CharacterType,
    val portrait: String
) : TextBoxWithUniverseOptionsData() {
    init {
        require(universeType != UniverseType.NONE) { "You can't use a TextBoxWithGamePortraitOptionsData with a UniverseType.NONE universe!" }
    }
}

/**
 * A [TextBoxWithGamePortraitOptionsData] that uses a custom image URL for the portrait.
 */
@Serializable
data class TextBoxWithCustomPortraitOptionsData(
    override val text: String,
    override val dialogBoxType: DialogBoxType,
    val imageUrl: String,
    val colorPortraitType: ColorPortraitType
) : TextBoxOptionsData()