package net.perfectdreams.loritta.website.utils

import com.mrpowergamerbr.loritta.utils.toBufferedImage
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
    val original = File("C:\\Users\\Leonardo\\Pictures\\imgset_test\\lori_support\\lori_support.png")
    val originalImage = ImageIO.read(original)

    repeat(10) {
        val newWidth = originalImage.width - (100 * (it + 1))
        val newHeight = (originalImage.height * newWidth) / originalImage.width

        val target = originalImage.getScaledInstance(
                newWidth,
                newHeight,
                BufferedImage.SCALE_SMOOTH
        )

        val newFile = File(original.parentFile, "lori_support_${newWidth}w.png")

        ImageIO.write(target.toBufferedImage(), "png", newFile)
    }
}