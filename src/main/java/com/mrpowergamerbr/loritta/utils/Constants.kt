package com.mrpowergamerbr.loritta.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandContext
import org.yaml.snakeyaml.Yaml
import java.awt.Color
import java.awt.Font
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import javax.imageio.ImageIO

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

	// Folder names used for the action commands
	const val ACTION_BOTH = "both"
	const val ACTION_FEMALE_AND_FEMALE = "female_x_female"
	const val ACTION_FEMALE_AND_MALE = "female_x_male"
	const val ACTION_GENERIC = "generic"
	const val ACTION_MALE_AND_FEMALE = "male_x_female"
	const val ACTION_MALE_AND_MALE = "male_x_male"

	val MAPPER = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))
	val YAML = Yaml()

	const val PORTUGUESE_SUPPORT_GUILD_ID = "297732013006389252"
	const val ENGLISH_SUPPORT_GUILD_ID = "420626099257475072"
	const val SPARKLYPOWER_GUILD_ID = "320248230917046282"
	const val LORI_STICKERS_ROLE_ID = "510788363264196638"

	const val MAX_TRACKS_ON_PLAYLIST = 25

	// ===[ COLORS ]===
	val DISCORD_BLURPLE = Color(114, 137, 218)
	val LORITTA_AQUA = Color(0, 193, 223)
	val ROBLOX_RED = Color(226, 35, 26)
	val IMAGE_FALLBACK by lazy { ImageIO.read(File(Loritta.ASSETS, "avatar0.png")) }
	val URL_PATTERN = Pattern.compile("[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,7}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)")
	val HTTP_URL_PATTERN = Pattern.compile("https?:\\/\\/[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,7}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)")
	val EMOJI_PATTERN = Pattern.compile("(?:[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83E\uDD00-\uD83E\uDDFF]|" +
			"[\uD83D\uDE00-\uD83D\uDE4F]|[\uD83D\uDE80-\uD83D\uDEFF]|" +
			"[\u2600-\u26FF]\uFE0F?|[\u2700-\u27BF]\uFE0F?|\u24C2\uFE0F?|" +
			"[\uD83C\uDDE6-\uD83C\uDDFF]{1,2}|" +
			"[\uD83C\uDD70\uD83C\uDD71\uD83C\uDD7E\uD83C\uDD7F\uD83C\uDD8E\uD83C\uDD91-\uD83C\uDD9A]\uFE0F?|" +
			"[\u0023\u002A\u0030-\u0039]\uFE0F?\u20E3|[\u2194-\u2199\u21A9-\u21AA]\uFE0F?|[\u2B05-\u2B07\u2B1B\u2B1C\u2B50\u2B55]\uFE0F?|" +
			"[\u2934\u2935]\uFE0F?|[\u3030\u303D]\uFE0F?|[\u3297\u3299]\uFE0F?|" +
			"[\uD83C\uDE01\uD83C\uDE02\uD83C\uDE1A\uD83C\uDE2F\uD83C\uDE32-\uD83C\uDE3A\uD83C\uDE50\uD83C\uDE51]\uFE0F?|" +
			"[\u203C\u2049]\uFE0F?|[\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE]\uFE0F?|" +
			"[\u00A9\u00AE]\uFE0F?|[\u2122\u2139]\uFE0F?|\uD83C\uDC04\uFE0F?|\uD83C\uDCCF\uFE0F?|" +
			"[\u231A\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA]\uFE0F?)+")

	val REPEATING_CHARACTERS_REGEX = Regex("(.)\\1+")

	val YOUTUBE_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
	val ASSETS_FOLDER by lazy { File(Loritta.ASSETS) }

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
			"putona",
			"viada",
			"vadia",
			"putiane",
			"fdp"
	)

	// Canais de textos utilizados na Loritta
	const val RELAY_YOUTUBE_VIDEOS_CHANNEL = "509043859792068609"

	/**
	 * Used in conjuction with the elvis operation ("?:") plus a "return;" when the image is null, this allows the user to receive feedback if the image
	 * is valid or, if he doesn't provide any arguments to the command, explain how the command works.
	 *
	 * ```
	 * context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }
	 * ```
	 */
	val INVALID_IMAGE_REPLY: suspend ((CommandContext) -> Unit) = { context ->
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