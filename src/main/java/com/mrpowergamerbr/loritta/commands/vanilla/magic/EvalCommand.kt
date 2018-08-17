package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.commons.lang3.exception.ExceptionUtils
import java.awt.Color
import java.util.concurrent.ExecutionException
import javax.script.Invocable
import javax.script.ScriptEngineManager

class EvalCommand : AbstractCommand("evaljs", listOf("evaljavascript", "evaluatejs", "evaluatejavascript"), category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Executa códigos em JavaScript"
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
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
		} catch (e: Exception) {
			val builder = EmbedBuilder()
			builder.setTitle("❌ Ih Serjão Sujou! 🤦", "https://youtu.be/G2u8QGY25eU")
			var description = "Irineu, você não sabe e nem eu!"
			if (e is ExecutionException) {
				description = "A thread que executava este comando agora está nos céus... *+angel* (Provavelmente seu script atingiu o limite máximo de memória utilizada!)"
			} else {
				val message = e.cause?.message
				if (e != null && e.cause != null && message != null) {
					description = message.trim { it <= ' ' }
				} else if (e != null) {
					description = ExceptionUtils.getStackTrace(e).substring(0, Math.min(1000, ExceptionUtils.getStackTrace(e).length))
				}
			}
			builder.setDescription("```$description```")
			builder.setFooter("Aprender a programar seria bom antes de me forçar a executar códigos que não funcionam 😢", null)
			builder.setColor(Color.RED)
			context.sendMessage(builder.build())
		}
	}
}