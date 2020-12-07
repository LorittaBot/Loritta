package net.perfectdreams.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import org.apache.commons.codec.digest.DigestUtils
import java.util.*

class EncodeCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("encode", "codificar", "encrypt", "criptografar", "hash"), CommandCategory.UTILS) {
	companion object {
		private const val LOCALE_PREFIX = "commands.utils.encode"
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.description", listOf("md2", "md5", "sha1", "sha256", "sha384", "sha512", "rot13", "uuid", "base64").joinToString(", ", transform = { "`$it`" }))

		examples {
			+ "A Loritta é fofa sha256"
		}

		usage {
			argument(ArgumentType.TEXT) {
				this.text = "code"
			}
			argument(ArgumentType.TEXT) {}
		}

		executesDiscord {
			val context = this

			val args = context.args.toMutableList()
			val encodeMode = context.args.getOrNull(0)?.toLowerCase()

			if (encodeMode == null) {
				context.explain()
				return@executesDiscord
			}

			args.removeAt(0)
			val text = args.joinToString(" ")

			if (text.isEmpty()) {
				context.explain()
				return@executesDiscord
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
						locale["$LOCALE_PREFIX.invalidMethod", encodeMode.stripCodeMarks()],
						Constants.ERROR
				)
				return@executesDiscord
			}

			context.reply(
					LorittaReply(
							"**${locale["$LOCALE_PREFIX.originalText"]}:** `${text.stripCodeMarks()}`",
							"\uD83D\uDCC4",
							mentionUser = false
					),
					LorittaReply(
							"**${locale["$LOCALE_PREFIX.encodedText"]}:** `${encodedText.stripCodeMarks()}`",
							"<:blobspy:465979979876794368>",
							mentionUser = false
					)
			)
		}
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