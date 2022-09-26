package net.perfectdreams.loritta.legacy.commands.vanilla.action

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import java.awt.Color

class HugCommand(loritta: LorittaDiscord): ActionCommand(loritta, listOf("hug", "abraço", "abraçar", "abraco", "abracar")) {
    override fun create(): ActionCommandDSL = action {
        emoji = "\uD83D\uDC99"
        color = Color(255, 235, 59)

        response { locale, sender, target ->
            locale["commands.command.hug.response", sender.asMention, target.asMention]
        }
    }
}