package com.mrpowergamerbr.loritta.website

import com.github.benmanes.caffeine.cache.Caffeine
import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.cache.tag.CaffeineTagCache
import com.mitchellbosecke.pebble.cache.template.CaffeineTemplateCache
import com.mitchellbosecke.pebble.loader.FileLoader
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.KtsObjectLoader
import kotlinx.html.HtmlBlockTag
import mu.KotlinLogging
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import java.io.File
import java.io.StringWriter
import kotlin.reflect.full.functions

class LorittaWebsite(val loritta: Loritta, val websiteUrl: String, var frontendFolder: String) {
	companion object {
		lateinit var ENGINE: PebbleEngine
		lateinit var FOLDER: String
		lateinit var WEBSITE_URL: String
		private val logger = KotlinLogging.logger {}
		val kotlinTemplateCache = Caffeine.newBuilder().build<String, Any>().asMap()

		const val API_V1 = "/api/v1/"

		fun canManageGuild(g: TemmieDiscordAuth.Guild): Boolean {
			val isAdministrator = g.permissions shr 3 and 1 == 1
			val isManager = g.permissions shr 5 and 1 == 1
			return g.owner || isAdministrator || isManager
		}

		fun getUserPermissionLevel(g: TemmieDiscordAuth.Guild): UserPermissionLevel {
			val isAdministrator = g.permissions shr 3 and 1 == 1
			val isManager = g.permissions shr 5 and 1 == 1

			return when {
				g.owner -> UserPermissionLevel.OWNER
				isAdministrator -> UserPermissionLevel.ADMINISTRATOR
				isManager -> UserPermissionLevel.MANAGER
				else -> UserPermissionLevel.MEMBER
			}
		}
	}

	init {
		OptimizeAssets.optimizeCss()

		WEBSITE_URL = websiteUrl
		FOLDER = frontendFolder

		val fl = FileLoader()
		fl.prefix = frontendFolder
		ENGINE = PebbleEngine.Builder().cacheActive(true) // Deixar o cache ativo ajuda na performance ao usar "extends" em templates (e não ao carregar templates de arquivos!)
				.templateCache(CaffeineTemplateCache()) // Utilizar o cache do Caffeine em vez do padrão usando ConcurrentMapTemplateCache
				.tagCache(CaffeineTagCache()) // Cache para tags de {% cache %} do Pebble
				.allowUnsafeMethods(true)
				.strictVariables(true)
				.loader(fl)
				.build()
	}

	enum class UserPermissionLevel {
		OWNER, ADMINISTRATOR, MANAGER, MEMBER
	}
}

fun evaluate(file: String, variables: MutableMap<String, Any?> = mutableMapOf<String, Any?>()): String {
	val writer = StringWriter()
	val template = LorittaWebsite.ENGINE.getTemplate(file)
	template.evaluate(writer, variables)
	return writer.toString()
}

fun evaluateKotlin(fileName: String, function: String, vararg args: Any?): HtmlBlockTag.() -> Unit {
	println("Evaluating $fileName...")
	val template = LorittaWebsite.kotlinTemplateCache.getOrPut(fileName) {
		val file = File(LorittaWebsite.FOLDER, fileName)
		val scriptContent = file.readText()
		val content = """
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
			import com.mrpowergamerbr.loritta.utils.*
			import com.mrpowergamerbr.loritta.utils.locale.*
			import com.mrpowergamerbr.loritta.dao.*
			import com.mrpowergamerbr.loritta.tables.*
			import com.mrpowergamerbr.loritta.userdata.*
			import net.perfectdreams.temmiediscordauth.*
			import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth.*
			import net.perfectdreams.loritta.website.session.*
			import com.mrpowergamerbr.loritta.website.*
            import com.mrpowergamerbr.loritta.network.*
            import net.perfectdreams.loritta.tables.*
            import net.perfectdreams.loritta.dao.*
			import com.github.salomonbrys.kotson.*
			import org.jetbrains.exposed.sql.transactions.*
            import org.jetbrains.exposed.sql.*
			import java.awt.image.BufferedImage
			import java.io.File
            import java.lang.*
			import javax.imageio.ImageIO
			import kotlinx.coroutines.GlobalScope
			import kotlinx.coroutines.launch
			import kotlinx.html.body
			import kotlinx.html.html
			import kotlinx.html.stream.appendHTML
			import kotlinx.html.*
			import net.dv8tion.jda.api.entities.*
			import net.dv8tion.jda.api.*

			class ContentStuff {
				$scriptContent
			}

			ContentStuff()"""
		KtsObjectLoader().load<Any>(content)
	}

	val kotlinFunction = template::class.functions.first { it.name == function }
	val result = kotlinFunction.call(template, *args) as HtmlBlockTag.() -> Unit
	return result
}