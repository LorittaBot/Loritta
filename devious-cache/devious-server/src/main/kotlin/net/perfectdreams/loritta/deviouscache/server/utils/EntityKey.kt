package net.perfectdreams.loritta.deviouscache.server.utils

import dev.kord.common.entity.Snowflake

sealed class EntityKey {
    abstract val id: Snowflake
}

data class GuildKey(override val id: Snowflake) : EntityKey()

data class ChannelKey(override val id: Snowflake) : EntityKey()

data class UserKey(override val id: Snowflake) : EntityKey()