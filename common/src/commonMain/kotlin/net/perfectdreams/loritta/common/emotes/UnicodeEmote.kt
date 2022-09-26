package net.perfectdreams.loritta.common.emotes

class UnicodeEmote(override val name: String) : Emote() {
    override val asMention: String
        get() = name

    override fun toString() = asMention
}