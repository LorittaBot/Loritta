package net.perfectdreams.loritta.cinnamon.pudding.entities

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.ServerConfigRoot

class PuddingServerConfigRoot(
    private val pudding: Pudding,
    val data: ServerConfigRoot
) {
    companion object;

    val id by data::id
    val localeId by data::localeId
}