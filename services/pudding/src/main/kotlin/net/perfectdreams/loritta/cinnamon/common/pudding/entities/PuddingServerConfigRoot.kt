package net.perfectdreams.loritta.cinnamon.common.pudding.entities

import net.perfectdreams.loritta.cinnamon.common.entities.ServerConfigRoot

class PuddingServerConfigRoot(val serverConfigRoot: net.perfectdreams.loritta.pudding.common.data.ServerConfigRoot) : ServerConfigRoot {
    override val id = serverConfigRoot.id.toULong()
    override val localeId by serverConfigRoot::localeId
}