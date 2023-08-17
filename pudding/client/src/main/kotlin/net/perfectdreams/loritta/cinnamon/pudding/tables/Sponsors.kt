package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable

object Sponsors : LongIdTable() {
    val name = text("name")
    val enabled = bool("enabled")
    val payment = reference("payment", Payments)
    val link = text("link")
    val slug = text("slug").index()
    val banners = jsonb("banners")
}