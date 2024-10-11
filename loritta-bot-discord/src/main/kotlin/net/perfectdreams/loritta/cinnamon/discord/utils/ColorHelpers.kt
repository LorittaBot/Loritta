package net.perfectdreams.loritta.cinnamon.discord.utils

import net.perfectdreams.loritta.common.utils.Color
import java.awt.Color as JavaColor

fun Color.toJavaColor() = JavaColor(this.rgb, false)