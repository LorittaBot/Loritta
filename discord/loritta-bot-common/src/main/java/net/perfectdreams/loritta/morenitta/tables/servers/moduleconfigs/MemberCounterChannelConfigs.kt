package net.perfectdreams.loritta.morenitta.tables.servers.moduleconfigs

import net.perfectdreams.loritta.morenitta.tables.ServerConfigs
import net.perfectdreams.loritta.morenitta.utils.counter.CounterThemes
import org.jetbrains.exposed.dao.id.LongIdTable

object MemberCounterChannelConfigs : LongIdTable() {
    val guild = reference("guild", ServerConfigs).index()
    val channelId = long("channel").index()
    val topic = text("topic")
    val theme = enumeration("theme", CounterThemes::class)
    val padding = integer("padding")
}