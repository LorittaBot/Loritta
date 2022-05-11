package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute

import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayUtils
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

class RetributeAttackButtonExecutor(
    client: RandomRoleplayPicturesClient
) : RetributePictureExecutor(
    client,
    RoleplayUtils.ATTACK_ATTRIBUTES
) {
    companion object : ButtonClickExecutorDeclaration(ComponentExecutorIds.RETRIBUTE_ATTACK_BUTTON_EXECUTOR)
}