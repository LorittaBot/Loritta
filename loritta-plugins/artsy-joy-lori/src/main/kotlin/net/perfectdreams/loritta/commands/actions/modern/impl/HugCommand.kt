package net.perfectdreams.loritta.commands.actions.modern.impl

import net.perfectdreams.loritta.commands.actions.modern.ActionCommand
import net.perfectdreams.loritta.commands.actions.modern.ActionCommandDSL
import net.perfectdreams.loritta.commands.actions.modern.action
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
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