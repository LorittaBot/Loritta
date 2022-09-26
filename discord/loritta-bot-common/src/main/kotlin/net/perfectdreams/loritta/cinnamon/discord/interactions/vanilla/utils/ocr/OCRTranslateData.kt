package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.ocr

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.data.SingleUserComponentData
import net.perfectdreams.loritta.cinnamon.discord.utils.google.Language

@Serializable
data class OCRTranslateData(
    override val userId: Snowflake,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val isEphemeral: Boolean,
    val text: String
) : SingleUserComponentData