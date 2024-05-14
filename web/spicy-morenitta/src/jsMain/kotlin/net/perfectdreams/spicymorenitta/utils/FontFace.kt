package net.perfectdreams.spicymorenitta.utils

import kotlin.js.Promise

external class FontFace(family: String, src: String) {  // todo: add second constructor
    fun load(): Promise<FontFace>

    // todo: add other methods
}