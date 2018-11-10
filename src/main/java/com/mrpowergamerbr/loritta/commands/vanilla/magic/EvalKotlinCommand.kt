package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.commons.lang3.exception.ExceptionUtils
import java.awt.Color
import java.lang.reflect.Method
import java.util.concurrent.ExecutionException
import javax.script.Invocable
import javax.script.ScriptEngineManager
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

class EvalKotlinCommand : AbstractCommand("eval", listOf("evalkt", "evalkotlin", "evaluate", "evalulatekt", "evaluatekotlin"), category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Executa códigos em Kotlin"
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		var kotlinCode = context.args.joinToString(" ")

		// Agora vamos mudar um pouquinho o nosso código
		kotlinCode = """
			import com.mrpowergamerbr.loritta.Loritta
			import com.mrpowergamerbr.loritta.LorittaLauncher
			import com.mrpowergamerbr.loritta.commands.CommandContext
			import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
			import com.mrpowergamerbr.loritta.utils.loritta
			import com.mrpowergamerbr.loritta.utils.lorittaShards
			import com.mrpowergamerbr.loritta.utils.save
			import com.mrpowergamerbr.loritta.utils.Constants
			import com.mrpowergamerbr.loritta.utils.LorittaImage
			import com.mrpowergamerbr.loritta.utils.toBufferedImage
			import java.awt.image.BufferedImage
			import java.io.File
			import javax.imageio.ImageIO
			import kotlinx.coroutines.GlobalScope
			import kotlinx.coroutines.launch

			fun loritta(context: CommandContext, locale: BaseLocale) {
			    GlobalScope.launch(loritta.coroutineDispatcher) {
					$kotlinCode
				}
			}""".trimIndent()

		val engine = ScriptEngineManager().getEngineByName("kotlin") // Iniciar o nashorn
		try {
			engine.eval(kotlinCode)
			val invocable = engine as Invocable
			invocable.invokeFunction("loritta", context, locale) // Pegar o valor retornado pelo script
		} catch (e: Exception) {
			e.printStackTrace()
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

suspend fun Method.invokeSuspend(obj: Any, vararg args: Any?): Any? =
		suspendCoroutineUninterceptedOrReturn { cont ->
			invoke(obj, *args, cont)
		}