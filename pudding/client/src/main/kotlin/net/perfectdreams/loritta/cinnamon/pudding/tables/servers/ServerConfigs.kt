package net.perfectdreams.loritta.cinnamon.pudding.tables.servers

import net.perfectdreams.loritta.cinnamon.pudding.tables.SnowflakeTable
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.InviteBlockerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.MiscellaneousConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.StarboardConfigs
import org.jetbrains.exposed.sql.ReferenceOption

object ServerConfigs : SnowflakeTable() {
    val localeId = text("locale_id").default("default")
    val starboardConfig = optReference("starboard_config", StarboardConfigs, onDelete = ReferenceOption.CASCADE).index()
    val miscellaneousConfig = optReference("miscellaneous_config", MiscellaneousConfigs, onDelete = ReferenceOption.CASCADE).index()
    val inviteBlockerConfig = optReference("invite_blocker_config", InviteBlockerConfigs, onDelete = ReferenceOption.CASCADE).index()
    val moderationConfig = optReference("moderation_config", ModerationConfigs, onDelete = ReferenceOption.CASCADE).index()
}