package net.perfectdreams.loritta.cinnamon.pudding.tables.servers

import net.perfectdreams.loritta.cinnamon.pudding.tables.SnowflakeTable
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.*
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.TextColumnType

object ServerConfigs : SnowflakeTable() {
    val commandPrefix = text("prefix").default("+")
    val localeId = text("locale_id").default("default")
    val deleteMessageAfterCommand = bool("delete_message_after_command").default(false)
    val warnOnMissingPermission = bool("warn_on_missing_permission").default(false)
    val warnOnUnknownCommand = bool("warn_on_unknown_command").default(false)
    val blacklistedChannels = array<Long>("blacklisted_channels", LongColumnType()).default(listOf())
    val warnIfBlacklisted = bool("warn_if_blacklisted").default(false)
    val blacklistedWarning = text("blacklisted_warning").nullable()
    val disabledCommands = array<String>("disabled_commands", TextColumnType()).default(listOf())
    // val donationKey = optReference("donation_key", DonationKeys)
    val donationConfig = optReference("donation_config", DonationConfigs, onDelete = ReferenceOption.CASCADE).index()
    val economyConfig = optReference("economy_config", EconomyConfigs, onDelete = ReferenceOption.CASCADE).index()
    val levelConfig = optReference("level_config", LevelConfigs, onDelete = ReferenceOption.CASCADE).index()
    val starboardConfig = optReference("starboard_config", StarboardConfigs, onDelete = ReferenceOption.CASCADE).index()
    val miscellaneousConfig = optReference("miscellaneous_config", MiscellaneousConfigs, onDelete = ReferenceOption.CASCADE).index()
    val eventLogConfig = optReference("event_log_config", EventLogConfigs, onDelete = ReferenceOption.CASCADE).index()
    val autoroleConfig = optReference("autorole_config", AutoroleConfigs, onDelete = ReferenceOption.CASCADE).index()
    val inviteBlockerConfig = optReference("invite_blocker_config", InviteBlockerConfigs, onDelete = ReferenceOption.CASCADE).index()
    val welcomerConfig = optReference("welcomer_config", WelcomerConfigs, onDelete = ReferenceOption.CASCADE).index()
    val moderationConfig = optReference("moderation_config", ModerationConfigs, onDelete = ReferenceOption.CASCADE).index()
    val migrationVersion = integer("migration_version").default(0)
}