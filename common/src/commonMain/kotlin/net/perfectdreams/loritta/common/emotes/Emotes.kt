package net.perfectdreams.loritta.common.emotes

class Emotes(manager: EmoteManager) {
    companion object {
        // Missing emote always uses UnicodeEmote, after all, what if the missing emote is also missing?
        // It is also a static object, because emote managers can use this reference
        val missingEmote = UnicodeEmote("\uD83D\uDC1B")
        // Same as above, this is referenced for the Loritta-styled reply code
        val defaultStyledPrefix = UnicodeEmote("\uD83D\uDD39")
    }

    val loriHeart = manager.getEmoteByName("lori_heart")
    val loriYay = manager.getEmoteByName("lori_yay")
    val loriHm = manager.getEmoteByName("lori_hm")
    val loriHmpf = manager.getEmoteByName("lori_hmpf")
    val loriWow = manager.getEmoteByName("lori_wow")
    val loriRage = manager.getEmoteByName("lori_rage")
    val loriShrug = manager.getEmoteByName("lori_shrug")
    val loriSmile = manager.getEmoteByName("lori_smile")
    val loriPat = manager.getEmoteByName("lori_pat")
    val loriSob = manager.getEmoteByName("lori_sob")
    val loriRich = manager.getEmoteByName("lori_rich")
    val loriOkHand = manager.getEmoteByName("lori_ok_hand")

    val error = manager.getEmoteByName("error")

    val vinDiesel = manager.getEmoteByName("vin_diesel")

    val coinHeads = manager.getEmoteByName("coin_heads")
    val coinTails = manager.getEmoteByName("coin_tails")

    val chinoAyaya = manager.getEmoteByName("chino_ayaya")

    val tada = manager.getEmoteByName("tada")
    val whiteFlag = manager.getEmoteByName("white_flag")
    val blackFlag = manager.getEmoteByName("black_flag")
    val newspaper = manager.getEmoteByName("newspaper")
    val scissors = manager.getEmoteByName("scissors")
    val rock = manager.getEmoteByName("rock")
    val jesus = manager.getEmoteByName("jesus")
    val thinking = manager.getEmoteByName("thinking")
    val shrug = manager.getEmoteByName("shrug")

    val radio = manager.getEmoteByName("radio")
    val handPointLeft = manager.getEmoteByName("hand_point_left")
    val handPointRight = manager.getEmoteByName("hand_point_right")
}