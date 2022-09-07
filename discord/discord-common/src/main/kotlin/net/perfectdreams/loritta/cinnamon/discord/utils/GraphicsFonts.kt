package net.perfectdreams.loritta.cinnamon.discord.utils

import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import java.awt.Font

class GraphicsFonts {
    val m5x7 = loadFont("m5x7.ttf")
    val oswaldRegular = loadFont("oswald-regular.ttf")
    val latoRegular = loadFont("lato-regular.ttf")
    val latoBold = loadFont("lato-bold.ttf")
    val latoBlack = loadFont("lato-black.ttf")

    private fun loadFont(name: String) = Font.createFont(Font.TRUETYPE_FONT, LorittaCinnamon::class.java.getResourceAsStream("/fonts/$name"))
}