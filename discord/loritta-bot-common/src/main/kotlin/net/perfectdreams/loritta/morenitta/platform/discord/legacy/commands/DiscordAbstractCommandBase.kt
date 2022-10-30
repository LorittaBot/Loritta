package net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands

import net.perfectdreams.loritta.morenitta.api.commands.AbstractCommandBase
import net.perfectdreams.loritta.morenitta.api.commands.Command
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.LorittaBot

abstract class DiscordAbstractCommandBase(
    val loritta: LorittaBot,
    labels: List<String>,
    category: net.perfectdreams.loritta.common.commands.CommandCategory
) : AbstractCommandBase<CommandContext, DiscordCommandBuilder>(labels, category) {
    final override fun create(builder: DiscordCommandBuilder.() -> Unit): Command<CommandContext> {
        val cmd = discordCommand(
            loritta,
            labels,
            category
        ) {
            builder.invoke(this)
        }

        return cmd
    }
}