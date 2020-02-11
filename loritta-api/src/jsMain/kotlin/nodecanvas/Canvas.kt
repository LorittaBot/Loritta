@file:JsModule("canvas")
@file:JsNonModule

package nodecanvas

import net.perfectdreams.loritta.api.utils.image.Canvas
import kotlin.js.Promise

external fun createCanvas(width: Int, height: Int): Canvas
external fun loadImage(path: String): Promise<dynamic>