package com.mrpowergamerbr.loritta.website

import com.github.benmanes.caffeine.cache.Caffeine
import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.attributes.methodaccess.NoOpMethodAccessValidator
import com.mitchellbosecke.pebble.cache.tag.CaffeineTagCache
import com.mitchellbosecke.pebble.cache.template.CaffeineTemplateCache
import com.mitchellbosecke.pebble.loader.FileLoader
import com.mrpowergamerbr.loritta.Loritta
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import java.io.StringWriter

class LorittaWebsite(val loritta: Loritta, val websiteUrl: String, var frontendFolder: String) {
	companion object {
		lateinit var ENGINE: PebbleEngine
		lateinit var FOLDER: String
		lateinit var WEBSITE_URL: String

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
				.methodAccessValidator(NoOpMethodAccessValidator())
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