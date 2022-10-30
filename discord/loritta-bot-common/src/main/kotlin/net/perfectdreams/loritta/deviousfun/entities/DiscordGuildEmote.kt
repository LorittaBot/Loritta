package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.Permission
import net.perfectdreams.loritta.deviouscache.data.DeviousGuildEmojiData
import net.perfectdreams.loritta.deviousfun.DeviousFun

class DiscordGuildEmote(
    override val deviousFun: DeviousFun,
    val guild: Guild,
    val emoji: DeviousGuildEmojiData
) : DiscordEmote {
    override val idSnowflake by emoji::id
    override val name by emoji::name
    override val isAnimated by emoji::animated
    override val imageUrl: String
        get() = TODO("Not yet implemented")

    suspend fun setName(name: String) {
        deviousFun.loritta.rest.emoji.modifyEmoji(
            guild.idSnowflake,
            idSnowflake,
        ) {
            this.name = name
        }
    }

    suspend fun delete() {
        deviousFun.loritta.rest.emoji.deleteEmoji(
            guild.idSnowflake,
            idSnowflake,
        )
    }

    suspend fun canInteract(member: Member) = member.hasPermission(Permission.ManageEmojisAndStickers)
}