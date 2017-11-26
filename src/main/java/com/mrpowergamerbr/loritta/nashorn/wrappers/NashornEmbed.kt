package com.mrpowergamerbr.loritta.nashorn.wrappers

import com.google.gson.annotations.SerializedName
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import java.awt.Color

class NashornEmbed(
		var color: Int? = null,
		var title: String? = null,
		var description: String? = null,
		var url: String? = null,
		var thumbnail: NashornEmbedImage? = null,
		var image: NashornEmbedImage? = null,
		var author: NashornEmbedAuthor? = null,
		var footer: NashornEmbedFooter? = null,
		var fields: List<NashornEmbedField>? = null
) {

	fun toDiscordEmbed(): MessageEmbed {
		val embed = EmbedBuilder()
		if (color != null) {
			val red = color!! shr 16 and 0xFF
			val green = color!! shr 8 and 0xFF
			val blue = color!! and 0xFF
			embed.setColor(Color(red, green, blue))
		}
		if (title != null) {
			embed.setTitle(title, url)
		}
		if (description != null) {
			embed.setDescription(description)
		}
		if (author != null) {
			embed.setAuthor(author!!.name, author!!.url, author!!.iconUrl)
		}
		if (footer != null) {
			embed.setFooter(footer!!.text, footer!!.iconUrl)
		}
		if (fields != null) {
			for (field in fields!!) {
				embed.addField(field.name, field.value, field.inline)
			}
		}
		if (thumbnail != null) {
			embed.setThumbnail(thumbnail!!.url)
		}
		if (image != null) {
			embed.setThumbnail(image!!.url)
		}
		return embed.build()
	}

	class NashornEmbedAuthor(
			var name: String? = null,
			var url: String? = null,
			@SerializedName("icon_url")
			var iconUrl: String? = null)

	class NashornEmbedFooter(
			var text: String? = null,
			@SerializedName("icon_url")
			var iconUrl: String? = null)

	class NashornEmbedImage(
			var url: String? = null
	)

	class NashornEmbedField(
			var name: String? = null,
			var value: String? = null,
			var inline: Boolean = false
	)
}