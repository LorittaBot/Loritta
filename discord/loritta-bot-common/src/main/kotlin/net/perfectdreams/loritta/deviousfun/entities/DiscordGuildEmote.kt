package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.Permission
import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.cache.DeviousGuildEmojiData

class DiscordGuildEmote(
    override val jda: JDA,
    val guild: Guild,
    val emoji: DeviousGuildEmojiData
) : DiscordEmote {
    override val idSnowflake by emoji::id
    override val name by emoji::name
    override val isAnimated by emoji::animated
    override val imageUrl: String
        get() = TODO("Not yet implemented")

    suspend fun setName(name: String) {
        jda.loritta.rest.emoji.modifyEmoji(
            guild.idSnowflake,
            idSnowflake,
        ) {
            this.name = name
        }
    }

    suspend fun delete() {
        jda.loritta.rest.emoji.deleteEmoji(
            guild.idSnowflake,
            idSnowflake,
        )
    }

    suspend fun canInteract(member: Member) = member.hasPermission(Permission.ManageEmojisAndStickers)
}