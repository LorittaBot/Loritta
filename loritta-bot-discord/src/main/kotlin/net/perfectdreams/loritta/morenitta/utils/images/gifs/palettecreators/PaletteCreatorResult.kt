package net.perfectdreams.loritta.morenitta.utils.images.gifs.palettecreators

data class PaletteCreatorResult(
    val colorTab: ByteArray,
    val colorDepth: Int,
    val palSize: Int,
    val transIndex: Int
)