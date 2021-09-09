package net.perfectdreams.loritta.cinnamon.common.emotes

class DiscordEmote(
    val id: Long,
    override val name: String,
    val animated: Boolean
) : Emote() {
    // To avoid recreating the Discord emote code every single call, we keep it loaded in memory.
    private val discordEmoteAsMention: String by lazy {
        val builder = StringBuilder()
        builder.append("<")
        if (animated)
            builder.append("a")
        builder.append(":")
        builder.append(name)
        builder.append(":")
        builder.append(id)
        builder.append(">")
        builder.toString()
    }

    override val asMention: String
            = discordEmoteAsMention
}