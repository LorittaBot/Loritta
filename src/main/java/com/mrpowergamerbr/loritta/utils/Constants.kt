package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import java.awt.Color
import java.awt.Font
import java.io.File
import java.io.FileInputStream

/**
 * Constantes
 */
object Constants {
	const val ERROR = "<:error:412585701054611458>"
	const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0"
	const val LEFT_PADDING = "\uD83D\uDD39"
	val INDEXES = listOf("1⃣",
			"2⃣",
			"3⃣",
			"4⃣",
			"5⃣",
			"6⃣",
			"7⃣",
			"8⃣",
			"9⃣")

	// ===[ COLORS ]===
	val DISCORD_BURPLE = Color(114, 137, 218)
	val LORITTA_AQUA = Color(0, 193, 223)
	val ROBLOX_RED = Color(226, 35, 26)

	// Palavras inapropariadas
	val BAD_NICKNAME_WORDS = listOf(
			"puta",
			"vagabunda",
			"lixo",
			"desgraça",
			"burra",
			"piranha",
			"protistuta",
			"bicha",
			"bixa",
			"arromabada",
			"cachorra",
			"ruim",
			"boquete",
			"boqueteira",
			"putona"
	)

	// ===[ FONTS ]===
	val OSWALD_REGULAR: Font by lazy {
		FileInputStream(File(Loritta.ASSETS + "oswald_regular.ttf")).use {
			Font.createFont(java.awt.Font.TRUETYPE_FONT, it)
		}
	}

	val MINECRAFTIA: Font by lazy {
		FileInputStream(File(Loritta.ASSETS + "minecraftia.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
	}

	val DOTUMCHE: Font by lazy {
		FileInputStream(File(Loritta.ASSETS + "dotumche.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
	}

	val DETERMINATION_MONO: Font by lazy {
		FileInputStream(File(Loritta.ASSETS + "DTM-Mono.otf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
	}

	val VOLTER: Font by lazy {
		FileInputStream(File(Loritta.ASSETS + "Volter__28Goldfish_29.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
	}
}