package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberCharacters : LongIdTable() {
    val owner = reference("owner", Profiles).index()
    val name = text("name")
    val currentTask = jsonb("current_task").nullable()
    val energy = double("energy")
    val hunger = double("hunger")
}