package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute

import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayUtils
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonExecutorDeclaration
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

class RetributeHugButtonExecutor(
    loritta: LorittaCinnamon,
    client: RandomRoleplayPicturesClient
) : RetributePictureExecutor(
    loritta,
    client,
    RoleplayUtils.HUG_ATTRIBUTES
) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.RETRIBUTE_HUG_BUTTON_EXECUTOR)
}