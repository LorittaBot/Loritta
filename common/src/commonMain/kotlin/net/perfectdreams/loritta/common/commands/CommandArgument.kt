package net.perfectdreams.loritta.common.commands

import net.perfectdreams.loritta.common.commands.options.CommandOptions
import kotlin.reflect.KProperty

// Inspired by Kord-Extensions arguments
// https://github.com/Kord-Extensions/kord-extensions/blob/c3c7dcfa33c688058c72076ef383c6f40791b795/kord-extensions/src/main/kotlin/com/kotlindiscord/kord/extensions/commands/converters/SingleConverter.kt
class CommandArgument<T> {
    var parsed: T? = null

    operator fun getValue(options: CommandOptions, property: KProperty<*>): T {
        return parsed as T
    }

    fun optional() = CommandArgument<T?>()
}