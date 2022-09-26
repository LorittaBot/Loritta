package net.perfectdreams.loritta.common.utils.embed

class LorittaColor(val rgb: Int) {
    companion object {
        val DISCORD_BLURPLE = LorittaColor(114, 137, 218)
        val LORITTA_AQUA = LorittaColor(26, 160, 254)
        val ROBLOX_RED = LorittaColor(226, 35, 26)
    }

    constructor(r: Int, g: Int, b: Int) : this(
            rgb = 255 and 0xFF shl 24 or
                    (r and 0xFF shl 16) or
                    (g and 0xFF shl 8) or
                    (b and 0xFF shl 0)
        )
}