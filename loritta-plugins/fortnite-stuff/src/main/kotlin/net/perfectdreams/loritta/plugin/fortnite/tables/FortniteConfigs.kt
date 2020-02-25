package net.perfectdreams.loritta.plugin.fortnite.tables

import org.jetbrains.exposed.dao.LongIdTable

object FortniteConfigs : LongIdTable() {
    val advertiseNewItems = bool("advertise_new_items").default(false)
    val channelToAdvertiseNewItems = long("channel").nullable()
}