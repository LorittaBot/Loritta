package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import net.perfectdreams.loritta.common.commands.ApplicationCommandType
import net.perfectdreams.loritta.common.commands.InteractionContextType
import org.jetbrains.exposed.sql.javatime.timestamp

object ExecutedApplicationCommandsLog : LongIdTableWithoutOverriddenPrimaryKey() {
    val userId = long("user").index()
    val guildId = long("guild").nullable()
    val channelId = long("channel")
    // Because this is already a partition table, we can't change its type (for now)
    val sentAt = timestamp("sent_at").index()

    val type = postgresEnumeration<ApplicationCommandType>("type").index()
    val declaration = text("declaration").index()
    val executor = text("executor").index()
    val options = jsonb("options")
    val success = bool("success")
    val latency = double("latency")
    val context = postgresEnumeration<InteractionContextType>("context").nullable()
    val guildIntegration = long("guild_integration").nullable()
    val userIntegration = long("user_integration").nullable()
    val lorittaCluster = integer("loritta_cluster").nullable()

    val stacktrace = text("stacktrace").nullable()

    // Sent At must be a primary key because it is used as a partition key
    // While this means that all partitions should have an unique ID and Sent At, the ID is always incrementing so I don't think
    // that this will cause issues
    override val primaryKey = PrimaryKey(id, sentAt)
}