package net.perfectdreams.loritta.commands.actions.modern.impl

import com.mrpowergamerbr.loritta.LorittaLauncher
import net.perfectdreams.loritta.commands.actions.modern.ActionCommand
import net.perfectdreams.loritta.commands.actions.modern.ActionCommandDSL
import net.perfectdreams.loritta.commands.actions.modern.action
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import java.awt.Color

class AttackCommand(loritta: LorittaDiscord): ActionCommand(loritta, listOf("attack", "atacar")) {

    override fun create(): ActionCommandDSL = action {
        emoji = "\uD83E\uDD4A"
        color = Color(244, 67, 54)

        response { locale, sender, target ->
            if (target.id != LorittaLauncher.loritta.discordConfig.discord.clientId) {
                locale["commands.actions.attack.response", sender.asMention, target.asMention]
            } else {
                locale["commands.actions.attack.responseAntiIdiot", sender.asMention, target.asMention]
            }
        }
    }

}