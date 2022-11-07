package net.perfectdreams.loritta.deviousfun.utils

import net.perfectdreams.loritta.deviouscache.data.LightweightSnowflake

sealed class EntityKey {
    abstract val id: LightweightSnowflake
}

data class GuildKey(override val id: LightweightSnowflake) : EntityKey()

data class ChannelKey(override val id: LightweightSnowflake) : EntityKey()

data class UserKey(override val id: LightweightSnowflake) : EntityKey()