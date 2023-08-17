package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.common.utils.CounterThemes
import org.jetbrains.exposed.dao.id.LongIdTable

object MemberCounterChannelConfigs : LongIdTable() {
    val guild = reference("guild", ServerConfigs).index()
    val channelId = long("channel").index()
    val topic = text("topic")
    val theme = enumeration("theme", CounterThemes::class)
    val padding = integer("padding")
}