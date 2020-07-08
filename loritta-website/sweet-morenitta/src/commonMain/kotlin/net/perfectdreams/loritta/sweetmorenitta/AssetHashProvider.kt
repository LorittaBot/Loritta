package net.perfectdreams.loritta.sweetmorenitta

/**
 * Provides assets hashs for frontend files, used for cache busting
 */
interface AssetHashProvider {
    /**
     * Provides a asset hash for the specified [filePath]
     *
     * @param  filePath where the file is located
     * @return a asset hash for the specified [filePath], if the file's content
     * was changed, the hash will be different, useful for cache busting
     */
    fun assetHash(filePath: String): String
}