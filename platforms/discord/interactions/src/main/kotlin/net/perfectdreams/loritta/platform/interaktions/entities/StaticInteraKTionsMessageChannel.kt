package net.perfectdreams.loritta.platform.interaktions.entities

import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.discord.objects.LorittaDiscordMessageChannel

open class StaticInteraKTionsMessageChannel : LorittaDiscordMessageChannel {
    /* override val id: Long = handle.id.value
    override val name: String? = handle.name.value
    override val topic: String? = handle.topic.value
    override val nsfw: Boolean? = handle.nsfw.value
    override val guildId: Long? = handle.guildId.value?.value
    override val creation: Instant = handle.id.timeStamp */

    override suspend fun sendMessage(message: LorittaMessage) {
        error("This kind of channel doesn't support messages.")
    }
}