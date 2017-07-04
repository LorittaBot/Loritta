package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import org.apache.commons.io.FileUtils
import org.mongodb.morphia.Morphia

import java.io.File

class ReloadCommand : CommandBase() {
	override fun getLabel(): String {
		return "reload"
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MAGIC
	}

	override fun onlyOwner(): Boolean {
		return true
	}

	override fun run(context: CommandContext) {
		val json = FileUtils.readFileToString(File("./config.json"), "UTF-8")
		val config = Loritta.gson.fromJson(json, LorittaConfig::class.java)
		LorittaLauncher.loritta.loadFromConfig(config)

		LorittaLauncher.getInstance().morphia = Morphia()
		LorittaLauncher.getInstance().ds = LorittaLauncher.getInstance().morphia.createDatastore(LorittaLauncher.getInstance().mongo, "loritta")
		LorittaLauncher.loritta.loadCommandManager()
		context.sendMessage("Loritta recarregada com sucesso!")
	}
}