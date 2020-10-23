package net.perfectdreams.loritta.commands.actions.modern.impl

import net.perfectdreams.loritta.commands.actions.modern.ActionCommand
import net.perfectdreams.loritta.commands.actions.modern.ActionCommandDSL
import net.perfectdreams.loritta.commands.actions.modern.action
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import java.awt.Color

class KissCommand(loritta: LorittaDiscord): ActionCommand(loritta, listOf("kiss", "beijo", "beijar")) {

    override fun create(): ActionCommandDSL = action {
        emoji = "\uD83D\uDC8F"
        color = Color(233, 30, 99)

        response { locale, sender, target ->
            locale["commands.actions.kiss.response", sender.asMention, target.asMention]
        }
    }

}