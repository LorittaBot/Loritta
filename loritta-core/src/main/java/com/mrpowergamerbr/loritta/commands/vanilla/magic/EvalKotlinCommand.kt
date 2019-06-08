package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.parallax.ParallaxUtils
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory
import javax.script.Invocable
import javax.script.ScriptEngineManager

class EvalKotlinCommand : AbstractCommand("eval", listOf("evalkt", "evalkotlin", "evaluate", "evalulatekt", "evaluatekotlin"), category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return "Executa códigos em Kotlin"
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		var kotlinCode = context.args.joinToString(" ")

		// Agora vamos mudar um pouquinho o nosso código
		kotlinCode = """
			import com.mrpowergamerbr.loritta.Loritta
			import com.mrpowergamerbr.loritta.LorittaLauncher
			import com.mrpowergamerbr.loritta.commands.CommandContext
			import com.mrpowergamerbr.loritta.utils.locale.*
			import com.mrpowergamerbr.loritta.utils.loritta
			import com.mrpowergamerbr.loritta.utils.lorittaShards
			import com.mrpowergamerbr.loritta.utils.save
			import com.mrpowergamerbr.loritta.utils.Constants
			import com.mrpowergamerbr.loritta.utils.LorittaImage
			import com.mrpowergamerbr.loritta.utils.toBufferedImage
			import com.mrpowergamerbr.loritta.dao.*
			import com.mrpowergamerbr.loritta.tables.*
			import com.mrpowergamerbr.loritta.network.*
			import com.github.salomonbrys.kotson.*
			import org.jetbrains.exposed.sql.transactions.*
			import java.awt.image.BufferedImage
			import java.io.File
			import javax.imageio.ImageIO
			import kotlinx.coroutines.GlobalScope
			import kotlinx.coroutines.launch

			fun loritta(context: CommandContext, locale: LegacyBaseLocale) {
			    GlobalScope.launch(loritta.coroutineDispatcher) {
					$kotlinCode
				}
			}""".trimIndent()

		val engine = ScriptEngineManager().getEngineByName("kotlin") // Iniciar o nashorn
		try {
			engine.eval(kotlinCode)
			val invocable = engine as Invocable
			invocable.invokeFunction("loritta", context, locale) // Pegar o valor retornado pelo script
		} catch (t: Throwable) {
			ParallaxUtils.sendThrowableToChannel(
					t,
					context.event.channel
			)
		}
	}
}