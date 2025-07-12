package net.perfectdreams.loritta.morenitta.utils

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.serialization.hocon.Hocon
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import java.awt.Color
import java.awt.Font
import java.io.File
import java.io.FileInputStream
import java.time.ZoneId
import java.util.regex.Pattern
import javax.imageio.ImageIO

/**
 * Constantes
 */
object Constants {
	const val ERROR = "<:error:412585701054611458>"
	const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0"
	const val ONE_MINUTE_IN_MILLISECONDS = 60_000L
	const val ONE_HOUR_IN_MILLISECONDS = 3_600_000L
	const val ONE_DAY_IN_MILLISECONDS = 86_400_000L
	const val SEVEN_DAYS_IN_MILLISECONDS = ONE_DAY_IN_MILLISECONDS * 7
	const val ONE_WEEK_IN_MILLISECONDS = 604_800_000L
	const val ONE_MONTH_IN_MILLISECONDS = 2_592_000_000L
	const val TWO_MONTHS_IN_MILLISECONDS = ONE_MONTH_IN_MILLISECONDS * 2
	const val SIX_MONTHS_IN_MILLISECONDS = ONE_MONTH_IN_MILLISECONDS * 8
	const val DELAY_CUT_OFF = SIX_MONTHS_IN_MILLISECONDS // six months
	const val CLUSTER_USER_AGENT = "Loritta Cluster %s (%s)"
	const val CANARY_CLUSTER_USER_AGENT = "Canary Cluster %s (%s)"
	val DEFAULT_DISCORD_BLUE_AVATAR by lazy { ImageIO.read(LorittaBot::class.java.getResourceAsStream("/avatars/0.png")) }

	/**
	 * A 512x512 image used for icon (especially server icons) fallbacks
	 */
	val MISSING_DISCORD_ICON_FALLBACK_IMAGE by lazy { ImageIO.read(LorittaBot::class.java.getResourceAsStream("/fallback_icon.png")) }

	/**
	 * Discord's URL Crawler User Agent
	 */
	const val DISCORD_CRAWLER_USER_AGENT = "Mozilla/5.0 (compatible; Discordbot/2.0; +https://discordapp.com)"

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

	val JSON_MAPPER = ObjectMapper()
	val HOCON = Hocon { useArrayPolymorphism = true }

	const val PORTUGUESE_SUPPORT_GUILD_ID = 297732013006389252L
	const val ENGLISH_SUPPORT_GUILD_ID = 420626099257475072L
	const val SPARKLYPOWER_GUILD_ID = 320248230917046282L
	const val LORI_STICKERS_ROLE_ID = 510788363264196638L
	const val THANK_YOU_DONATORS_CHANNEL_ID = 536171041546960916L

	const val DEFAULT_LOCALE_ID = "default"

	const val DONATION_ACTIVE_MILLIS = 2_764_800_000 // 32 dias

	// ===[ COLORS ]===
	val DISCORD_BLURPLE = Color(84, 105, 236)
	val LORITTA_AQUA = Color(41, 166, 254)
	val ROBLOX_RED = Color(226, 35, 26)
	val URL_PATTERN = Pattern.compile("[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[A-z]{2,7}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)")
	val URL_WITH_OPTIONAL_HTTP_PATTERN = Pattern.compile("(https?:\\/\\/)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[A-z]{2,7}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)")
	val HTTP_URL_PATTERN = Pattern.compile("https?:\\/\\/[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[A-z]{2,7}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)")
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
	val WHITE_SPACE_MULTIPLE_REGEX = Regex(" +")
	val TWITCH_USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9][\\w]{3,24}\$")
	val DISCORD_EMOTE_PATTERN = Pattern.compile("<a?:([A-z0-9_]+):([0-9]+)>")
	val DISCORD_INVITE_PATTERN = Pattern.compile(".*(discord\\.gg|(?:discordapp.com|discord.com)(?:/invite))/([A-z0-9]+).*", Pattern.CASE_INSENSITIVE)

	val ASSETS_FOLDER by lazy { File(LorittaBot.ASSETS) }

	// TODO: Don't hardcode this
	val INVALID_IMAGE_URL = "https://loritta.website/assets/img/oopsie_woopsie_invalid_image.png"

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
			"fdp",
			"xereca",
			"pepeca",
			"pepeka",
			"ppk",
			"escrava sexual",
			"xota",
			"xoxota",
			"xoxotinha"
	)

	// Canais de textos utilizados na Loritta
	const val RELAY_YOUTUBE_VIDEOS_CHANNEL = "509043859792068609"
	const val RELAY_TWITCH_STREAMS_CHANNEL = "520354012021784586"

	/**
	 * Timezone used by Loritta, Brazil's timezone
	 *
	 * In the future this should be configurable by the server admin or by the user
	 */
	val LORITTA_TIMEZONE = ZoneId.of("America/Sao_Paulo")

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
                    LorittaReply(
                            message = context.locale["commands.noValidImageFound", Emotes.LORI_CRYING.toString()],
                            prefix = ERROR
                    )
			)
		}
	}

	// ===[ FONTS ]===
	val OSWALD_REGULAR: Font by lazy {
		FileInputStream(File(LorittaBot.ASSETS + "oswald_regular.ttf")).use {
			Font.createFont(java.awt.Font.TRUETYPE_FONT, it)
		}
	}

	val MINECRAFTIA: Font by lazy {
		FileInputStream(File(LorittaBot.ASSETS + "minecraftia.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
	}

	val DOTUMCHE: Font by lazy {
		FileInputStream(File(LorittaBot.ASSETS + "dotumche.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
	}

	val DETERMINATION_MONO: Font by lazy {
		FileInputStream(File(LorittaBot.ASSETS + "DTM-Mono.otf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
	}

	val VOLTER: Font by lazy {
		FileInputStream(File(LorittaBot.ASSETS + "Volter__28Goldfish_29.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
	}

	val JACKEY: Font by lazy {
		FileInputStream(File(LorittaBot.ASSETS + "jackeyfont.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
	}
}
