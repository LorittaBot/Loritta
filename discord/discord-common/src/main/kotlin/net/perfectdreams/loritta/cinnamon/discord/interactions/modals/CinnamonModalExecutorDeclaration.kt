package net.perfectdreams.loritta.cinnamon.discord.interactions.modals

import net.perfectdreams.loritta.cinnamon.discord.interactions.ComponentId

open class CinnamonModalExecutorDeclaration(
    parent: Any? = null,
    id: ComponentId
) : net.perfectdreams.discordinteraktions.common.modals.ModalExecutorDeclaration(parent, id.value) {
    constructor(id: ComponentId) : this(null, id)
}