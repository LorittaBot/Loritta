package net.perfectdreams.loritta.cinnamon.emotes

class UnicodeEmote(override val name: String) : net.perfectdreams.loritta.cinnamon.emotes.Emote() {
    override val asMention: String
        get() = name

    override fun toString() = asMention
}