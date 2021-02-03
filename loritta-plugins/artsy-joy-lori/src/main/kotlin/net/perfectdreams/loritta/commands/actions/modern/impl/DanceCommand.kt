package net.perfectdreams.loritta.commands.actions.modern.impl

import net.perfectdreams.loritta.commands.actions.modern.ActionCommand
import net.perfectdreams.loritta.commands.actions.modern.ActionCommandDSL
import net.perfectdreams.loritta.commands.actions.modern.action
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
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