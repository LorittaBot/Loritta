package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute

import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayUtils
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

class RetributeHugButtonExecutor(
    client: RandomRoleplayPicturesClient
) : RetributePictureExecutor(
    client,
    { gender1, gender2 -> client.hug(gender1, gender2) },
    Companion,
    RoleplayUtils.HUG_ATTRIBUTES
) {
    companion object : ButtonClickExecutorDeclaration(ComponentExecutorIds.RETRIBUTE_HUG_BUTTON_EXECUTOR)
}