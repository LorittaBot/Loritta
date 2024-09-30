package net.perfectdreams.loritta.lorituber.server.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.json.json

object LoriTuberCharacters : LongIdTable() {
    // Discord User ID
    val ownerId = long("owner_id").index()
    val firstName = text("first_name")
    val lastName = text("last_name")
    val createdAtTick = long("created_at_tick")
    val ticksLived = long("ticks_lived")
    val currentTask = json("current_task", { it }, { it }).nullable()
    val energyNeed = double("energy_need")
    val hungerNeed = double("hunger_need")
    val funNeed = double("fun_need")
    val hygieneNeed = double("hygiene_need")
    val bladderNeed = double("bladder_need")
    val socialNeed = double("social_need")
}