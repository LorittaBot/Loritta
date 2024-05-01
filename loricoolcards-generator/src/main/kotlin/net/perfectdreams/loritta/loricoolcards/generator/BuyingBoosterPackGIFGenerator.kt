package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.morenitta.loricoolcards.LoriCoolCardsManager
import net.perfectdreams.loritta.morenitta.utils.GraphicsFonts
import java.io.File
import javax.imageio.ImageIO

fun main() {
    // Speeds up image loading/writing/etc
    // https://stackoverflow.com/a/44170254/7271796
    ImageIO.setUseCache(false)

    val manager = LoriCoolCardsManager(GraphicsFonts())

    val result = manager.generateBuyingBoosterPackGIF()

    File("D:\\Pictures\\Loritta\\LoriCoolCards\\buying_package_v4.gif")
        .writeBytes(result)
}