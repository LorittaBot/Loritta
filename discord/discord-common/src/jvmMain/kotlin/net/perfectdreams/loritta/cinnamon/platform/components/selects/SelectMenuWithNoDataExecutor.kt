package net.perfectdreams.loritta.cinnamon.platform.components.selects

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext

interface SelectMenuWithNoDataExecutor : SelectMenuExecutor {
    suspend fun onSelect(user: User, context: ComponentContext, values: List<String>)
}