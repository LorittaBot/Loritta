package net.perfectdreams.loritta.legacy.commands.vanilla.action

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import java.awt.Color

class DanceCommand(loritta: LorittaDiscord): ActionCommand(loritta, listOf("dance", "dançar")) {
    override fun create(): ActionCommandDSL = action {
        emoji = "\uD83D\uDD7A"
        color = Color(255, 152, 0)

        response { locale, sender, target ->
            locale["commands.command.dance.response", sender.asMention, target.asMention]
        }
    }
}