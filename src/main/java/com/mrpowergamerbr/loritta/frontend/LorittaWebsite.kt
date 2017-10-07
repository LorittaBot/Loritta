package com.mrpowergamerbr.loritta.frontend

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.loader.FileLoader
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite.Companion.WEBSITE_URL
import com.mrpowergamerbr.loritta.frontend.views.GlobalHandler
import org.jooby.Kooby
import org.jooby.mongodb.MongoSessionStore
import org.jooby.mongodb.Mongodb
import java.io.File
import java.io.StringWriter

class LorittaWebsite(val websiteUrl: String, val frontendFolder: String) : Kooby({
	port(4860) // Porta do website
	use(Mongodb()) // Usar extensão do MongoDB para o Jooby
	session(MongoSessionStore::class.java) // Usar session store para o MongoDB do Jooby
	assets("/**", File(frontendFolder, "static/").toPath())
	get("/**", { req, res ->
		println(req.path())
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
	}

	init {
		LorittaWebsite.WEBSITE_URL = websiteUrl
		LorittaWebsite.FOLDER = frontendFolder

		val fl = FileLoader()
		fl.prefix = frontendFolder
		LorittaWebsite.engine = PebbleEngine.Builder().cacheActive(false).strictVariables(true).loader(fl).build()
	}
}

inline fun evaluate(file: String, variables: MutableMap<String, Any?> = mutableMapOf<String, Any?>()): String {
	variables["websiteUrl"] = WEBSITE_URL
	val writer = StringWriter()
	LorittaWebsite.engine.getTemplate("$file").evaluate(writer, variables)
	return writer.toString()
}