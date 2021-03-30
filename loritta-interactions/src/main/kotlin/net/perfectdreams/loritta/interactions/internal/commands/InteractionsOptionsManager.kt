package net.perfectdreams.loritta.interactions.internal.commands

import net.perfectdreams.discordinteraktions.commands.get
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.loritta.api.commands.OptionsManager
import net.perfectdreams.loritta.api.commands.declarations.CommandOption

class InteractionsOptionsManager(val context: SlashCommandContext) : OptionsManager {
    override fun getNullableString(option: CommandOption<String?>): String? {
        val interaKtionsOption = context.command.declaration.options.arguments.first { it.name == option.name }
                as net.perfectdreams.discordinteraktions.declarations.slash.CommandOption<String?>

        return interaKtionsOption.get(context)
    }

    override fun getString(option: CommandOption<String>): String {
        val interaKtionsOption = context.command.declaration.options.arguments.first { it.name == option.name }
                as net.perfectdreams.discordinteraktions.declarations.slash.CommandOption<String>

        return interaKtionsOption.get(context)
    }

    override fun getNullableInt(option: CommandOption<Int?>): Int? {
        val interaKtionsOption = context.command.declaration.options.arguments.first { it.name == option.name }
                as net.perfectdreams.discordinteraktions.declarations.slash.CommandOption<Int?>

        return interaKtionsOption.get(context)
    }
}