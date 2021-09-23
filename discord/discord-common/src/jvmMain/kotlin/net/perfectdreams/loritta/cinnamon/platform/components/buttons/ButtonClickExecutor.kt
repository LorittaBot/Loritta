package net.perfectdreams.loritta.cinnamon.platform.components.buttons

import net.perfectdreams.discordinteraktions.api.entities.User
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentExecutor

interface ButtonClickExecutor : ComponentExecutor {
    suspend fun onClick(user: User, context: ComponentContext, data: String)
}