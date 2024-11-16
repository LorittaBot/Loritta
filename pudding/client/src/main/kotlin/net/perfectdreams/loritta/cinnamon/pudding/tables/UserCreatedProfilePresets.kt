package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object UserCreatedProfilePresets : LongIdTable() {
    val createdBy = long("created_by").index()
    val createdAt = timestampWithTimeZone("created_at")
    val lastUsedAt = timestampWithTimeZone("last_used_at").nullable()
    val name = text("name")
    val profileDesign = reference("profile_design", ProfileDesigns)
    val background = reference("background", Backgrounds)
    // val aboutMe = text("about_me").nullable()
    // val activeBadge = uuid("active_badge").nullable()
}