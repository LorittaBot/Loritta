package net.perfectdreams.loritta.morenitta.commands.vanilla.magic

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.parallax.ParallaxUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import javax.script.Invocable
import javax.script.ScriptEngineManager

class EvalKotlinCommand : AbstractCommand("eval", listOf("evalkt", "evalkotlin", "evaluate", "evalulatekt", "evaluatekotlin"), category = net.perfectdreams.loritta.common.commands.CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Executa códigos em Kotlin"
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		var kotlinCode = context.args.joinToString(" ")

		val importLines = kotlinCode.lines().takeWhile { it.startsWith("import ") }

		// Agora vamos mudar um pouquinho o nosso código
		kotlinCode = """
			import net.perfectdreams.loritta.legacy.Loritta
			import net.perfectdreams.loritta.legacy.LorittaLauncher
			import net.perfectdreams.loritta.legacy.commands.CommandContext
			import net.perfectdreams.loritta.legacy.utils.loritta
			import net.perfectdreams.loritta.legacy.utils.lorittaShards
			import net.perfectdreams.loritta.legacy.utils.Constants
			import net.perfectdreams.loritta.legacy.utils.LorittaImage
			import net.perfectdreams.loritta.legacy.utils.toBufferedImage
			import net.perfectdreams.loritta.legacy.dao.*
			import net.perfectdreams.loritta.legacy.tables.*
			import net.perfectdreams.loritta.legacy.network.*
			import net.perfectdreams.loritta.legacy.utils.extensions.*
			import net.perfectdreams.loritta.legacy.utils.locale.*
			import net.perfectdreams.loritta.common.locale.*
			import net.perfectdreams.loritta.legacy.tables.*
			import net.perfectdreams.loritta.legacy.tables.servers.*
			import net.perfectdreams.loritta.legacy.tables.servers.moduleconfigs.*
			import net.perfectdreams.loritta.legacy.dao.*
			import net.perfectdreams.loritta.legacy.dao.servers.*
			import net.perfectdreams.loritta.legacy.dao.servers.moduleconfigs.*
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