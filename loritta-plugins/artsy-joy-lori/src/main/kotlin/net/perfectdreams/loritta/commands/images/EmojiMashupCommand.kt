package net.perfectdreams.loritta.commands.images

import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.commands.LorittaCommandContext
import net.perfectdreams.loritta.emojimasher.EmojiMasher
import java.io.File
import kotlin.contracts.ExperimentalContracts

open class EmojiMashupCommand : LorittaCommand(arrayOf("emojimashup"), CommandCategory.IMAGES) {
	@ExperimentalContracts
	@Subcommand
	suspend fun run(context: LorittaCommandContext, locale: BaseLocale) {
		val emojiArg1 = context.args.getOrNull(0)
		val emojiArg2 = context.args.getOrNull(1)
		val emojiArg3 = context.args.getOrNull(2)
		val emojiArg4 = context.args.getOrNull(3)

		val emoji1 = emojiArg1?.let { LorittaUtils.toUnicode(it.codePointAt(0)).substring(2) }!!
		val emoji2 = emojiArg2?.let { LorittaUtils.toUnicode(it.codePointAt(0)).substring(2) }!!
		val emoji3 = emojiArg3?.let { LorittaUtils.toUnicode(it.codePointAt(0)).substring(2) }
		val emoji4 = emojiArg4?.let { LorittaUtils.toUnicode(it.codePointAt(0)).substring(2) }

		val emojiMasher = EmojiMasher(File(loritta.instanceConfig.loritta.folders.assets, "emoji_mashup"))

		val image = emojiMasher.mashupEmojis(emoji1, emoji2, emoji3, emoji4)

		context.sendFile(image, "emoji_mashup.png", context.getAsMention(true))
	}
}