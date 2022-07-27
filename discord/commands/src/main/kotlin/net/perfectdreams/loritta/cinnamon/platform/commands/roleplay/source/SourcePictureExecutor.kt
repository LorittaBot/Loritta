package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.source

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.randomroleplaypictures.common.data.api.AnimeSource
import net.perfectdreams.randomroleplaypictures.common.data.api.PictureSource

class SourcePictureExecutor(loritta: LorittaCinnamon) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.SOURCE_PICTURE_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        val pictureSource = context.decodeDataFromComponentOrFromDatabase<PictureSource>()

        context.sendEphemeralMessage {
            content = when (pictureSource) {
                is AnimeSource -> pictureSource.name
            }
        }
    }
}