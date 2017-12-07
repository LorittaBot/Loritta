package com.mrpowergamerbr.loritta.frontend

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.loader.FileLoader
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite.Companion.WEBSITE_URL
import com.mrpowergamerbr.loritta.frontend.views.GlobalHandler
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import org.jooby.Kooby
import org.jooby.mongodb.MongoSessionStore
import org.jooby.mongodb.Mongodb
import java.io.File
import java.io.StringWriter

class LorittaWebsite(val websiteUrl: String, var frontendFolder: String) : Kooby({
	port(4568) // Porta do website
	use(Mongodb()) // Usar extensão do MongoDB para o Jooby
	session(MongoSessionStore::class.java) // Usar session store para o MongoDB do Jooby
	assets("/**", File(frontendFolder, "static/").toPath())
	get("/**", { req, res ->
		res.send(GlobalHandler.render(req, res))
	})
	post("/**", { req, res ->
		res.send(GlobalHandler.render(req, res))
	})
}) {
	companion object {
		lateinit var engine: PebbleEngine
		lateinit var FOLDER: String
		lateinit var WEBSITE_URL: String

		fun canManageGuild(g: TemmieDiscordAuth.DiscordGuild): Boolean {
			val isAdministrator = g.permissions shr 3 and 1 == 1
			val isManager = g.permissions shr 5 and 1 == 1
			return g.owner || isAdministrator || isManager
		}

		fun getUserPermissionLevel(g: TemmieDiscordAuth.DiscordGuild): UserPermissionLevel {
			val isAdministrator = g.permissions shr 3 and 1 == 1
			val isManager = g.permissions shr 5 and 1 == 1

			return when {
				g.owner -> UserPermissionLevel.OWNER
				isAdministrator -> UserPermissionLevel.ADMINISTRATOR
				isManager -> UserPermissionLevel.MANAGER
				else -> UserPermissionLevel.MEMBER
			}
		}

		var loaded = false
	}

	init {
		LorittaWebsite.WEBSITE_URL = websiteUrl
		LorittaWebsite.FOLDER = frontendFolder

		val fl = FileLoader()
		fl.prefix = frontendFolder
		LorittaWebsite.engine = PebbleEngine.Builder().cacheActive(false).strictVariables(true).loader(fl).build()
	}

	enum class UserPermissionLevel {
		OWNER, ADMINISTRATOR, MANAGER, MEMBER
	}
}

inline fun evaluate(file: String, variables: MutableMap<String, Any?> = mutableMapOf<String, Any?>()): String {
	variables["websiteUrl"] = WEBSITE_URL
	val writer = StringWriter()
	LorittaWebsite.engine.getTemplate("$file").evaluate(writer, variables)
	return writer.toString()
}