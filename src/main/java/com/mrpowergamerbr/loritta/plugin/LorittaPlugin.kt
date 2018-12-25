package com.mrpowergamerbr.loritta.plugin

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.LorittaCommand
import java.io.File
import java.net.URLClassLoader

open class LorittaPlugin {
	lateinit var name: String
	lateinit var classLoader: URLClassLoader
	lateinit var pluginFile: File

	val commands = mutableListOf<LorittaCommand>()
	val dataFolder by lazy { File(Loritta.FOLDER, "plugins/$name") }

	open fun onEnable() {

	}

	open fun onDisable() {

	}

	fun registerCommand(command: LorittaCommand) {
		loritta.commandManager.registerCommand(command)
		commands.add(command)
	}
}