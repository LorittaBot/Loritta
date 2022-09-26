package net.perfectdreams.loritta.morenitta.commands.vanilla.action

import net.perfectdreams.loritta.morenitta.LorittaLauncher
import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord
import java.awt.Color

class SlapCommand(loritta: LorittaDiscord): ActionCommand(loritta, listOf("slap", "tapa", "tapinha")) {
    override fun create(): ActionCommandDSL = action {
        emoji = "\uD83D\uDE40"
        color = Color(244, 67, 54)

        response { locale, sender, target ->
            if (target.id != LorittaLauncher.loritta.discordConfig.discord.clientId) {
                locale["commands.command.slap.response", sender.asMention, target.asMention]
            } else {
                locale["commands.command.slap.responseAntiIdiot", sender.asMention, target.asMention]
            }
        }
    }
}