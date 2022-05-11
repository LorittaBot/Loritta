package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.source

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.randomroleplaypictures.common.data.api.AnimeSource
import net.perfectdreams.randomroleplaypictures.common.data.api.PictureSource

class SourcePictureExecutor : ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration(ComponentExecutorIds.SOURCE_PICTURE_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        val pictureSource = ComponentDataUtils.decode<PictureSource>(data)

        context.sendEphemeralMessage {
            content = when (pictureSource) {
                is AnimeSource -> pictureSource.name
            }
        }
    }
}