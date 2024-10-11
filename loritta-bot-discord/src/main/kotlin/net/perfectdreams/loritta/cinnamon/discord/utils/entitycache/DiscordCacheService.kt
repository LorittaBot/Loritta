package net.perfectdreams.loritta.cinnamon.discord.utils.entitycache

import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.perfectdreams.loritta.morenitta.LorittaBot

/**
 * Services related to Discord entity caching
 */
class DiscordCacheService(
    private val loritta: LorittaBot
) {
    /**
     * Gets the [userId]'s voice channel on [guildId], if they are connected to a voice channel
     *
     * @param guildId the guild's ID
     * @param userId  the user's ID
     * @return the voice channel ID, if they are connected to a voice channel
     */
    suspend fun getUserConnectedVoiceChannel(guildId: Long, userId: Long): VoiceChannel? {
        return loritta.lorittaShards.getGuildById(guildId)
            ?.getMemberById(userId)
            ?.voiceState
            ?.channel
            ?.asVoiceChannel()
    }
}