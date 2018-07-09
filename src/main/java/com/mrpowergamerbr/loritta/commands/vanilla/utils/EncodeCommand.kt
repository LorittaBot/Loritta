package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import org.apache.commons.codec.digest.DigestUtils
import java.util.*

class EncodeCommand : AbstractCommand("encode", listOf("codificar", "encrypt", "criptografar", "hash"), CommandCategory.UTILS) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["ENCODE_Description", listOf("md2", "md5", "sha1", "sha256", "sha384", "sha512", "rot13", "uuid", "base64").joinToString(", ", transform = { "`$it`" })]
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf(
				"tipo de codificação" to "Tipo de codificação que será utilizado",
				"texto" to "Texto que você quer que seja criptografado"
		)
	}

	override fun getExtendedExamples(): Map<String, String> {
		return mapOf(
				"sha256 Loritta é muito fofa!" to "Criptografa \"Loritta é muito fofa!\" em SHA256"
		)
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
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
				val b64 = Base64.getEncoder().encode("Test".toByteArray(Charsets.UTF_8))
				String(b64)
			}
			else -> null
		}

		if (encodedText == null) {
			context.reply(
					locale["ENCODE_InvalidMethod", encodeMode.stripCodeMarks()],
					Constants.ERROR
			)
			return
		}

		context.reply(
				true,
				LoriReply(
						"**${locale["ENCODE_OriginalText"]}:** `${text.stripCodeMarks()}`",
						"\uD83D\uDCC4",
						mentionUser = false
				),
				LoriReply(
						"**${locale["ENCODE_EncodedText"]}:** `${encodedText.stripCodeMarks()}`",
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