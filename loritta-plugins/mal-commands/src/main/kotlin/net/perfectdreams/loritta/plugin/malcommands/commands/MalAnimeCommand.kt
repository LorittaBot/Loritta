package net.perfectdreams.loritta.plugin.malcommands.commands

import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.plugin.malcommands.MalCommandsPlugin
import net.perfectdreams.loritta.plugin.malcommands.commands.base.DSLCommandBase

object MalAnimeCommand: DSLCommandBase {
    private val LOCALE_PREFIX = "commands.anime.mal.anime"
    override fun command(loritta: LorittaDiscord, m: MalCommandsPlugin) = create(loritta, listOf("malanime")) {

    }
}