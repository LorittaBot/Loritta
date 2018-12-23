package com.mrpowergamerbr.loritta.plugin

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.commands.loritta.LorittaCommand
import java.io.File
import java.net.URLClassLoader

open class LorittaPlugin(val name: String, val classLoader: URLClassLoader, val pluginFile: File) {
	val commands = mutableListOf<LorittaCommand>()
	val dataFolder = File(Loritta.FOLDER, "plugins/$name")

	open fun onEnable() {

	}

	open fun onDisable() {

	}

	fun registerCommand(command: LorittaCommand) {
		loritta.lorittaCommandManager.registerCommand(command)
		commands.add(command)
	}
}