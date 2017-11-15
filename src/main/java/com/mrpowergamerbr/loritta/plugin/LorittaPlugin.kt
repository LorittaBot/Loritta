package com.mrpowergamerbr.loritta.plugin

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import java.io.File
import java.net.URLClassLoader

open class LorittaPlugin() {
	var classLoader: URLClassLoader? = null
	var name: String = "???"
	val dataFolder: File
		get() = File(Loritta.config.pluginFolder + "$name/")

	open fun onEnable() {

	}

	open fun onDisable() {

	}

	open fun getCommands(): List<CommandBase> = listOf<CommandBase>()
}