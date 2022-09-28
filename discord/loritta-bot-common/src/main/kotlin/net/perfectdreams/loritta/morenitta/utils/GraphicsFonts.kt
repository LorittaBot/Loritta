package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.morenitta.LorittaBot
import java.awt.Font

class GraphicsFonts {
    val m5x7 = loadFont("m5x7.ttf")
    val oswaldRegular = loadFont("oswald-regular.ttf")
    val latoRegular = loadFont("lato-regular.ttf")
    val latoBold = loadFont("lato-bold.ttf")
    val latoBlack = loadFont("lato-black.ttf")
    val bebasNeueRegular = loadFont("bebas-neue-regular.ttf")
    val komikaHand = loadFont("komika.ttf")

    private fun loadFont(name: String) = Font.createFont(Font.TRUETYPE_FONT, LorittaBot::class.java.getResourceAsStream("/fonts/$name"))
}