package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import net.perfectdreams.exposedpowerutils.sql.jsonb
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberCharacters : LongIdTable() {
    val owner = reference("owner", Profiles).index()
    val name = text("name")
    val createdAtTick = long("created_at_tick")
    val ticksLived = long("ticks_lived")
    val currentTask = jsonb("current_task").nullable()
    val energyNeed = double("energy_need")
    val hungerNeed = double("hunger_need")
    val funNeed = double("fun_need")
    val hygieneNeed = double("hygiene_need")
    val bladderNeed = double("bladder_need")
    val socialNeed = double("social_need")
}