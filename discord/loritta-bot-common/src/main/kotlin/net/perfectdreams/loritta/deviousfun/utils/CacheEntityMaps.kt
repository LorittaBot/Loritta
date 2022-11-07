package net.perfectdreams.loritta.deviousfun.utils

import net.perfectdreams.loritta.deviouscache.data.*
import java.util.concurrent.ConcurrentHashMap

data class CacheEntityMaps(
    val users: SnowflakeMap<DeviousUserData>,
    val channels: SnowflakeMap<DeviousChannelData>,
    val guilds: SnowflakeMap<DeviousGuildDataWrapper>,
    val emotes: SnowflakeMap<SnowflakeMap<DeviousGuildEmojiData>>,
    val roles: SnowflakeMap<SnowflakeMap<DeviousRoleData>>,
    val members: SnowflakeMap<SnowflakeMap<DeviousMemberData>>,
    val voiceStates: SnowflakeMap<SnowflakeMap<DeviousVoiceStateData>>,
    val gatewaySessions: ConcurrentHashMap<Int, DeviousGatewaySession>
)