package net.perfectdreams.loritta.plugin.rosbife.utils

import java.awt.Graphics
import java.awt.Color
import java.awt.image.BufferedImage

class GraphicsUtils {
    fun drawTextCentralizedNewLines(graphics: Graphics, image: BufferedImage, text: String, startAtX: Int, startAtY: Int) : Boolean {
        var startAtX = startAtX
        var startAtY = startAtY
    
        val splitInput1 = text.split("((?<= )|(?= ))".toRegex()).dropLastWhile { it.isEmpty() }
        var input1FitInLine = ""
    
        for (split in splitInput1) {
            val old = input1FitInLine
    
            input1FitInLine += split
    
            println("${startAtX - (graphics.getFontMetrics(graphics.font).stringWidth(old) / 2)}")
            if (0 >= startAtX - (graphics.getFontMetrics(graphics.font).stringWidth(input1FitInLine) / 2) || startAtX + (graphics.getFontMetrics(graphics.font).stringWidth(input1FitInLine) / 2) >= image.width) {
                println((graphics.getFontMetrics(graphics.font).stringWidth(old)))
    
                val drawAtX = startAtX - (graphics.getFontMetrics(graphics.font).stringWidth(old) / 2)
                this.drawStringWithOutline(graphics, old, drawAtX.toInt(), startAtY.toInt(), 2)
                startAtY += 26
                input1FitInLine = ""
                input1FitInLine += split
            }
        }
    
        val drawAtX = startAtX - (graphics.getFontMetrics(graphics.font).stringWidth(input1FitInLine) / 2)
        this.drawStringWithOutline(graphics, input1FitInLine, drawAtX, startAtY, 2)

        return true
    }
    
    fun drawStringWithOutline(graphics: Graphics, text: String, x: Int, y: Int, power: Int) : Boolean {
        graphics.color = Color.BLACK
        for (powerX in -power..power) {
            for (powerY in -power..power) {
                graphics.drawString(text, x + powerX, y + powerY)
            }
        }
    
        graphics.color = Color(255, 251, 0)
        graphics.drawString(text, x, y)

        return true
    }
}