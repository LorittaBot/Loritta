package net.perfectdreams.loritta.api.entities

class UnicodeEmote(code: String) : LorittaEmote(code) {
    override val asMention: String
        get() = getName()

    override fun getName(): String {
        return code
    }

    override fun isAvailable(): Boolean {
        return true
    }

    override fun toString() = asMention
}