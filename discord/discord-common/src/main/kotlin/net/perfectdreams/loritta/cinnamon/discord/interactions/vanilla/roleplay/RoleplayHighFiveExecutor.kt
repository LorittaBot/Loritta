package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.roleplay

import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

class RoleplayHighFiveExecutor(
    loritta: LorittaCinnamon,
    client: RandomRoleplayPicturesClient,
) : RoleplayPictureExecutor(
    loritta,
    client,
    RoleplayUtils.HIGH_FIVE_ATTRIBUTES
)