package net.perfectdreams.loritta.commands.actions.modern.impl

import net.perfectdreams.loritta.commands.actions.modern.ActionCommand
import net.perfectdreams.loritta.commands.actions.modern.ActionCommandDSL
import net.perfectdreams.loritta.commands.actions.modern.action
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.Emotes
import java.awt.Color

class HeadPatCommand(loritta: LorittaDiscord): ActionCommand(loritta, listOf("headpat", "headpet", "cafuné", "cafune", "pat")) {

    override fun create(): ActionCommandDSL = action {
        emoji = Emotes.LORI_PAT.toString()
        color = Color(156, 39, 176)

        response { locale, sender, target ->
            locale["commands.actions.headpat.response", sender.asMention, target.asMention]
        }
    }

}