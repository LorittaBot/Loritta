package net.perfectdreams.loritta.common.emotes

class Emotes(manager: EmoteManager) {
    companion object {
        // Missing emote always uses UnicodeEmote, after all, what if the missing emote is also missing?
        // It is also a static object, because emote managers can use this reference
        val missingEmote = UnicodeEmote("\uD83D\uDC1B")
    }

    val loriHeart = manager.getEmoteByName("lori_heart")
    val loriYay = manager.getEmoteByName("lori_yay")
    val loriHmpf = manager.getEmoteByName("lori_hmpf")
    val loriWow = manager.getEmoteByName("lori_wow")
    val loriRage = manager.getEmoteByName("lori_rage")
    val loriShrug = manager.getEmoteByName("lori_shrug")
    val loriSmile = manager.getEmoteByName("lori_smile")
    val loriPat = manager.getEmoteByName("lori_pat")

    val chinoAyaya = manager.getEmoteByName("chino_ayaya")
}