package net.perfectdreams.loritta.morenitta.commands.vanilla.utils

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.math.MathUtils
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.vanilla.utils.CalculatorCommand

class CalculadoraCommand(loritta: LorittaBot) : AbstractCommand(loritta, "calc", listOf("calculadora", "calculator", "calcular", "calculate"), net.perfectdreams.loritta.common.commands.CommandCategory.UTILS) {
	companion object {
		const val LOCALE_PREFIX = "commands.command.calc"
	}

	// TODO: Fix Usage

	override fun getDescriptionKey() = LocaleKeyData("$LOCALE_PREFIX.description")
	override fun getExamplesKey() = LocaleKeyData("$LOCALE_PREFIX.examples")

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "calc")

		if (context.args.isNotEmpty()) {
			CalculatorCommand.executeCompat(
				CommandContextCompat.LegacyMessageCommandContextCompat(context),
				context.args.joinToString(" ")
			)
		} else {
			this.explain(context)
		}
	}
}
