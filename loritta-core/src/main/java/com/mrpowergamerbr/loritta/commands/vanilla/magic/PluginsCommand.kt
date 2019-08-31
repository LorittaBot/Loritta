package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.commands.LorittaCommandContext
import java.io.File

class PluginsCommand : LorittaCommand(arrayOf("plugins"), category = CommandCategory.MAGIC) {
	override val onlyOwner = true

	@Subcommand
	suspend fun pluginList(context: LorittaCommandContext) {
		context.reply(
				LoriReply(
						"**Plugins (${loritta.pluginManager.plugins.size}):** ${loritta.pluginManager.plugins.joinToString(", ", transform = { it.name })}"
				)
		)
	}

	@Subcommand(["load"])
	suspend fun load(context: LorittaCommandContext, pluginName: String) {
		context.reply(
				LoriReply(
						"Carregando plugin `$pluginName.jar`..."
				)
		)

		loritta.pluginManager.loadPlugin(File(loritta.instanceConfig.loritta.folders.plugins, "$pluginName.jar"))
		context.reply(
				LoriReply(
						"Finalizado, yay!"
				)
		)
	}

	@Subcommand(["unload"])
	suspend fun unload(context: LorittaCommandContext, pluginName: String) {
		val plugin = loritta.pluginManager.getPlugin(pluginName)

		if (plugin == null) {
			context.reply(
					LoriReply(
							"Plugin não existe! Como você vai descarregar algo que não existe?"
					)
			)
			return
		}

		context.reply(
				LoriReply(
						"Descarregando plugin `$pluginName.jar`..."
				)
		)

		loritta.pluginManager.unloadPlugin(plugin)

		context.reply(
				LoriReply(
						"Finalizado, yay!"
				)
		)
	}

	@Subcommand(["reload"])
	suspend fun reload(context: LorittaCommandContext, pluginName: String) {
		val plugin = loritta.pluginManager.getPlugin(pluginName)

		if (plugin == null) {
			context.reply(
					LoriReply(
							"Plugin não existe! Como você vai recarregar algo que não existe?"
					)
			)
			return
		}

		context.reply(
				LoriReply(
						"Recarregando plugin `$pluginName.jar`..."
				)
		)

		loritta.pluginManager.unloadPlugin(plugin)
		loritta.pluginManager.loadPlugin(plugin.pluginFile)

		context.reply(
				LoriReply(
						"Finalizado, yay!"
				)
		)
	}
}