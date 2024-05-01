package net.perfectdreams.loritta.morenitta.utils.images

import java.awt.Composite
import java.awt.CompositeContext
import java.awt.RenderingHints
import java.awt.image.ColorModel

internal class MultiplyComposite : Composite {
    override fun createContext(
        srcColorModel: ColorModel,
        dstColorModel: ColorModel,
        hints: RenderingHints
    ): CompositeContext {
        return MultiplyContext()
    }
}