package net.perfectdreams.loritta.legacy.commands.vanilla.utils

import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.common.locale.LocaleStringData
import net.perfectdreams.loritta.legacy.utils.stripCodeMarks
import net.perfectdreams.loritta.legacy.api.commands.ArgumentType
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.api.commands.arguments
import net.perfectdreams.loritta.legacy.api.messages.LorittaReply
import org.apache.commons.codec.digest.DigestUtils
import java.util.*

class EncodeCommand : AbstractCommand("encode", listOf("codificar", "encrypt", "criptografar", "hash"), CommandCategory.UTILS) {
	override fun getDescriptionKey() = LocaleKeyData(
			"commands.command.encode.description",
			listOf(
					LocaleStringData(
							listOf("md2", "md5", "sha1", "sha256", "sha384", "sha512", "rot13", "uuid", "base64").joinToString(", ", transform = { "`$it`" })
					)
			)
	)

	override fun getExamplesKey() = LocaleKeyData("commands.command.encode.examples")

	// TODO: Fix Detailed Usage
	override fun getUsage() = arguments {
		argument(ArgumentType.TEXT) {}
		argument(ArgumentType.TEXT) {}
	}

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		val args = context.rawArgs.toMutableList()
		val encodeMode = context.rawArgs.getOrNull(0)?.toLowerCase()

		if (encodeMode == null) {
			context.explain()
			return
		}

		args.removeAt(0)
		val text = args.joinToString(" ")

		if (text.isEmpty()) {
			context.explain()
			return
		}

		val encodedText = when (encodeMode) {
			"md2" -> DigestUtils.md2Hex(text)
			"md5" -> DigestUtils.md5Hex(text)
			"sha1" -> DigestUtils.sha1Hex(text)
			"sha256" -> DigestUtils.sha256Hex(text)
			"sha384" -> DigestUtils.sha384Hex(text)
			"sha512" -> DigestUtils.sha512Hex(text)
			"rot13" -> rot13(text)
			"uuid" -> UUID.nameUUIDFromBytes(text.toByteArray(Charsets.UTF_8)).toString()
			"base64" -> {
				val b64 = Base64.getEncoder().encode(text.toByteArray(Charsets.UTF_8))
				String(b64)
			}
			else -> null
		}

		if (encodedText == null) {
			context.reply(
					locale["commands.command.encode.invalidMethod", encodeMode.stripCodeMarks()],
					Constants.ERROR
			)
			return
		}

		context.reply(
				true,
				LorittaReply(
						"**${locale["commands.command.encode.originalText"]}:** `${text.stripCodeMarks()}`",
						"\uD83D\uDCC4",
						mentionUser = false
				),
				LorittaReply(
						"**${locale["commands.command.encode.encodedText"]}:** `${encodedText.stripCodeMarks()}`",
						"<:blobspy:465979979876794368>",
						mentionUser = false
				)
		)
	}

	fun rot13(input: String): String {
		val sb = StringBuilder()
		for (i in 0 until input.length) {
			var c = input[i]
			if (c in 'a'..'m')
				c += 13
			else if (c in 'A'..'M')
				c += 13
			else if (c in 'n'..'z')
				c -= 13
			else if (c in 'N'..'Z') c -= 13
			sb.append(c)
		}
		return sb.toString()
	}
}