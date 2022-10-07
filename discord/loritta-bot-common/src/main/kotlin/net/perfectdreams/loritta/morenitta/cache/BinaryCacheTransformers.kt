package net.perfectdreams.loritta.morenitta.cache

import net.perfectdreams.loritta.cinnamon.discord.utils.entitycache.PuddingGuildVoiceState
import net.perfectdreams.loritta.cinnamon.discord.utils.entitycache.ZstdDictionaries
import net.perfectdreams.loritta.deviousfun.cache.*

class BinaryCacheTransformers(zstdDictionaries: ZstdDictionaries) {
    val roles = BinaryCacheTransformer<DeviousRoleData>(zstdDictionaries, ZstdDictionaries.Dictionary.ROLES_V1)
    val emojis = BinaryCacheTransformer<DeviousGuildEmojiData>(zstdDictionaries, ZstdDictionaries.Dictionary.EMOJIS_V1)
    val channels = BinaryCacheTransformer<DeviousChannelData>(zstdDictionaries, ZstdDictionaries.Dictionary.NO_DICTIONARY)
    val voiceStates = BinaryCacheTransformer<PuddingGuildVoiceState>(zstdDictionaries, ZstdDictionaries.Dictionary.NO_DICTIONARY)
    val users = BinaryCacheTransformer<DeviousUserData>(zstdDictionaries, ZstdDictionaries.Dictionary.NO_DICTIONARY)
    val members = BinaryCacheTransformer<DeviousMemberData>(zstdDictionaries, ZstdDictionaries.Dictionary.NO_DICTIONARY)
}