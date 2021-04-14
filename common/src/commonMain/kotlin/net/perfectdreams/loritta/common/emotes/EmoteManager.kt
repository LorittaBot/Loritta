package net.perfectdreams.loritta.common.emotes

interface EmoteManager {
    fun getEmoteByName(name: String): Emote
    fun getEmoteByCode(code: String): Emote

    class DefaultEmoteManager : EmoteManager {
        override fun getEmoteByName(name: String) = getEmoteByCode(name)
        override fun getEmoteByCode(code: String) = UnicodeEmote(code)
    }
}