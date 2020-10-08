package net.perfectdreams.loritta.plugin.funfunfun

import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.plugin.funfunfun.commands.HungerGamesCommand

actual fun FunFunFunPlugin.platformSpecificOnEnable() {
    registerCommand(HungerGamesCommand.command(loritta as LorittaDiscord))
}