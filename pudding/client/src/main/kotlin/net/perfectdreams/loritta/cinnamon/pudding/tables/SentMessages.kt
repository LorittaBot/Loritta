package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone

object SentMessages : LongIdTableWithoutOverriddenPrimaryKey() {
    val userId = long("user").index()
    val guildId = long("guild").index().nullable()
    val channelId = long("channel").index()
    val sentAt = timestampWithTimeZone("sent_at").index()

    // Sent At must be a primary key because it is used as a partition key
    // While this means that all partitions should have an unique ID and Sent At, the ID is always incrementing so I don't think
    // that this will cause issues
    override val primaryKey = PrimaryKey(id, sentAt)
}