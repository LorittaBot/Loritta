package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay

import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute.RetributeHighFiveButtonExecutor
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

class RoleplayHighFiveExecutor(
    client: RandomRoleplayPicturesClient,
) : RoleplayPictureExecutor(
    client,
    { gender1, gender2 ->
        highFive(gender1, gender2)
    },
    RetributeHighFiveButtonExecutor.Companion
) {
    companion object : SlashCommandExecutorDeclaration(RoleplayHighFiveExecutor::class) {
        override val options = RoleplayPictureExecutor.Companion.Options
    }
}