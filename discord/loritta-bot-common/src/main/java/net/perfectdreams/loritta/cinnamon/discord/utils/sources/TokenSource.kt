package net.perfectdreams.loritta.cinnamon.discord.utils.sources

import net.perfectdreams.loritta.common.utils.LorittaPlaceholder

interface TokenSource {
    fun tokens(): Map<LorittaPlaceholder, String?>

    fun inheritFromAndPutAll(source: TokenSource, vararg pairs: Pair<LorittaPlaceholder, String?>) = source.tokens().toMutableMap().apply {
        putAll(pairs)
    }
}