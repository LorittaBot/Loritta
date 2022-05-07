package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute

import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

class RetributeHugButtonExecutor(
    client: RandomRoleplayPicturesClient
) : RetributePictureExecutor(client, { gender1, gender2 -> client.hug(gender1, gender2) }, Companion) {
    companion object : ButtonClickExecutorDeclaration(
        RetributeHugButtonExecutor::class,
        ComponentExecutorIds.RETRIBUTE_HUG_BUTTON_EXECUTOR
    )
}