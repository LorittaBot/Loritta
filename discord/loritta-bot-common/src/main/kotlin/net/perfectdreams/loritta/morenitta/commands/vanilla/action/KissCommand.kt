package net.perfectdreams.loritta.morenitta.commands.vanilla.action

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay.RoleplayUtils
import java.awt.Color

class KissCommand(loritta: LorittaBot): ActionCommand(RoleplayUtils.KISS_ATTRIBUTES, loritta, listOf("kiss", "beijo", "beijar")) {
    override fun create(): ActionCommandDSL = action {
        emoji = "\uD83D\uDC8F"
        color = Color(233, 30, 99)

        response { locale, sender, target ->
            locale["commands.command.kiss.response", sender.asMention, target.asMention]
        }
    }
}