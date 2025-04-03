package net.perfectdreams.loritta.morenitta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.google.gson.JsonParser
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import org.apache.commons.lang3.StringUtils
import java.awt.Color
import java.net.URLEncoder
import net.perfectdreams.loritta.morenitta.LorittaBot

class WikipediaCommand(loritta: LorittaBot) : AbstractCommand(loritta, "wikipedia", listOf("wiki"), net.perfectdreams.loritta.common.commands.CommandCategory.UTILS) {
	companion object {
		// https://en.wikipedia.org/wiki/List_of_Wikipedias
		private val VALID_WIKIPEDIAS = listOf(
				"en", "fr", "de", "es", "ja", "ru", "it", "zh", "pt", "ar", "fa", "pl", "nl", "id", "uk", "he", "sv", "cs", "ko", "vi", "ca", "no", "fi", "hu", "tr", "ro", "el", "th", "hi", "bn", "az", "simple", "ceb", "sw", "kk", "da", "eo", "sr", "lt", "sk", "bg", "sl", "eu", "et", "hr", "ms", "arz", "ur", "ta", "te", "nn", "gl", "af", "bs", "be", "ml", "ka", "is", "sq", "uz", "la", "mk", "lv", "azb", "mr", "sh", "tl", "cy", "sco", "ku", "ckb", "ast", "ba", "be-tarask", "zh-yue", "als", "ga", "hy", "pa", "my", "kn", "mn", "war", "zh-min-nan", "vo", "min", "lmo", "ht", "lb", "br", "gu", "tg", "new", "bpy", "nds", "io", "pms", "su", "oc", "jv", "nap", "scn", "wa", "bar", "an", "ksh", "szl", "fy", "frr", "ia", "yi", "mg", "gd", "vec", "ce", "sa", "mai", "xmf", "sd", "wuu", "as", "mrj", "mhr", "km", "roa-tara", "am", "roa-rup", "map-bms", "bh", "mnw", "shn", "bcl", "co", "cv", "dv", "nds-nl", "fo", "hif", "fur", "gan", "glk", "gu", "hak", "ilo", "pam", "csb", "avk", "lij", "li", "gv", "mi", "mt", "nah", "ne", "nrm", "se", "nov", "qu", "os", "pi", "pag", "ps", "pdc", "rm", "bat-smg", "sc", "si", "tt", "tk", "hsb", "fiu-vro", "vls", "yo", "diq", "zh-classical", "frp", "lad", "kw", "haw", "ang", "ln", "ie", "wo", "crh", "nv", "jbo", "ay", "pcd", "zea", "eml", "ky", "ig", "or", "cbk-zam", "kg", "arc", "rmy", "ab", "gn", "so", "kab", "ug", "stq", "ha", "udm", "ext", "mzn", "pap", "cu", "sah", "tet", "sn", "lo", "pnb", "iu", "na", "got", "bo", "dsb", "chr", "cdo", "om", "sm", "ee", "av", "bm", "zu", "cr", "pih", "ss", "bi", "rw", "ch", "xh", "kl", "ik", "bug", "ts", "kv", "xal", "st", "tw", "bxr", "ak", "ny", "lbe", "za", "ks", "ff", "lg", "chy", "mwl", "lez", "bjn", "gom", "lrc", "tyv", "vep", "nso", "kbd", "ltg", "rue", "pfl", "gag", "koi", "ace", "olo", "kaa", "mdf", "myv", "ady", "tcy", "dty", "atj", "kbp", "din", "lfn", "gor", "inh", "sat", "hyw", "nqo", "ban", "szy", "gcr", "ary", "lld", "smn", "to", "tpi", "ty", "ti", "pnt", "ve", "dz", "tn", "tum", "fj", "ki", "sg", "rn", "krc", "srn", "jam", "awa", "nostalgia",
		)
	}
	override fun getDescriptionKey() = LocaleKeyData("commands.command.wikipedia.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.wikipedia.examples")
	// TODO: Fix Usage
	// TODO: Fix Detailed Usage

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			var languageId = when (context.config.localeId) {
				"default" -> "pt"
				"pt-pt" -> "pt"
				"pt-funk" -> "pt"
				else -> "en"
			}

			val inputLanguageId = context.args[0]
			var hasValidLanguageId = false

			if (inputLanguageId.startsWith("[") && inputLanguageId.endsWith("]")) {
				languageId = inputLanguageId.substring(1, inputLanguageId.length - 1)
						.lowercase()

				if (languageId !in VALID_WIKIPEDIAS) {
					context.reply(
							LorittaReply(
									locale[
											"commands.command.wikipedia.invalidLanguage",
											VALID_WIKIPEDIAS.joinToString(", ", transform = { "`$it`" })
									],
									Constants.ERROR
							)
					)
					return
				}

				hasValidLanguageId = true
			}

			try {
				val query = StringUtils.join(context.args, " ", if (hasValidLanguageId) 1 else 0, context.args.size)
				val wikipediaResponse = HttpRequest.get("https://" + languageId + ".wikipedia.org/w/api.php?format=json&action=query&prop=extracts|info&inprop=url&redirects=1&exintro=&explaintext=&titles=" + URLEncoder.encode(query, "UTF-8")).body()
				val wikipedia = JsonParser.parseString(wikipediaResponse).asJsonObject // Base
				val wikiQuery = wikipedia.getAsJsonObject("query") // Query
				val wikiPages = wikiQuery.getAsJsonObject("pages") // Páginas
				val entryWikiContent = wikiPages.entrySet().iterator().next() // Conteúdo

				if (entryWikiContent.key == "-1") { // -1 = Nenhuma página encontrada
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["commands.command.wikipedia.couldntFind", query])
				} else {
					// Se não é -1, então é algo que existe! Yay!
					val pageTitle = entryWikiContent.value.asJsonObject.get("title").asString
					val pageExtract = entryWikiContent.value.asJsonObject.get("extract").asString
					val pageUrl = entryWikiContent.value.asJsonObject.get("fullurl").asString

					val embed = EmbedBuilder()
							.setTitle("<:wikipedia:400981794666840084> $pageTitle", pageUrl)
							.setColor(Color.BLACK)
							.setDescription(if (pageExtract.length > 512) pageExtract.substring(0, 509) + "..." else pageExtract)

					context.sendMessageEmbeds(embed.build()) // Envie a mensagem!
				}

			} catch (e: Exception) {
				e.printStackTrace()
				context.sendMessage(context.getAsMention(true) + "**Deu ruim!**")
			}
		} else {
			context.explain()
		}
	}
}