package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import java.io.File

open class EmojiMashupCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("emojimashup", "emojismashup", "mashupemoji", "mashupemojis", "misturaremojis", "misturaremoji"), net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES) {
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

			val emojiMasher = EmojiMasher(File(loritta.config.loritta.folders.assets, "emoji_mashup"))

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