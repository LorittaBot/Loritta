package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.parallax.ParallaxUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import javax.script.Invocable
import javax.script.ScriptEngineManager

class EvalKotlinCommand : AbstractCommand("eval", listOf("evalkt", "evalkotlin", "evaluate", "evalulatekt", "evaluatekotlin"), category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Executa códigos em Kotlin"
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		var kotlinCode = context.args.joinToString(" ")

		val importLines = kotlinCode.lines().takeWhile { it.startsWith("import ") }

		// Agora vamos mudar um pouquinho o nosso código
		kotlinCode = """
			import com.mrpowergamerbr.loritta.Loritta
			import com.mrpowergamerbr.loritta.LorittaLauncher
			import com.mrpowergamerbr.loritta.commands.CommandContext
			import com.mrpowergamerbr.loritta.utils.loritta
			import com.mrpowergamerbr.loritta.utils.lorittaShards
			import com.mrpowergamerbr.loritta.utils.Constants
			import com.mrpowergamerbr.loritta.utils.LorittaImage
			import com.mrpowergamerbr.loritta.utils.toBufferedImage
			import com.mrpowergamerbr.loritta.dao.*
			import com.mrpowergamerbr.loritta.tables.*
			import com.mrpowergamerbr.loritta.network.*
			import com.mrpowergamerbr.loritta.utils.extensions.*
			import net.perfectdreams.loritta.utils.locale.*
			import net.perfectdreams.loritta.common.locale.*
			import net.perfectdreams.loritta.tables.*
			import net.perfectdreams.loritta.tables.servers.*
			import net.perfectdreams.loritta.tables.servers.moduleconfigs.*
			import net.perfectdreams.loritta.dao.*
			import net.perfectdreams.loritta.dao.servers.*
			import net.perfectdreams.loritta.dao.servers.moduleconfigs.*
			import com.github.salomonbrys.kotson.*
			import org.jetbrains.exposed.sql.transactions.*
			import org.jetbrains.exposed.sql.*
			import java.awt.image.BufferedImage
			import java.io.File
			import javax.imageio.ImageIO
			import kotlinx.coroutines.*
			import io.ktor.client.request.*
			import io.ktor.client.statement.*
			import io.ktor.http.*
			${importLines.joinToString("\n")}

			fun loritta(context: CommandContext, locale: BaseLocale) {
			    GlobalScope.launch(loritta.coroutineDispatcher) {
					${kotlinCode.lines().dropWhile { it.startsWith("import ") }.joinToString("\n") }
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