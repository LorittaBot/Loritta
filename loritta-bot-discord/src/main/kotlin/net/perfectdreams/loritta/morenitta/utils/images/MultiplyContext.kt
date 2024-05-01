package net.perfectdreams.loritta.morenitta.utils.images

import java.awt.CompositeContext
import java.awt.image.Raster
import java.awt.image.WritableRaster

class MultiplyContext : CompositeContext {
    override fun dispose() {
    }

    override fun compose(src: Raster, dstIn: Raster, dstOut: WritableRaster) {
        val width = src.width.coerceAtMost(dstIn.width)
        val height = src.height.coerceAtMost(dstIn.height)

        val srcPixel = IntArray(4)
        val dstPixel = IntArray(4)
        val resultPixel = IntArray(4)

        for (y in 0 until height) {
            for (x in 0 until width) {
                src.getPixel(x, y, srcPixel)
                dstIn.getPixel(x, y, dstPixel)
                for (i in 0..2) {
                    resultPixel[i] = (srcPixel[i] * dstPixel[i]) / 255
                }
                resultPixel[3] = srcPixel[3] // Ignore alpha altogether
                dstOut.setPixel(x, y, resultPixel)
            }
        }
    }
}