package net.perfectdreams.loritta.cinnamon.common.emotes

class UnicodeEmote(code: String) : Emote(code) {
    override val asMention: String
        get() = getName()

    override fun getName(): String {
        return code
    }

    override fun toString() = asMention
}