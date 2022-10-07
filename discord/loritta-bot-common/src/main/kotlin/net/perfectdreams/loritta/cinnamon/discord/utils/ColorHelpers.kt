package net.perfectdreams.loritta.cinnamon.discord.utils

import net.perfectdreams.loritta.common.utils.Color
import dev.kord.common.Color as KordColor
import java.awt.Color as JavaColor

fun Color.toJavaColor() = JavaColor(this.rgb, false)
fun Color.toKordColor() = KordColor(this.rgb)

fun java.awt.Color.toKordColor() = KordColor(this.rgb)