package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object WebsiteDiscountCoupons : LongIdTable() {
    val public = bool("public").index()
    val code = text("code").index()
    val total = double("total")
    val startsAt = timestampWithTimeZone("starts_at").index()
    val endsAt = timestampWithTimeZone("ends_at").index()
    val maxUses = integer("max_uses").nullable()
}