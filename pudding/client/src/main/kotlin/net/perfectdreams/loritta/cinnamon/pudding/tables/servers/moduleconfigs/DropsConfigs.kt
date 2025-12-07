package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.loritta.cinnamon.pudding.tables.SnowflakeTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object DropsConfigs : SnowflakeTable() {
    val showGuildInformationOnTransactions = bool("show_guild_information_on_transactions").index()
    val guildName = text("guild_name").nullable() // Must be updated when the guild name changes!
    val guildInviteCode = text("guild_invite_code").nullable()
    val updatedAt = timestampWithTimeZone("updated_at").nullable()
}