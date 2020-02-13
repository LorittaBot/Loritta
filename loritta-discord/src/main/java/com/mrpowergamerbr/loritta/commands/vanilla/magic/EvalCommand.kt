package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.parallax.ParallaxUtils
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory
import javax.script.Invocable
import javax.script.ScriptEngineManager

class EvalCommand : AbstractCommand("evaljs", listOf("evaljavascript", "evaluatejs", "evaluatejavascript"), category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return "Executa códigos em JavaScript"
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		var javaScript = context.args.joinToString(" ")

		// Agora vamos mudar um pouquinho o nosso código
		javaScript = "function loritta(context) {$javaScript}"

		val engine = ScriptEngineManager().getEngineByName("nashorn") // Iniciar o nashorn
		try {
			engine.eval(javaScript)
			val invocable = engine as Invocable
			val returnedValue = invocable.invokeFunction("loritta", context) // Pegar o valor retornado pelo script

			if (returnedValue != null) {
				context.sendMessage(returnedValue.toString()) // Value of, já que nós não sabemos qual tipo esse objeto é
			}
		} catch (t: Throwable) {
			ParallaxUtils.sendThrowableToChannel(
					t,
					context.event.channel
			)
		}
	}
}