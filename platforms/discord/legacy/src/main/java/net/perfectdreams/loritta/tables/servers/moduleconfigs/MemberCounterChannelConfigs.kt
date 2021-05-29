package net.perfectdreams.loritta.tables.servers.moduleconfigs

import com.mrpowergamerbr.loritta.tables.ServerConfigs
import com.mrpowergamerbr.loritta.utils.counter.CounterThemes
import org.jetbrains.exposed.dao.id.LongIdTable

object MemberCounterChannelConfigs : LongIdTable() {
    val guild = reference("guild", ServerConfigs).index()
    val channelId = long("channel").index()
    val topic = text("topic")
    val theme = enumeration("theme", CounterThemes::class)
    val padding = integer("padding")
}