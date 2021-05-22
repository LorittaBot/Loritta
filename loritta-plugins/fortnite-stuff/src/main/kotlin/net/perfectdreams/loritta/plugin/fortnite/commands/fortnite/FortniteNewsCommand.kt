package net.perfectdreams.loritta.plugin.fortnite.commands.fortnite

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.gifs.GifSequenceWriter
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.MiscUtils
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.fortnite.FortniteStuff
import net.perfectdreams.loritta.utils.Emotes
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.stream.FileImageOutputStream

class FortniteNewsCommand(val m: FortniteStuff) : DiscordAbstractCommandBase(m.loritta, listOf("fortnitenews", "fortnitenoticias", "fortnitenotícias", "fnnews", "fnnoticias", "fnnotícias"), CommandCategory.FORTNITE) {
	private val LOCALE_PREFIX = "commands.command.fnnews"

	override fun command() = create {
		localizedDescription("${LOCALE_PREFIX}.description")
		needsToUploadFiles = true

		executesDiscord {
			val newsPayload = m.updateStoreItems!!.getNewsData("br", locale["commands.command.fnshop.localeId"])

			val data = newsPayload.obj["data"]["motds"].array

			val embed = EmbedBuilder()
					.setImage("attachment://fortnite-news.gif")
					.setColor(Color(0, 125, 187))

			val fileName = Loritta.TEMP + "fortnite-news-" + System.currentTimeMillis() + ".gif"
			val output = FileImageOutputStream(File(fileName))
			val writer = GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, 300, true)

			for (_entry in data) {
				val entry = _entry.obj
				val markerText = entry["tabTitle"].nullString
				val title = entry["title"].string
				val body = entry["body"].string
				val imageUrl = entry["tileImage"].string

				val prefix = markerText?.let { "***[$it]*** " } ?: ""

				embed.addField("${Emotes.DEFAULT_DANCE} $prefix **${title}**", body, false)

				val image = LorittaUtils.downloadImage(imageUrl, bypassSafety = true)!!

				writer.writeToSequence(image)
			}

			writer.close()
			output.close()

			MiscUtils.optimizeGIF(File(fileName))

			sendFile(File(fileName), "fortnite-news.gif", getUserMention(true), embed.build())
			File(fileName).delete()
		}
	}
}