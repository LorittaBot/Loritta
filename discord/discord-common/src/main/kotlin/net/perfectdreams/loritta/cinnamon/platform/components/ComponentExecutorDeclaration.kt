package net.perfectdreams.loritta.cinnamon.platform.components

import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentId

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