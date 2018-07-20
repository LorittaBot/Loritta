package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandContext
import java.awt.Color
import java.awt.Font
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat

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
	val DISCORD_BLURPLE = Color(114, 137, 218)
	val LORITTA_AQUA = Color(0, 193, 223)
	val ROBLOX_RED = Color(226, 35, 26)

	val YOUTUBE_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

	val INVALID_IMAGE_URL: String by lazy {
		Loritta.config.websiteUrl + "assets/img/oopsie_woopsie_invalid_image.png"
	}

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

	/**
	 * Used in conjuction with the elvis operation ("?:") plus a "return;" when the image is null, this allows the user to receive feedback if the image
	 * is valid or, if he doesn't provide any arguments to the command, explain how the command works.
	 *
	 * ```
	 * context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
	 * ```
	 */
	val INVALID_IMAGE_REPLY: ((CommandContext) -> Unit) = { context ->
		if (context.rawArgs.isEmpty()) {
			context.explain()
		} else {
			context.reply(
					LoriReply(
							message = context.locale["NO_VALID_IMAGE"],
							prefix = Constants.ERROR
					)
			)
		}
	}

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

	val JACKEY: Font by lazy {
		FileInputStream(File(Loritta.ASSETS + "jackeyfont.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
	}
}