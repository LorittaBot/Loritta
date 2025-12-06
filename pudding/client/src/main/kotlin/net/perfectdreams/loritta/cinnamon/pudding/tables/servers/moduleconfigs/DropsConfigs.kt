package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.loritta.cinnamon.pudding.tables.SnowflakeTable

object DropsConfigs : SnowflakeTable() {
    val showGuildInformationOnTransactions = bool("show_guild_information_on_transactions").index()
    val guildName = text("guild_name").nullable() // Must be updated when the guild name changes!
    val guildInviteCode = text("guild_invite_code").nullable()
}