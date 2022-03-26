package net.perfectdreams.loritta.cinnamon.platform.utils

import net.perfectdreams.loritta.cinnamon.common.utils.Color
import dev.kord.common.Color as KordColor
import java.awt.Color as JavaColor

fun Color.toJavaColor() = JavaColor(this.rgb, true)
fun Color.toKordColor() = KordColor(this.rgb)