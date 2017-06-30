package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.util.*
import javax.script.Invocable
import javax.script.ScriptEngineManager

class EvalCommand : CommandBase() {
	override fun getLabel(): String {
		return "eval"
	}

	override fun getAliases(): List<String> {
		return Arrays.asList("executar")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MAGIC
	}

	override fun onlyOwner(): Boolean {
		return true
	}

	override fun run(context: CommandContext) {
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
			e.printStackTrace()
			val builder = EmbedBuilder()
			builder.setTitle("❌ Ih Serjão Sujou! 🤦", "https://youtu.be/G2u8QGY25eU")
			builder.setFooter("Aprender a programar seria bom antes de me forçar a executar códigos que não funcionam 😢", null)
			builder.setColor(Color.RED)
			context.sendMessage(builder.build())
		}
	}
}