package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.encodeToUrl
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import org.json.XML
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO

class TextCraftCommand : AbstractCommand("textcraft", category = CommandCategory.IMAGES) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["TEXTCRAFT_Description", TextCraftFont.values().joinToString(", ", transform = { it.internalName })]
	}

	override fun getExamples(): List<String> {
		return listOf(
				"Minecraft",
				"PerfectDreams | é o melhor servidor survival de Minecraft",
				"Olá Mundo! | A Lori é fofa! | font6 | font12"
		)
	}

	override fun getUsage(): String {
		return "<texto>"
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val args = context.args.joinToString(" ").split(" | ")

			var text1 = ""
			var text2 = ""
			var text3 = ""
			var fontStyle1 = "font1"
			var fontStyle2 = "font6"
			var fontStyle3 = "font6"
			val fontColour = "0"

			var fontIndex = 0
			var textIndex = 0
			for (arg in args) {
				val fontName = arg.toLowerCase()
				if (isValidFont(fontName)) {
					when (fontIndex) {
						0 -> fontStyle1 = fontName
						1 -> fontStyle2 = fontName
						2 -> fontStyle3 = fontName
					}
					fontIndex++
				} else {
					when (textIndex) {
						0 -> text1 = arg
						1 -> text2 = arg
						2 -> text3 = arg
					}
					textIndex++
				}
			}

			val body = HttpRequest.get("https://textcraft.net/gentext3.php?text=${text1.encodeToUrl()}&text2=${text2.encodeToUrl()}&text3=${text3.encodeToUrl()}&font_style=$fontStyle1&font_size=x&font_colour=$fontColour&bgcolour=%232C262E&glow_halo=0&glossy=0&lighting=0&fit_lines=0&truecolour_images=0&non_trans=false&glitter_border=true&text_border=1&border_colour=%232C262E&anim_type=none&submit_type=text&perspective_effect=1&drop_shadow=1&savedb=0&multiline=1&font_style2=$fontStyle2&font_style3=$fontStyle3&font_size2=t&font_size3=t&font_colour2=68&font_colour3=66&text_border2=1&text_border3=1&border_colour2=%23211E4E&border_colour3=%23EBD406")
					.body()

			val xmlJSONObj = XML.toJSONObject(body)
			val jsonPrettyPrintString = xmlJSONObj.toString(4)
			val payload = jsonParser.parse(jsonPrettyPrintString)

			val dataDir = payload["image"]["datadir"].string
			val fullFilename = payload["image"]["fullfilename"].string
			val imageUrl = URL("https://static1.textcraft.net/$dataDir/$fullFilename")
			val connection = imageUrl.openConnection() as HttpURLConnection

			val bi = ImageIO.read(connection.inputStream)
			context.sendFile(bi, "textcraft.png", context.getAsMention(true))
		} else {
			context.explain()
		}
	}

	fun isValidFont(internalName: String): Boolean {
		for (font in TextCraftFont.values()) {
			if (font.internalName == internalName)
				return true
		}
		return false
	}

	enum class TextCraftFont(val internalName: String) {
		FONT_1("font1"),
		FONT_2("font2"),
		FONT_3("font3"),
		FONT_4("font4"),
		FONT_5("font5"),
		FONT_6("font6"),
		FONT_7("font7"),
		FONT_8("font8"),
		FONT_9("font9"),
		FONT_10("font10"),
		FONT_11("font11"),
		FONT_12("font12"),
		FONT_13("font13"),
		FONT_14("font14"),
		FONT_15("font15"),
		FONT_16("font16"),
		FONT_17("font17"),
		FONT_18("font18"),
		FONT_19("font19"),
		FONT_20("font20"),
		FONT_21("font21"),
		FONT_22("font22"),
		FONT_23("font23"),
		FONT_24("font24"),
		FONT_25("font25"),
		FONT_26("font26"),
		FONT_27("font27"),
		FONT_28("font28"),
		FONT_29("font29"),
		FONT_30("font30"),
		FONT_31("font31"),
		FONT_32("font32"),
		FONT_33("font33"),
		FONT_34("font34"),
		FONT_35("font35"),
		FONT_36("font36")
	}
}