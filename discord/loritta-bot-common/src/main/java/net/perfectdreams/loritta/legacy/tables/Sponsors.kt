package net.perfectdreams.loritta.legacy.tables

import net.perfectdreams.loritta.legacy.utils.exposed.rawJsonb
import net.perfectdreams.loritta.legacy.utils.gson
import org.jetbrains.exposed.dao.id.LongIdTable

object Sponsors : LongIdTable() {
    val name = text("name")
    val enabled = bool("enabled")
    val payment = reference("payment", Payments)
    val link = text("link")
    val slug = text("slug").index()
    val banners = rawJsonb("banners", gson)
}