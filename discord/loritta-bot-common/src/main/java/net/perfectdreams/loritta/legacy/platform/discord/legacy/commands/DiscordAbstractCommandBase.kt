package net.perfectdreams.loritta.legacy.platform.discord.legacy.commands

import net.perfectdreams.loritta.common.api.commands.AbstractCommandBase
import net.perfectdreams.loritta.common.api.commands.Command
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.api.commands.CommandContext
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord

abstract class DiscordAbstractCommandBase(
        val loritta: LorittaDiscord,
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