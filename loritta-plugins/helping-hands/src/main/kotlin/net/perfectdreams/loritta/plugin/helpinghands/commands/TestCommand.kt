package net.perfectdreams.loritta.plugin.helpinghands.commands

import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.helpinghands.HelpingHandsPlugin

class TestCommand(val plugin: HelpingHandsPlugin) : DiscordAbstractCommandBase(
        plugin.loritta,
        listOf("testcommand"),
        CommandCategory.MISC
) {
    override fun command() = create {
        executesDiscord {
            reply("Hello World!")
        }
    }
}