package net.perfectdreams.loritta.morenitta.utils

import org.jetbrains.exposed.sql.ResultRow
import java.util.*

data class GuildCommandConfigData(val enabled: Boolean) {
    companion object {
        fun fromResultRow(row: ResultRow) = GuildCommandConfigData(row[net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildCommandConfigs.enabled])

        fun fromResultRowOrDefault(row: ResultRow?): GuildCommandConfigData {
            return if (row != null) {
                GuildCommandConfigData(row[net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildCommandConfigs.enabled])
            } else {
                GuildCommandConfigData(true)
            }
        }
    }
}