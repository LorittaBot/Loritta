package net.perfectdreams.loritta.morenitta.utils.extensions

import net.perfectdreams.loritta.morenitta.utils.MiscUtils
import net.perfectdreams.loritta.deviousfun.EmbedBuilder
import net.perfectdreams.loritta.deviousfun.DeviousEmbed

fun String?.isValidUrl(): Boolean {
	if (this == null)
		return false
	else if (this.length > DeviousEmbed.URL_MAX_LENGTH)
		return false
	else if (!EmbedBuilder.URL_PATTERN.matcher(this).matches())
		return false
	return true
}

fun String.stripLinks() = MiscUtils.stripLinks(this)