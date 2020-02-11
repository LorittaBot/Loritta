package net.perfectdreams.loritta.tables

import com.mrpowergamerbr.loritta.utils.exposed.rawJsonb
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.jsonParser
import org.jetbrains.exposed.dao.LongIdTable

object Sponsors : LongIdTable() {
    val name = text("name")
    val enabled = bool("enabled")
    val payment = reference("payment", Payments)
    val link = text("link")
    val slug = text("slug").index()
    val banners = rawJsonb("banners", gson, jsonParser)
}