package net.perfectdreams.loritta.morenitta.commands.vanilla.action

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay.RoleplayUtils
import java.awt.Color

class HeadPatCommand(loritta: LorittaBot): ActionCommand(RoleplayUtils.HEAD_PAT_ATTRIBUTES, loritta, listOf("headpat", "headpet", "cafuné", "cafune", "pat")) {
    override fun create(): ActionCommandDSL = action {
        emoji = Emotes.LORI_PAT.toString()
        color = Color(156, 39, 176)

        response { locale, sender, target ->
            locale["commands.command.headpat.response", sender.asMention, target.asMention]
        }
    }
}