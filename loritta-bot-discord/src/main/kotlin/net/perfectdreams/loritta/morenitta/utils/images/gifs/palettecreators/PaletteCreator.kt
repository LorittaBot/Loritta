package net.perfectdreams.loritta.morenitta.utils.images.gifs.palettecreators

import java.awt.Color

interface PaletteCreator {
    fun createPaletteFromPixels(pixels: ByteArray, indexedPixels: ByteArray, transparent: Color?, hasTransparentPixels: Boolean): PaletteCreatorResult
}