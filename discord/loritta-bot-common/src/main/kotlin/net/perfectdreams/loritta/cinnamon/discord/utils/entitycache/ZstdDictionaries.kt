package net.perfectdreams.loritta.cinnamon.discord.utils.entitycache

import com.github.luben.zstd.ZstdDictCompress
import com.github.luben.zstd.ZstdDictDecompress
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon

class ZstdDictionaries {
    enum class Dictionary {
        NO_DICTIONARY,
        ROLES_V1,
        CHANNELS_V1,
        EMOJIS_V1
    }

    val rolesV1 = ZstdDictionary(
        LorittaCinnamon::class.java.getResourceAsStream("/zstd_dictionaries/roles_v1")
            .readAllBytes()
    )
    val channelsV1 = ZstdDictionary(
        LorittaCinnamon::class.java.getResourceAsStream("/zstd_dictionaries/channels_v1")
            .readAllBytes()
    )
    val emojisV1 = ZstdDictionary(
        LorittaCinnamon::class.java.getResourceAsStream("/zstd_dictionaries/emojis_v1")
            .readAllBytes()
    )

    class ZstdDictionary(dict: ByteArray) {
        val compress = ZstdDictCompress(dict, 2)
        val decompress = ZstdDictDecompress(dict)
    }
}