package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.roleplay.retribute

import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.roleplay.RoleplayUtils
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

class RetributeHighFiveButtonExecutor(
    loritta: LorittaCinnamon,
    client: RandomRoleplayPicturesClient
) : RetributePictureExecutor(
    loritta,
    client,
    RoleplayUtils.HIGH_FIVE_ATTRIBUTES
) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.RETRIBUTE_HIGH_FIVE_BUTTON_EXECUTOR)
}