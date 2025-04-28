package net.perfectdreams.loritta.morenitta.interactions.modals.options

import net.dv8tion.jda.api.components.ActionComponent
import net.dv8tion.jda.api.components.textinput.TextInput
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.ModalMapping
import java.util.*

sealed class ModalOptionReference<T>

sealed class DiscordModalOptionReference<T>(
    val label: String,
    val required: Boolean
) : ModalOptionReference<T>() {
    val name = UUID.randomUUID().toString()

    abstract fun get(option: ModalMapping): T

    abstract fun toJDA(): ActionComponent
}

class StringDiscordModalOptionReference<T>(
    label: String,
    val style: TextInputStyle,
    val value: String?,
    required: Boolean,
    val placeholder: String?,
    val range: IntRange?
) : DiscordModalOptionReference<T>(label, required) {
    override fun get(option: ModalMapping): T {
        val value = option.asString

        // Discord, when using an optional option, sends down a "" string when the user has not filled it with anything
        //
        // To make the behavior consistent between slash command args and modal args, we will manually send down null if
        // we detect that the parameter is blank
        if (!required && value.isBlank())
            return null as T

        return value as T
    }

    override fun toJDA() = TextInput.create(
        name,
        label,
        style
    ).setValue(value)
        .setPlaceholder(placeholder)
        .setRequired(required)
        .apply {
            if (range != null) {
                setMinLength(range.first)
                setMaxLength(range.last)
            }
        }
        .build()
}

// ===[ BUILDERS ]===
fun modalString(label: String, style: TextInputStyle, value: String? = null, placeholder: String? = null, range: IntRange? = null) = StringDiscordModalOptionReference<String>(
    label,
    style,
    value,
    true,
    placeholder,
    range
)

fun optionalModalString(label: String, style: TextInputStyle, value: String? = null, placeholder: String? = null, range: IntRange? = null) = StringDiscordModalOptionReference<String?>(
    label,
    style,
    value,
    false,
    placeholder,
    range
)