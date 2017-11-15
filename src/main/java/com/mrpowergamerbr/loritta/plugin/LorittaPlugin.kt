package com.mrpowergamerbr.loritta.plugin

import com.mrpowergamerbr.loritta.commands.CommandBase
import java.net.URLClassLoader

open class LorittaPlugin() {
	var classLoader: URLClassLoader? = null
	var name: String = "???"

	open fun onEnable() {

	}

	open fun onDisable() {

	}

	open fun getCommands(): List<CommandBase>{
		return listOf<CommandBase>()
	}
}