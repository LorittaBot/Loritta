package net.perfectdreams.loritta.commands.images

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.emojimasher.EmojiMasher
import java.io.File
import kotlin.contracts.ExperimentalContracts

open class EmojiMashupCommand : LorittaCommand(arrayOf("emojimashup", "emojismashup", "mashupemoji", "mashupemojis", "misturaremojis", "misturaremoji"), CommandCategory.IMAGES) {
	override fun getDescription(locale: BaseLocale): String? {
		return locale["commands.images.emojimashup.description"]
	}

	override fun getUsage(locale: BaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.EMOTE) {}
			argument(ArgumentType.EMOTE) {}
			argument(ArgumentType.EMOTE) {
				this.optional = true
			}
			argument(ArgumentType.EMOTE) {
				this.optional = true
			}
		}
	}

	override fun getExamples(locale: BaseLocale) = listOf(
			"\uD83D\uDE0E \uD83D\uDE02",
			"\uD83E\uDD20 \uD83D\uDE2D \uD83D\uDE0E",
			"\uD83D\uDCA9 \uD83E\uDD11 \uD83D\uDE18 \uD83D\uDE05"
	)

	@ExperimentalContracts
	@Subcommand
	suspend fun run(context: LorittaCommandContext, locale: BaseLocale) {
		val emojiArg1 = context.args.getOrNull(0)
		val emojiArg2 = context.args.getOrNull(1)

		if (emojiArg1 == null || emojiArg2 == null) {
			context.explain()
			return
		}

		val emojiMasher = EmojiMasher(File(loritta.instanceConfig.loritta.folders.assets, "emoji_mashup"))

		val emojiArg3 = context.args.getOrNull(2)
		val emojiArg4 = context.args.getOrNull(3)

		fun getUnicodeCodeIfSupported(arg: String?): String? {
			if (arg == null)
				return null

			val unicode = try { LorittaUtils.toUnicode(arg.codePointAt(0)).substring(2) } catch (e: Exception) { return null }
			val isSupported = emojiMasher.isEmojiSupported(unicode)

			return if (isSupported) {
				unicode
			} else {
				null
			}
		}

		val emoji1 = getUnicodeCodeIfSupported(emojiArg1) ?: run {
			invalidEmote(context, locale, emojiArg1)
			return
		}

		val emoji2 = getUnicodeCodeIfSupported(emojiArg2) ?: run {
			invalidEmote(context, locale, emojiArg2)
			return
		}

		val emoji3 = emojiArg3?.let {
			getUnicodeCodeIfSupported(emojiArg3) ?: run {
				invalidEmote(context, locale, it)
				return
			}
		}

		val emoji4 = emojiArg4?.let {
			getUnicodeCodeIfSupported(emojiArg4) ?: run {
				invalidEmote(context, locale, it)
				return
			}
		}

		val image = emojiMasher.mashupEmojis(emoji1, emoji2, emoji3, emoji4)

		context.sendFile(image, "emoji_mashup.png", context.getAsMention(true))
	}

	suspend fun invalidEmote(context: LorittaCommandContext, locale: BaseLocale, arg: String) {
		if (arg.startsWith("<")) {
			context.reply(
					LoriReply(
							locale["commands.images.emojimashup.invalidEmojiDiscord", arg.stripCodeMarks()],
							Constants.ERROR
					)
			)
		} else {
			context.reply(
					LoriReply(
							locale["commands.images.emojimashup.invalidEmoji", arg.stripCodeMarks()],
							Constants.ERROR
					)
			)
		}
	}
}