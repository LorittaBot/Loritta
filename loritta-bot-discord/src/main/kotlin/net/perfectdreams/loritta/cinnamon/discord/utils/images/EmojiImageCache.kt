package net.perfectdreams.loritta.cinnamon.discord.utils.images

import com.github.benmanes.caffeine.cache.Caffeine
import dev.kord.rest.Image
import java.awt.image.BufferedImage
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrNull

/**
 * Caches emoji images, used for graphics drawing
 */
class EmojiImageCache {
    private val twitterEmojis = Caffeine.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build<String, Optional<BufferedImage>>().asMap()
    private val discordEmojis = Caffeine.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build<String, Optional<BufferedImage>>().asMap()

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun getDiscordEmoji(emojiId: Long, size: Image.Size): BufferedImage? {
        val imageUrl = "https://cdn.discordapp.com/emojis/${emojiId}.png?size=${size.maxRes}&quality=lossless"
        val cacheKey = "${emojiId}-${size}"

        try {
            if (discordEmojis.containsKey(cacheKey))
                return discordEmojis[cacheKey]?.getOrNull()

            val emoteImage = ImageUtils.downloadImage(imageUrl)
            discordEmojis[cacheKey] = Optional.ofNullable(emoteImage)
            return emoteImage
        } catch (e: Exception) {
            // If anything fails, we will store "empty", to avoid downloading images that do not exist
            discordEmojis[imageUrl] = Optional.empty()
            return null
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun getTwitterEmoji(codePoints: List<Int>): BufferedImage? {
        val hexCodePointsJoinedAsString = codePoints.toList().joinToString("-") { Integer.toHexString(it) }
        val imageUrl = "https://abs.twimg.com/emoji/v2/72x72/${hexCodePointsJoinedAsString}.png"

        try {
            if (twitterEmojis.containsKey(imageUrl))
                return twitterEmojis[imageUrl]?.getOrNull()

            val emoteImage = ImageUtils.downloadImage(imageUrl)
            twitterEmojis[imageUrl] = Optional.ofNullable(emoteImage)
            return emoteImage
        } catch (e: Exception) {
            // If anything fails, we will store "empty", to avoid downloading images that do not exist
            twitterEmojis[imageUrl] = Optional.empty()
            return null
        }
    }
}