package net.perfectdreams.loritta.commands.images

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.utils.EmojiMasher
import java.io.File

open class EmojiMashupCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("emojimashup", "emojismashup", "mashupemoji", "mashupemojis", "misturaremojis", "misturaremoji"), CommandCategory.IMAGES) {
	companion object {
		private const val LOCALE_PREFIX = "commands.command"
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.emojimashup.description")
		localizedExamples("$LOCALE_PREFIX.emojimashup.examples")

		usage {
			arguments {
				argument(ArgumentType.EMOTE) {}
				argument(ArgumentType.EMOTE) {}
				argument(ArgumentType.EMOTE) {
					optional = true
				}
				argument(ArgumentType.EMOTE) {
					optional = true
				}
			}
		}

		executesDiscord {
			val context = this

			val emojiArg1 = context.args.getOrNull(0)
			val emojiArg2 = context.args.getOrNull(1)

			if (emojiArg1 == null || emojiArg2 == null) {
				context.explain()
				return@executesDiscord
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
				return@executesDiscord
			}

			val emoji2 = getUnicodeCodeIfSupported(emojiArg2) ?: run {
				invalidEmote(context, locale, emojiArg2)
				return@executesDiscord
			}

			val emoji3 = emojiArg3?.let {
				getUnicodeCodeIfSupported(emojiArg3) ?: run {
					invalidEmote(context, locale, it)
					return@executesDiscord
				}
			}

			val emoji4 = emojiArg4?.let {
				getUnicodeCodeIfSupported(emojiArg4) ?: run {
					invalidEmote(context, locale, it)
					return@executesDiscord
				}
			}

			val image = emojiMasher.mashupEmojis(emoji1, emoji2, emoji3, emoji4)

			context.sendImage(JVMImage(image), "emoji_mashup.png", context.getUserMention(true))
		}
	}
	private suspend fun invalidEmote(context: DiscordCommandContext, locale: BaseLocale, arg: String) {
		if (arg.startsWith("<")) {
			context.reply(
					LorittaReply(
							locale["$LOCALE_PREFIX.emojimashup.invalidEmojiDiscord", arg.stripCodeMarks()],
							Constants.ERROR
					)
			)
		} else {
			context.reply(
					LorittaReply(
							locale["$LOCALE_PREFIX.emojimashup.invalidEmoji", arg.stripCodeMarks()],
							Constants.ERROR
					)
			)
		}
	}
}