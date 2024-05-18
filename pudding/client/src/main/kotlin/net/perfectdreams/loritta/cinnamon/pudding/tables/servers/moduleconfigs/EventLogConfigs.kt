package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object EventLogConfigs : LongIdTable() {
    val enabled = bool("enabled").default(false)
    val eventLogChannelId = long("event_log_channel")
    val memberBanned = bool("member_banned").default(false)
    val memberUnbanned = bool("member_unbanned").default(false)
    val messageEdited = bool("message_edited").default(false)
    val messageDeleted = bool("message_deleted").default(false)
    val nicknameChanges = bool("nickname_changes").default(false)
    val voiceChannelJoins = bool("voice_channel_joins").default(false)
    val voiceChannelLeaves = bool("voice_channel_leaves").default(false)
    val avatarChanges = bool("avatar_changes").default(false)

    val memberBannedLogChannelId = long("member_banned_log_channel").nullable()
    val memberUnbannedLogChannelId = long("member_unbanned_log_channel").nullable()
    val messageEditedLogChannelId = long("message_edited_log_channel").nullable()
    val messageDeletedLogChannelId = long("message_deleted_log_channel").nullable()
    val nicknameChangesLogChannelId = long("nickname_changes_log_channel").nullable()
    val voiceChannelJoinsLogChannelId = long("voice_channel_joins_log_channel").nullable()
    val voiceChannelLeavesLogChannelId = long("voice_channel_leaves_log_channel").nullable()
    val avatarChangesLogChannelId = long("avatar_changes_log_channel").nullable()

    val updatedAt = timestampWithTimeZone("updated_at").nullable()
}