package net.perfectdreams.loritta.common.utils.embed

sealed class LorittaColor(val rgb: Int)  {
    constructor(r: Int, g: Int, b: Int) : this(
            rgb = 255 and 0xFF shl 24 or
                    (r and 0xFF shl 16) or
                    (g and 0xFF shl 8) or
                    (b and 0xFF shl 0)
        )

    object DISCORD_BLURPLE: LorittaColor(114, 137, 218)
    object LORITTA_AQUA: LorittaColor(26, 160, 254)
    object ROBLOX_RED: LorittaColor(226, 35, 26)
}