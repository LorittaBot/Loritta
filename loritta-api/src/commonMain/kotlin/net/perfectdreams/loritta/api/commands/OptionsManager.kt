package net.perfectdreams.loritta.api.commands

import net.perfectdreams.loritta.api.commands.declarations.CommandOption

interface OptionsManager {
    fun getNullableString(option: CommandOption<String?>): String?
    fun getString(option: CommandOption<String>): String
    fun getNullableInt(option: CommandOption<Int?>): Int?
}