package net.perfectdreams.loritta.plugin.fortnite.commands.fortnite

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.gifs.GifSequenceWriter
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.plugin.fortnite.FortniteStuff
import net.perfectdreams.loritta.utils.Emotes
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.stream.FileImageOutputStream

class FortniteNewsCommand(val m: FortniteStuff) : LorittaDiscordCommand(arrayOf("fortnitenews", "fortnitenoticias", "fortnitenotícias", "fnnews", "fnnoticias", "fnnotícias"), CommandCategory.FORTNITE) {
	override val needsToUploadFiles: Boolean
		get() = true

	override fun getDescription(locale: BaseLocale) = locale["commands.fortnite.news.description"]

	@Subcommand
	suspend fun root(context: DiscordCommandContext, locale: BaseLocale) {
		val newsPayload = m.updateStoreItems!!.getNewsData("br", locale["commands.fortnite.shop.localeId"])

		val data = newsPayload["data"].array

		val embed = EmbedBuilder()
				.setImage("attachment://fortnite-news.gif")
				.setColor(Color(0, 125, 187))

		val fileName = Loritta.TEMP + "fortnite-news-" + System.currentTimeMillis() + ".gif"
		val output = FileImageOutputStream(File(fileName))
		val writer = GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, 300, true)

		for (_entry in data) {
			val entry = _entry.obj
			val markerText = entry["adspace"].nullString
			val title = entry["title"].string
			val body = entry["body"].string
			val imageUrl = entry["image"].string

			val prefix = markerText?.let { "***[$it]*** " } ?: ""

			embed.addField("${Emotes.DEFAULT_DANCE} $prefix **${title}**", body, false)

			val image = LorittaUtils.downloadImage(imageUrl, bypassSafety = true)!!

			writer.writeToSequence(image)
		}

		writer.close()
		output.close()

		MiscUtils.optimizeGIF(File(fileName))

		context.sendFile(File(fileName), "fortnite-news.gif", context.getAsMention(true), embed.build())

		return
	}
}