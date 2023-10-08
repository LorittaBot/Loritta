package net.perfectdreams.loritta.cinnamon.discord.interactions.components

import net.perfectdreams.loritta.cinnamon.discord.interactions.ComponentId

open class ButtonExecutorDeclaration(
    parent: Any? = null,
    id: ComponentId
) : net.perfectdreams.discordinteraktions.common.components.ButtonExecutorDeclaration(parent, id.value) {
    constructor(id: ComponentId) : this(null, id)
}

open class SelectMenuExecutorDeclaration(
    parent: Any? = null,
    id: ComponentId
) : net.perfectdreams.discordinteraktions.common.components.SelectMenuExecutorDeclaration(parent, id.value) {
    constructor(id: ComponentId) : this(null, id)
}