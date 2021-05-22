package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.Emotes
import java.util.*

class AvaliarWaifuCommand : AbstractCommand("ratewaifu", listOf("avaliarwaifu", "ratemywaifu", "ratewaifu", "avaliarminhawaifu", "notawaifu"), CommandCategory.FUN) {
	companion object {
		private const val LOCALE_PREFIX = "commands.command.ratewaifu"
	}

	override fun getDescriptionKey() = LocaleKeyData("$LOCALE_PREFIX.description")
	override fun getExamplesKey() = LocaleKeyData("$LOCALE_PREFIX.examples")

	// TODO: Fix Usage

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			var waifu = context.args.joinToString(separator = " ") // Vamos juntar tudo em uma string

			val user = context.getUserAt(0)
			if (user != null) {
				waifu = user.name
			}

			val waifuLowerCase = waifu.toLowerCase()
			val random = SplittableRandom(Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + waifuLowerCase.hashCode().toLong()) // Usar um RANDOM sempre com a mesma seed
			val nota = random.nextInt(0, 11)

			val scoreReason = context.locale.getList("$LOCALE_PREFIX.note${nota}").random()

			var reason = when (nota) {
				10 -> "$scoreReason ${Emotes.LORI_WOW}"
				9 -> "$scoreReason ${Emotes.LORI_HEART}"
				8 -> "$scoreReason ${Emotes.LORI_PAT}"
				7 -> "$scoreReason ${Emotes.LORI_SMILE}"
				3 -> "$scoreReason ${Emotes.LORI_SHRUG}"
				2 -> "$scoreReason ${Emotes.LORI_HMPF}"
				1 -> "$scoreReason ${Emotes.LORI_RAGE}"
				else -> scoreReason
			}

			var strNota = nota.toString()
			if (waifuLowerCase == "loritta") {
				strNota = "∞"
				reason = "${context.locale.getList("$LOCALE_PREFIX.noteLoritta").random()} ${Emotes.LORI_YAY}"
			}
			if (waifuLowerCase == "pollux") {
				strNota = "10"
				reason = context.locale.getList("$LOCALE_PREFIX.notePollux").random()
			}
			if (waifuLowerCase == "pantufa") {
				strNota = "10"
				reason = context.locale.getList("$LOCALE_PREFIX.notePantufa").random() + " ${Emotes.LORI_HEART}"
			}
			if (waifuLowerCase == "tatsumaki") {
				strNota = "10"
				reason = context.locale.getList("$LOCALE_PREFIX.noteTatsumaki").random()
			}
			if (waifuLowerCase == "mee6") {
				strNota = "10"
				reason = context.locale.getList("$LOCALE_PREFIX.noteMee6").random()
			}
			if (waifuLowerCase == "mantaro") {
				strNota = "10"
				reason = context.locale.getList("$LOCALE_PREFIX.noteMantaro").random()
			}
			if (waifuLowerCase == "dyno") {
				strNota = "10"
				reason = context.locale.getList("$LOCALE_PREFIX.noteDyno").random()
			}
			if (waifuLowerCase == "mudae") {
				strNota = "10"
				reason = context.locale.getList("$LOCALE_PREFIX.noteMudae").random()
			}
			if (waifuLowerCase == "nadeko") {
				strNota = "10"
				reason = context.locale.getList("$LOCALE_PREFIX.noteNadeko").random()
			}
			if (waifuLowerCase == "unbelievaboat") {
				strNota = "10"
				reason = context.locale.getList("$LOCALE_PREFIX.noteUnbelievaBoat").random()
			}
			if (waifuLowerCase == "chino kafuu") {
				strNota = "10"
				reason = context.locale.getList("$LOCALE_PREFIX.noteChinoKafuu").random()
			}
			if (waifuLowerCase == "groovy") {
				strNota = "10"
				reason = context.locale.getList("$LOCALE_PREFIX.noteGroovy").random()
			}
			if (waifuLowerCase == "lorita" || waifuLowerCase == "lorrita") {
				strNota = "-∞"
				reason = "${context.locale.getList("$LOCALE_PREFIX.noteLorrita").random()} ${Emotes.LORI_HMPF}"
			}

			context.reply(
                    LorittaReply(
                            message = context.locale["$LOCALE_PREFIX.result", strNota, waifu.stripCodeMarks(), reason],
                            prefix = "\uD83E\uDD14"
                    )
			)
		} else {
			this.explain(context)
		}
	}
}
