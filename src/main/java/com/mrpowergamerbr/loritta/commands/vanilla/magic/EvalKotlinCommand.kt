package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.commons.lang3.exception.ExceptionUtils
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import java.awt.Color
import java.util.concurrent.ExecutionException
import javax.script.Invocable

class EvalKotlinCommand : AbstractCommand("evalkt", category = CommandCategory.MAGIC) {
	override fun onlyOwner(): Boolean {
		return true
	}

	override fun getDescription(locale: BaseLocale): String {
		return "Executa códigos em Kotlin"
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var kotlinCode = context.args.joinToString(" ")

		val packages = listOf(
				"com.mrpowergamerbr.loritta.utils.locale.BaseLocale",
				"com.mrpowergamerbr.loritta.commands.CommandContext",
				"com.mrpowergamerbr.loritta.Loritta",
				"java.lang.management",
				"java.lang.annotation",
				"java.io",
				"java.nio.file",
				"java.text",
				"java.time",
				"java.time.format",
				"java.time.temporal",
				"java.util.concurrent",
				"java.util.concurrent.atomic",
				"java.util.stream",
				"java.net",
				"javax.script",
				"net.dv8tion.jda.core",
				"net.dv8tion.jda.core.entities",
				"net.dv8tion.jda.core.managers",
				"net.dv8tion.jda.core.utils",
				"net.dv8tion.jda.core.utils.cache",
				"net.dv8tion.jda.core.requests",
				"net.dv8tion.jda.core.exceptions",
				"net.dv8tion.jda.core.hooks",
				"net.dv8tion.jda.core.events.message",
				"net.dv8tion.jda.core.events.message.react",
				"kotlin.reflect",
				"kotlin.reflect.jvm",
				"kotlin.reflect.full",
				"kotlin.system",
				"kotlin.io",
				"kotlin.concurrent",
				"kotlin.coroutines.experimental",
				"kotlin.streams",
				"kotlin.properties"
		)

		// Agora vamos mudar um pouquinho o nosso código
		kotlinCode = """
fun loritta(context: CommandContext, locale: BaseLocale) {
	$kotlinCode
}"""

		var strPackages = ""
		for (packagee in packages) {
			strPackages = "import $packagee\n"
		}

		kotlinCode = strPackages + kotlinCode

		val engine = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine // Iniciar o Kotlin Script Engine
		try {
			engine.eval(kotlinCode)
			val invocable = engine as Invocable
			val returnedValue = invocable.invokeFunction("loritta", context, locale) // Pegar o valor retornado pelo script

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