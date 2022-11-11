package net.perfectdreams.loritta.deviousfun.utils

import it.unimi.dsi.fastutil.longs.Long2LongMap
import net.perfectdreams.loritta.deviouscache.data.*
import java.util.concurrent.ConcurrentHashMap

data class CacheEntityMaps(
    val users: SnowflakeMap<DeviousUserData>,
    val guilds: SnowflakeMap<DeviousGuildDataWrapper>,
    val guildChannels: SnowflakeMap<SnowflakeMap<DeviousChannelData>>,
    val channelsToGuilds: Long2LongMap,
    val emotes: SnowflakeMap<SnowflakeMap<DeviousGuildEmojiData>>,
    val roles: SnowflakeMap<SnowflakeMap<DeviousRoleData>>,
    val members: SnowflakeMap<SnowflakeMap<DeviousMemberData>>,
    val voiceStates: SnowflakeMap<SnowflakeMap<DeviousVoiceStateData>>
)