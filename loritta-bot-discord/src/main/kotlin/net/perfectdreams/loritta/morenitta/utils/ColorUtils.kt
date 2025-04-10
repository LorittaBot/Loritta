package net.perfectdreams.loritta.morenitta.utils

import java.awt.Color

/**
 * Java Code to get a color name from rgb/hex value/awt color
 *
 * The part of looking up a color name from the rgb values is edited from
 * https://gist.github.com/nightlark/6482130#file-gistfile1-java (that has some errors) by Ryan Mast (nightlark)
 *
 * @author Xiaoxiao Li
 */
object ColorUtils {
	private val colorList = ArrayList<ColorName>()

	/**
	 * Initialize the color list that we have.
	 */
	init {
		colorList.add(ColorName("AliceBlue", 0xF0, 0xF8, 0xFF))
		colorList.add(ColorName("AntiqueWhite", 0xFA, 0xEB, 0xD7))
		colorList.add(ColorName("Aqua", 0x00, 0xFF, 0xFF))
		colorList.add(ColorName("Aquamarine", 0x7F, 0xFF, 0xD4))
		colorList.add(ColorName("Azure", 0xF0, 0xFF, 0xFF))
		colorList.add(ColorName("Beige", 0xF5, 0xF5, 0xDC))
		colorList.add(ColorName("Bisque", 0xFF, 0xE4, 0xC4))
		colorList.add(ColorName("Black", 0x00, 0x00, 0x00))
		colorList.add(ColorName("BlanchedAlmond", 0xFF, 0xEB, 0xCD))
		colorList.add(ColorName("Blue", 0x00, 0x00, 0xFF))
		colorList.add(ColorName("BlueViolet", 0x8A, 0x2B, 0xE2))
		colorList.add(ColorName("Brown", 0xA5, 0x2A, 0x2A))
		colorList.add(ColorName("BurlyWood", 0xDE, 0xB8, 0x87))
		colorList.add(ColorName("CadetBlue", 0x5F, 0x9E, 0xA0))
		colorList.add(ColorName("Chartreuse", 0x7F, 0xFF, 0x00))
		colorList.add(ColorName("Chocolate", 0xD2, 0x69, 0x1E))
		colorList.add(ColorName("Coral", 0xFF, 0x7F, 0x50))
		colorList.add(ColorName("CornflowerBlue", 0x64, 0x95, 0xED))
		colorList.add(ColorName("Cornsilk", 0xFF, 0xF8, 0xDC))
		colorList.add(ColorName("Crimson", 0xDC, 0x14, 0x3C))
		colorList.add(ColorName("Cyan", 0x00, 0xFF, 0xFF))
		colorList.add(ColorName("DarkBlue", 0x00, 0x00, 0x8B))
		colorList.add(ColorName("DarkCyan", 0x00, 0x8B, 0x8B))
		colorList.add(ColorName("DarkGoldenRod", 0xB8, 0x86, 0x0B))
		colorList.add(ColorName("DarkGray", 0xA9, 0xA9, 0xA9))
		colorList.add(ColorName("DarkGreen", 0x00, 0x64, 0x00))
		colorList.add(ColorName("DarkKhaki", 0xBD, 0xB7, 0x6B))
		colorList.add(ColorName("DarkMagenta", 0x8B, 0x00, 0x8B))
		colorList.add(ColorName("DarkOliveGreen", 0x55, 0x6B, 0x2F))
		colorList.add(ColorName("DarkOrange", 0xFF, 0x8C, 0x00))
		colorList.add(ColorName("DarkOrchid", 0x99, 0x32, 0xCC))
		colorList.add(ColorName("DarkRed", 0x8B, 0x00, 0x00))
		colorList.add(ColorName("DarkSalmon", 0xE9, 0x96, 0x7A))
		colorList.add(ColorName("DarkSeaGreen", 0x8F, 0xBC, 0x8F))
		colorList.add(ColorName("DarkSlateBlue", 0x48, 0x3D, 0x8B))
		colorList.add(ColorName("DarkSlateGray", 0x2F, 0x4F, 0x4F))
		colorList.add(ColorName("DarkTurquoise", 0x00, 0xCE, 0xD1))
		colorList.add(ColorName("DarkViolet", 0x94, 0x00, 0xD3))
		colorList.add(ColorName("DeepPink", 0xFF, 0x14, 0x93))
		colorList.add(ColorName("DeepSkyBlue", 0x00, 0xBF, 0xFF))
		colorList.add(ColorName("DimGray", 0x69, 0x69, 0x69))
		colorList.add(ColorName("DodgerBlue", 0x1E, 0x90, 0xFF))
		colorList.add(ColorName("FireBrick", 0xB2, 0x22, 0x22))
		colorList.add(ColorName("FloralWhite", 0xFF, 0xFA, 0xF0))
		colorList.add(ColorName("ForestGreen", 0x22, 0x8B, 0x22))
		colorList.add(ColorName("Fuchsia", 0xFF, 0x00, 0xFF))
		colorList.add(ColorName("Gainsboro", 0xDC, 0xDC, 0xDC))
		colorList.add(ColorName("GhostWhite", 0xF8, 0xF8, 0xFF))
		colorList.add(ColorName("Gold", 0xFF, 0xD7, 0x00))
		colorList.add(ColorName("GoldenRod", 0xDA, 0xA5, 0x20))
		colorList.add(ColorName("Gray", 0x80, 0x80, 0x80))
		colorList.add(ColorName("Green", 0x00, 0x80, 0x00))
		colorList.add(ColorName("GreenYellow", 0xAD, 0xFF, 0x2F))
		colorList.add(ColorName("HoneyDew", 0xF0, 0xFF, 0xF0))
		colorList.add(ColorName("HotPink", 0xFF, 0x69, 0xB4))
		colorList.add(ColorName("IndianRed", 0xCD, 0x5C, 0x5C))
		colorList.add(ColorName("Indigo", 0x4B, 0x00, 0x82))
		colorList.add(ColorName("Ivory", 0xFF, 0xFF, 0xF0))
		colorList.add(ColorName("Khaki", 0xF0, 0xE6, 0x8C))
		colorList.add(ColorName("Lavender", 0xE6, 0xE6, 0xFA))
		colorList.add(ColorName("LavenderBlush", 0xFF, 0xF0, 0xF5))
		colorList.add(ColorName("LawnGreen", 0x7C, 0xFC, 0x00))
		colorList.add(ColorName("LemonChiffon", 0xFF, 0xFA, 0xCD))
		colorList.add(ColorName("LightBlue", 0xAD, 0xD8, 0xE6))
		colorList.add(ColorName("LightCoral", 0xF0, 0x80, 0x80))
		colorList.add(ColorName("LightCyan", 0xE0, 0xFF, 0xFF))
		colorList.add(ColorName("LightGoldenRodYellow", 0xFA, 0xFA, 0xD2))
		colorList.add(ColorName("LightGray", 0xD3, 0xD3, 0xD3))
		colorList.add(ColorName("LightGreen", 0x90, 0xEE, 0x90))
		colorList.add(ColorName("LightPink", 0xFF, 0xB6, 0xC1))
		colorList.add(ColorName("LightSalmon", 0xFF, 0xA0, 0x7A))
		colorList.add(ColorName("LightSeaGreen", 0x20, 0xB2, 0xAA))
		colorList.add(ColorName("LightSkyBlue", 0x87, 0xCE, 0xFA))
		colorList.add(ColorName("LightSlateGray", 0x77, 0x88, 0x99))
		colorList.add(ColorName("LightSteelBlue", 0xB0, 0xC4, 0xDE))
		colorList.add(ColorName("LightYellow", 0xFF, 0xFF, 0xE0))
		colorList.add(ColorName("Lime", 0x00, 0xFF, 0x00))
		colorList.add(ColorName("LimeGreen", 0x32, 0xCD, 0x32))
		colorList.add(ColorName("Linen", 0xFA, 0xF0, 0xE6))
		colorList.add(ColorName("Magenta", 0xFF, 0x00, 0xFF))
		colorList.add(ColorName("Maroon", 0x80, 0x00, 0x00))
		colorList.add(ColorName("MediumAquaMarine", 0x66, 0xCD, 0xAA))
		colorList.add(ColorName("MediumBlue", 0x00, 0x00, 0xCD))
		colorList.add(ColorName("MediumOrchid", 0xBA, 0x55, 0xD3))
		colorList.add(ColorName("MediumPurple", 0x93, 0x70, 0xDB))
		colorList.add(ColorName("MediumSeaGreen", 0x3C, 0xB3, 0x71))
		colorList.add(ColorName("MediumSlateBlue", 0x7B, 0x68, 0xEE))
		colorList.add(ColorName("MediumSpringGreen", 0x00, 0xFA, 0x9A))
		colorList.add(ColorName("MediumTurquoise", 0x48, 0xD1, 0xCC))
		colorList.add(ColorName("MediumVioletRed", 0xC7, 0x15, 0x85))
		colorList.add(ColorName("MidnightBlue", 0x19, 0x19, 0x70))
		colorList.add(ColorName("MintCream", 0xF5, 0xFF, 0xFA))
		colorList.add(ColorName("MistyRose", 0xFF, 0xE4, 0xE1))
		colorList.add(ColorName("Moccasin", 0xFF, 0xE4, 0xB5))
		colorList.add(ColorName("NavajoWhite", 0xFF, 0xDE, 0xAD))
		colorList.add(ColorName("Navy", 0x00, 0x00, 0x80))
		colorList.add(ColorName("OldLace", 0xFD, 0xF5, 0xE6))
		colorList.add(ColorName("Olive", 0x80, 0x80, 0x00))
		colorList.add(ColorName("OliveDrab", 0x6B, 0x8E, 0x23))
		colorList.add(ColorName("Orange", 0xFF, 0xA5, 0x00))
		colorList.add(ColorName("OrangeRed", 0xFF, 0x45, 0x00))
		colorList.add(ColorName("Orchid", 0xDA, 0x70, 0xD6))
		colorList.add(ColorName("PaleGoldenRod", 0xEE, 0xE8, 0xAA))
		colorList.add(ColorName("PaleGreen", 0x98, 0xFB, 0x98))
		colorList.add(ColorName("PaleTurquoise", 0xAF, 0xEE, 0xEE))
		colorList.add(ColorName("PaleVioletRed", 0xDB, 0x70, 0x93))
		colorList.add(ColorName("PapayaWhip", 0xFF, 0xEF, 0xD5))
		colorList.add(ColorName("PeachPuff", 0xFF, 0xDA, 0xB9))
		colorList.add(ColorName("Peru", 0xCD, 0x85, 0x3F))
		colorList.add(ColorName("Pink", 0xFF, 0xC0, 0xCB))
		colorList.add(ColorName("Plum", 0xDD, 0xA0, 0xDD))
		colorList.add(ColorName("PowderBlue", 0xB0, 0xE0, 0xE6))
		colorList.add(ColorName("Purple", 0x80, 0x00, 0x80))
		colorList.add(ColorName("Red", 0xFF, 0x00, 0x00))
		colorList.add(ColorName("RosyBrown", 0xBC, 0x8F, 0x8F))
		colorList.add(ColorName("RoyalBlue", 0x41, 0x69, 0xE1))
		colorList.add(ColorName("SaddleBrown", 0x8B, 0x45, 0x13))
		colorList.add(ColorName("Salmon", 0xFA, 0x80, 0x72))
		colorList.add(ColorName("SandyBrown", 0xF4, 0xA4, 0x60))
		colorList.add(ColorName("SeaGreen", 0x2E, 0x8B, 0x57))
		colorList.add(ColorName("SeaShell", 0xFF, 0xF5, 0xEE))
		colorList.add(ColorName("Sienna", 0xA0, 0x52, 0x2D))
		colorList.add(ColorName("Silver", 0xC0, 0xC0, 0xC0))
		colorList.add(ColorName("SkyBlue", 0x87, 0xCE, 0xEB))
		colorList.add(ColorName("SlateBlue", 0x6A, 0x5A, 0xCD))
		colorList.add(ColorName("SlateGray", 0x70, 0x80, 0x90))
		colorList.add(ColorName("Snow", 0xFF, 0xFA, 0xFA))
		colorList.add(ColorName("SpringGreen", 0x00, 0xFF, 0x7F))
		colorList.add(ColorName("SteelBlue", 0x46, 0x82, 0xB4))
		colorList.add(ColorName("Tan", 0xD2, 0xB4, 0x8C))
		colorList.add(ColorName("Teal", 0x00, 0x80, 0x80))
		colorList.add(ColorName("Thistle", 0xD8, 0xBF, 0xD8))
		colorList.add(ColorName("Tomato", 0xFF, 0x63, 0x47))
		colorList.add(ColorName("Turquoise", 0x40, 0xE0, 0xD0))
		colorList.add(ColorName("Violet", 0xEE, 0x82, 0xEE))
		colorList.add(ColorName("Wheat", 0xF5, 0xDE, 0xB3))
		colorList.add(ColorName("White", 0xFF, 0xFF, 0xFF))
		colorList.add(ColorName("WhiteSmoke", 0xF5, 0xF5, 0xF5))
		colorList.add(ColorName("Yellow", 0xFF, 0xFF, 0x00))
		colorList.add(ColorName("YellowGreen", 0x9A, 0xCD, 0x32))
	}

	/**
	 * Get the closest color name from our list
	 *
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	fun getColorNameFromRgb(r: Int, g: Int, b: Int): String {
		var closestMatch: ColorName? = null
		var minMSE = Integer.MAX_VALUE
		var mse: Int
		for (c in this.colorList) {
			mse = c.computeMSE(r, g, b)
			if (mse < minMSE) {
				minMSE = mse
				closestMatch = c
			}
		}

		return if (closestMatch != null) {
			closestMatch.name
		} else {
			"No matched color name."
		}
	}

	/**
	 * Convert hexColor to rgb, then call getColorNameFromRgb(r, g, b)
	 *
	 * @param hexColor
	 * @return
	 */
	fun getColorNameFromHex(hexColor: Int): String {
		val r = hexColor and 0xFF0000 shr 16
		val g = hexColor and 0xFF00 shr 8
		val b = hexColor and 0xFF
		return getColorNameFromRgb(r, g, b)
	}

	fun colorToHex(c: Color): Int {
		return Integer.decode("0x" + Integer.toHexString(c.rgb).substring(2))
	}

	fun getColorNameFromColor(color: Color): String {
		return getColorNameFromRgb(color.red, color.green, color.blue)
	}

	/**
	 * SubClass of ColorUtils. In order to lookup color name
	 *
	 * @author Xiaoxiao Li
	 */
	class ColorName(val name: String, val r: Int, val g: Int, val b: Int) {
		fun computeMSE(pixR: Int, pixG: Int, pixB: Int): Int {
			return (((pixR - r) * (pixR - r) + (pixG - g) * (pixG - g) + (pixB - b) * (pixB - b)) / 3).toInt()
		}
	}
}