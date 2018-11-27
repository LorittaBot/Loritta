package com.mrpowergamerbr.loritta.utils.counter

import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {
	val source = File("C:\\Users\\Whistler\\Pictures\\Loritta\\counter_color\\source")
	source.listFiles().forEach {
		val image = ImageIO.read(it)
		val graphics = image.graphics
		for (x in 0 until image.width) {
			for (y in 0 until image.height) {
				val rgb = image.getRGB(x, y)
				val color = Color(rgb, true)
				val vals = FloatArray(3)
				Color.RGBtoHSB(color.red, color.green, color.blue, vals)
				var hue = vals[0] * 360
				var saturation = vals[1] * 100
				var brightness = vals[2] * 100

				if (hue in 200f..210f) {
					// println("Changing hue!")
					hue = (hue - 90)
					saturation += 20
					// brightness = brightness + 10
				}

				var newColor = Color.getHSBColor((hue / 360), saturation / 100, brightness / 100)
				newColor = Color(newColor.red, newColor.green, newColor.blue, color.alpha)
				image.setRGB(x, y, newColor.rgb)
			}
		}

		ImageIO.write(image, "png", File("C:\\Users\\Whistler\\Pictures\\Loritta\\counter_color\\green\\green_${it.name}"))
	}
}