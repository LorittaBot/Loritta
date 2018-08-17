package com.mrpowergamerbr.loritta.utils.extensions

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed

fun String?.isValidUrl(): Boolean {
	if (this == null)
		return false
	else if (this.length > MessageEmbed.URL_MAX_LENGTH)
		return false
	else if (!EmbedBuilder.URL_PATTERN.matcher(this).matches())
		return false
	return true
}