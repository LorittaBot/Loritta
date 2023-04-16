package net.perfectdreams.loritta.morenitta.commands.vanilla.action

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay.RoleplayUtils
import java.awt.Color

class HugCommand(loritta: LorittaBot): ActionCommand(RoleplayUtils.HUG_ATTRIBUTES, loritta, listOf("hug", "abraço", "abraçar", "abraco", "abracar")) {
    override fun create(): ActionCommandDSL = action {
        emoji = "\uD83D\uDC99"
        color = Color(255, 235, 59)

        response { locale, sender, target ->
            locale["commands.command.hug.response", sender.asMention, target.asMention]
        }
    }
}