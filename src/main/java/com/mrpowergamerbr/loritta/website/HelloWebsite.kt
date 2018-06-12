package com.mrpowergamerbr.loritta.website

import com.mitchellbosecke.pebble.PebbleEngine
import com.mrpowergamerbr.loritta.utils.logger
import com.mrpowergamerbr.loritta.utils.oauth2.TemmieDiscordAuth
import org.jooby.Kooby
import org.jooby.mongodb.MongoSessionStore
import org.jooby.mongodb.Mongodb
import kotlin.concurrent.thread

class HelloWebsite : Kooby({
	port(4568) // Porta do website
	use(Mongodb()) // Usar extensão do MongoDB para o Jooby
	session(MongoSessionStore::class.java) // Usar session store para o MongoDB do Jooby
	get("**", { req, res ->
		res.send("Hello World!")
	})
	post("**", { req, res ->
		res.send("Hello World!")
	})
}) {
	companion object {
		lateinit var ENGINE: PebbleEngine
		lateinit var FOLDER: String
		lateinit var WEBSITE_URL: String
		val logger by logger()
		const val API_V1 = "/api/v1/"

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
	}
	enum class UserPermissionLevel {
		OWNER, ADMINISTRATOR, MANAGER, MEMBER
	}
}

fun main(args: Array<String>) {
	var website = HelloWebsite()

	thread {
		while (true) {
			val line = readLine()!!

			if (line == "reboot") {
				println("Rebooting...")
				website.stop()
				website = HelloWebsite()
				thread {
					website.start()
				}
				println("Rebooted!!!")
			}
		}
	}

	thread {
		website.start()
	}
}