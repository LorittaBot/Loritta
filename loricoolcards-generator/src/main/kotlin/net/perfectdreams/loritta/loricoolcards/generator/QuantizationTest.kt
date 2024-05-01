package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.morenitta.loricoolcards.LoriCoolCardsManager
import net.perfectdreams.loritta.morenitta.utils.GraphicsFonts
import net.perfectdreams.loritta.morenitta.utils.ImageUtils
import java.io.File
import javax.imageio.ImageIO

fun main() {
    // Speeds up image loading/writing/etc
    // https://stackoverflow.com/a/44170254/7271796
    ImageIO.setUseCache(false)

    val fonts = GraphicsFonts()
    val loriCoolCardsManager = LoriCoolCardsManager(fonts)
    val image = ImageIO.read(File("D:\\Pictures\\Loritta\\LoriCoolCards\\pages\\test_page.png"))

    val indexedImage = ImageUtils.convertToIndexedImage(image = image, loriCoolCardsManager.createStickerReceivedIndexColorModel())

    ImageIO.write(indexedImage, "png", File("D:\\Pictures\\Loritta\\LoriCoolCards\\pages\\test_page_indexed.png"))
}