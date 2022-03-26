package net.perfectdreams.loritta.cinnamon.platform.commands.options

import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext

class ArgumentReader(
    val context: ApplicationCommandContext,
    val arguments: Map<net.perfectdreams.discordinteraktions.common.commands.options.CommandOption<*>, Any?>
) {
    val entries = arguments.entries

    fun getOptionValueOrNull(name: String) = entries.firstOrNull { opt -> name == opt.key.name }?.value
}