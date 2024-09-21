package net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`.text

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.utils.text.VaporwaveUtils
import java.util.UUID
import kotlin.random.Random

abstract class SimpleTextTransformSubcommand(
    val uniqueId: UUID,
    val label: StringI18nData,
    val description: StringI18nData,
    val textOption: StringI18nData,
) {
    abstract fun transform(text: String): String
}

class TextTransformVaporwaveSubcommand : SimpleTextTransformSubcommand(
    uniqueId = UUID.fromString("bed4c9cb-f5f8-485d-9bde-f585a137a9ff"),
    label = I18N_PREFIX.Label,
    description = I18N_PREFIX.Description,
    textOption = I18N_PREFIX.Options.Text
) {
    override fun transform(text: String): String =
        VaporwaveUtils.vaporwave(text)

    companion object {
        val I18N_PREFIX = TextTransformCommand.I18N_PREFIX.Vaporwave
    }
}

class TextTransformUppercaseSubcommand : SimpleTextTransformSubcommand(
    uniqueId = UUID.fromString("cae46a7a-d689-4ba1-8f5e-f93e20e2809b"),
    label = I18N_PREFIX.Label,
    description = I18N_PREFIX.Description,
    textOption = I18N_PREFIX.Options.Text
) {
    override fun transform(text: String): String =
        text.uppercase()

    companion object {
        val I18N_PREFIX = TextTransformCommand.I18N_PREFIX.Uppercase
    }
}

class TextTransformLowercaseSubcommand : SimpleTextTransformSubcommand(
    uniqueId = UUID.fromString("08caa41e-d7a1-4a47-9c03-47c9207f8f8f"),
    label = I18N_PREFIX.Label,
    description = I18N_PREFIX.Description,
    textOption = I18N_PREFIX.Options.Text
) {
    override fun transform(text: String): String =
        text.lowercase()

    companion object {
        val I18N_PREFIX = TextTransformCommand.I18N_PREFIX.Lowercase
    }
}

class TextTransformMockSubcommand : SimpleTextTransformSubcommand(
    uniqueId = UUID.fromString("1cfb2ce2-bbaf-47bd-b332-b0f888ae4c9a"),
    label = I18N_PREFIX.Label,
    description = I18N_PREFIX.Description,
    textOption = I18N_PREFIX.Options.Text
) {
    override fun transform(text: String): String {
        val random = Random(text.hashCode())

        return text.mapIndexed { index, c -> if (random.nextBoolean()) c.uppercaseChar() else c.lowercaseChar() }
            .joinToString("")
    }

    companion object {
        val I18N_PREFIX = TextTransformCommand.I18N_PREFIX.Mock
    }
}

class TextTransformQualitySubcommand : SimpleTextTransformSubcommand(
    uniqueId = UUID.fromString("511461f1-1b98-446d-be42-4d34265ca615"),
    label = I18N_PREFIX.Label,
    description = I18N_PREFIX.Description,
    textOption = I18N_PREFIX.Options.Text
) {
    override fun transform(text: String): String =
        quality(text)

    companion object {
        val I18N_PREFIX = TextTransformCommand.I18N_PREFIX.Quality
    }
}

class TextTransformVaporQualitySubcommand : SimpleTextTransformSubcommand(
    uniqueId = UUID.fromString("b05a32f7-3b45-46b4-9a5b-6cb5fe905bc4"),
    label = I18N_PREFIX.Label,
    description = I18N_PREFIX.Description,
    textOption = I18N_PREFIX.Options.Text
) {
    override fun transform(text: String): String =
        VaporwaveUtils.vaporwave(quality(text))

    companion object {
        val I18N_PREFIX = TextTransformCommand.I18N_PREFIX.Vaporquality
    }
}

private fun quality(text: String): String =
    text.uppercase().toCharArray().joinToString(" ")