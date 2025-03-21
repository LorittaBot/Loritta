package net.perfectdreams.loritta.common.emotes

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

    private val discordEmoteWithGenericNameAsMention: String by lazy {
        val builder = StringBuilder()
        builder.append("<")
        if (animated)
            builder.append("a")
        builder.append(":l:")
        builder.append(id)
        builder.append(">")
        builder.toString()
    }

    override val asMention: String
            = discordEmoteAsMention

    /**
     * Gets the emoji as a chat mention, with a generic one character name to reduce character counts in a message
     */
    val asMentionWithGenericName: String
        get() = discordEmoteWithGenericNameAsMention
}