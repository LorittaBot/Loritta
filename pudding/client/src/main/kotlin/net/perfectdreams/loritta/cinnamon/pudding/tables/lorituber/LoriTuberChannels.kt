package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberChannels : LongIdTable() {
    val owner = reference("owner", LoriTuberCharacters).index()
    val name = text("name")
}