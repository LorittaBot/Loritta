package net.perfectdreams.loritta.morenitta.interactions.commands

import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.Command

@Serializable
data class DiscordCommand(
    val id: Long,
    val name: String,
    val nameLocalizations: Map<DiscordLocale, String>,
    val subcommands: List<DiscordSubcommand>,
    val subcommandGroups: List<DiscordSubcommandGroup>,
) {
    companion object {
        fun from(command: Command) = DiscordCommand(
            command.idLong,
            command.name,
            command.nameLocalizations.toMap(),
            command.subcommands.map {
                DiscordSubcommand(it.name, it.nameLocalizations.toMap())
            },
            command.subcommandGroups.map {
                DiscordSubcommandGroup(
                    it.name,
                    it.nameLocalizations.toMap(),
                    it.subcommands.map {
                        DiscordSubcommand(it.name, it.nameLocalizations.toMap())
                    }
                )
            }
        )
    }

    @Serializable
    data class DiscordSubcommand(
        val name: String,
        val nameLocalizations: Map<DiscordLocale, String>
    )

    @Serializable
    data class DiscordSubcommandGroup(
        val name: String,
        val nameLocalizations: Map<DiscordLocale, String>,
        val subcommands: List<DiscordSubcommand>
    )
}