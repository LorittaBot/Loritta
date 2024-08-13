package net.perfectdreams.loritta.morenitta.utils.extensions

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.perfectdreams.loritta.morenitta.utils.MiscUtils

fun String?.isValidUrl(): Boolean {
	if (this == null)
		return false
	else if (this.length > MessageEmbed.URL_MAX_LENGTH)
		return false
	else if (!EmbedBuilder.URL_PATTERN.matcher(this).matches())
		return false
	return true
}

fun String.stripLinks() = MiscUtils.stripLinks(this)

/**
 * Creates a string from all the elements separated using [separator].
 *
 * The [characterLimit] limits how many characters can be in the resulting list, the elements will be appended up to [characterLimit], removing any elements
 * that won't fit on the resulting string and, if truncated, it will be followed by the [truncated] string (which defaults to "...").
 */
fun <T> Iterable<T>.joinToStringLimitedByCharacters(separator: CharSequence = ", ", characterLimit: Int, truncated: CharSequence = "...", transform: ((T) -> CharSequence)? = null): String {
	val builder = StringBuilder()
	val trueLimit = characterLimit - truncated.length
	var isFirst = true
	for (element in this) {
		val input = buildString {
			if (!isFirst) {
				this.append(separator)
			}
			this.append(transform?.invoke(element) ?: element)
		}

		if (builder.length + input.length > trueLimit) {
			// Too long, bail out!
			builder.append(truncated)
			break
		}

		// It fits! Append to the builder
		builder.append(input)
		isFirst = false
	}

	return builder.toString()
}