package com.mrpowergamerbr.loritta.parallax.wrappers

import com.google.gson.annotations.SerializedName
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import java.awt.Color

class ParallaxEmbed {
	var color: Int? = null
	var title: String? = null
	var url: String?  = null
	var description: String?  = null
	var author: ParallaxEmbedAuthor?  = null
	var thumbnail: ParallaxEmbedImage?  = null
	var image: ParallaxEmbedImage? = null
	var footer: ParallaxEmbedFooter? = null
	var fields: MutableList<ParallaxEmbedField>? = null

	fun toDiscordEmbed(): MessageEmbed {
		val embed = EmbedBuilder()

		if (color != null) {
			val red = color!! shr 16 and 0xFF
			val green = color!! shr 8 and 0xFF
			val blue = color!! and 0xFF
			embed.setColor(Color(red, green, blue))
		}

		if (description != null) {
			embed.setDescription(description)
		}

		if (title != null) {
			embed.setTitle(title, url)
		}

		if (author != null) {
			embed.setAuthor(author!!.name, author!!.url, author!!.iconUrl)
		}

		if (footer != null) {
			embed.setFooter(footer!!.text, footer!!.iconUrl)
		}

		if (image != null) {
			embed.setImage(image!!.url)
		}

		if (thumbnail != null) {
			embed.setThumbnail(thumbnail!!.url)
		}

		if (fields != null) {
			fields!!.forEach {
				println(it)
				embed.addField(it.name, it.value, it.inline)
			}
		}

		return embed.build()
	}

	class ParallaxEmbedAuthor(
			var name: String?,
			var url: String?,
			@SerializedName("icon_url")
			var iconUrl: String?
	)

	class ParallaxEmbedImage(
			var url: String?
	)

	class ParallaxEmbedFooter(
			var text: String?,
			@SerializedName("icon_url")
			var iconUrl: String?
	)

	class ParallaxEmbedField(
			var name: String?,
			var value: String?,
			var inline: Boolean = false
	)
}