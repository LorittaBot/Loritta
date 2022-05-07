package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute

import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

class RetributeHeadPatButtonExecutor(
    client: RandomRoleplayPicturesClient
) : RetributePictureExecutor(client, { gender1, gender2 -> client.headPat(gender1, gender2) }, Companion) {
    companion object : ButtonClickExecutorDeclaration(
        RetributeHeadPatButtonExecutor::class,
        ComponentExecutorIds.RETRIBUTE_HEAD_PAT_BUTTON_EXECUTOR
    )
}