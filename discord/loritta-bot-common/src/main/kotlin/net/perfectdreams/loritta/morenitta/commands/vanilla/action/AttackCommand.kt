package net.perfectdreams.loritta.morenitta.commands.vanilla.action

import net.perfectdreams.loritta.morenitta.LorittaLauncher
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay.RoleplayUtils
import java.awt.Color

class AttackCommand(loritta: LorittaBot): ActionCommand(RoleplayUtils.ATTACK_ATTRIBUTES, loritta, listOf("attack", "atacar")) {
    override fun create(): ActionCommandDSL = action {
        emoji = "\uD83E\uDD4A"
        color = Color(244, 67, 54)

        response { locale, sender, target ->
            if (target.id != loritta.config.loritta.discord.applicationId.toString()) {
                locale["commands.command.attack.response", sender.asMention, target.asMention]
            } else {
                locale["commands.command.attack.responseAntiIdiot", sender.asMention, target.asMention]
            }
        }
    }
}