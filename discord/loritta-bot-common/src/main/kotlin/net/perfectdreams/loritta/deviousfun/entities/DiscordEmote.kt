package net.perfectdreams.loritta.deviousfun.entities

interface DiscordEmote : Emote, IdentifiableSnowflake {
    val isAnimated: Boolean
    val imageUrl: String
        get() = "https://cdn.discordapp.com/emojis/$idSnowflake.${if (isAnimated) "gif" else "png"}"
    override val asMention: String
        get() = buildString {
            append("<")
            if (isAnimated)
                append("a")
            append(":")
            append(name)
            append(":")
            append(idSnowflake)
            append(">")
        }
}