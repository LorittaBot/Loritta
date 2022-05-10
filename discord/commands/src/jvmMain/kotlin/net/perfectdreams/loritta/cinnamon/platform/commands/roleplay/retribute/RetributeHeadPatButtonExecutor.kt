package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute

import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.RoleplayUtils
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

class RetributeHeadPatButtonExecutor(
    client: RandomRoleplayPicturesClient
) : RetributePictureExecutor(
    client,
    RoleplayUtils.HEAD_PAT_ATTRIBUTES
) {
    companion object : ButtonClickExecutorDeclaration(ComponentExecutorIds.RETRIBUTE_HEAD_PAT_BUTTON_EXECUTOR)
}