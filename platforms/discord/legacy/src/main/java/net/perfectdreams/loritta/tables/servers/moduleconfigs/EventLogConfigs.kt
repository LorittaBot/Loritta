package net.perfectdreams.loritta.tables.servers.moduleconfigs

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
}