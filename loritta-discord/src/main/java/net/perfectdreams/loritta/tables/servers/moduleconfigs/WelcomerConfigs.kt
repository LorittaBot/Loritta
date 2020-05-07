package net.perfectdreams.loritta.tables.servers.moduleconfigs

import org.jetbrains.exposed.dao.id.LongIdTable

object WelcomerConfigs : LongIdTable() {
    val tellOnJoin = bool("tell_on_join").default(false)
    val channelJoinId = long("channel_join").nullable()
    val joinMessage = text("join_message").nullable()
    val deleteJoinMessagesAfter = long("delete_join_messages_after").nullable()

    val tellOnRemove = bool("tell_on_remove").default(false)
    val channelRemoveId = long("channel_remove").nullable()
    val removeMessage = text("remove_message").nullable()
    val deleteRemoveMessagesAfter = long("delete_remove_messages_after").nullable()

    val tellOnPrivateJoin = bool("tell_on_private_join").default(false)
    val joinPrivateMessage = text("join_private_message").nullable()

    val tellOnBan = bool("tell_on_ban").default(false)
    val bannedMessage = text("banned_message").nullable()
}