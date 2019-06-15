package net.perfectdreams.loritta.socket.network

object SocketOpCode {
    /**
     * Heartbeat
     */
    const val HEARTBEAT                     = 10000

    object Discord {
        // ===[ DISCORD ]===
        const val IDENTIFY                  = 20000
        const val GET_USER_BY_ID            = 20001
        const val GET_USERS_BY_ID           = 20002
        const val RETRIEVE_USER_BY_ID       = 20002
        const val RETRIEVE_USERS_BY_ID      = 20003
        const val GET_GUILD_BY_ID           = 20004
        const val GET_GUILDS_BY_ID          = 20005
        const val UPDATE_DISCORD_STATS      = 20006
        const val GET_GUILD_CONFIG_BY_ID    = 20007
        const val UPDATE_GUILD_CONFIG_BY_ID = 20008
        const val GET_LORITTA_SHARDS        = 20009
    }
}