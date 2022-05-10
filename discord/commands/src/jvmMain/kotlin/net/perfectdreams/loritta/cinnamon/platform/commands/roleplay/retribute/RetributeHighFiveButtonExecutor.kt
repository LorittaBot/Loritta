package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute

import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayUtils
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

class RetributeHighFiveButtonExecutor(
    client: RandomRoleplayPicturesClient
) : RetributePictureExecutor(
    client,
    { gender1, gender2 -> client.highFive(gender1, gender2) },
    Companion,
    RoleplayUtils.HIGH_FIVE_ATTRIBUTES
) {
    companion object : ButtonClickExecutorDeclaration(ComponentExecutorIds.RETRIBUTE_HIGH_FIVE_BUTTON_EXECUTOR)
}