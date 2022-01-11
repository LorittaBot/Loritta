package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.array
import org.jetbrains.exposed.sql.TextColumnType

object Giveaways : SnowflakeTable() {
    val channelId = long("channel_id")
    val guildId = long("guild_id")
    val title = text("title")

    val numberOfWinners = integer("number_of_winners")
    val users = array<String>("users", TextColumnType())

    val finishAt = long("finish_at")
    val finished = bool("finished")

    val host = long("host")
    val awardRoleIds = array<String>("award_roles", TextColumnType()).nullable()
    val awardSonhosPerWinner = long("award_sonhos_per_winner").nullable()
}