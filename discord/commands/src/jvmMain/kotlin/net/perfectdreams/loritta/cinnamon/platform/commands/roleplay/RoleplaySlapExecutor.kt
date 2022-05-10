package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay

import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

class RoleplaySlapExecutor(
    client: RandomRoleplayPicturesClient,
) : RoleplayPictureExecutor(
    client,
    RoleplayUtils.SLAP_ATTRIBUTES
) {
    companion object : SlashCommandExecutorDeclaration() {
        override val options = RoleplayPictureExecutor.Companion.Options
    }
}