package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.listeners.DiscordListener
import com.mrpowergamerbr.loritta.listeners.EventLogListener
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
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

	override fun getDescription(): String {
		return "Recarrega a Loritta"
	}

	override fun run(context: CommandContext) {
		val json = FileUtils.readFileToString(File("./config.json"), "UTF-8")
		val config = Loritta.gson.fromJson(json, LorittaConfig::class.java)
		Loritta.config = config

		LorittaLauncher.getInstance().morphia = Morphia()
		LorittaLauncher.getInstance().ds = LorittaLauncher.getInstance().morphia.createDatastore(LorittaLauncher.getInstance().mongo, "loritta")
		LorittaLauncher.getInstance().generateDummyServerConfig()
		LorittaLauncher.loritta.loadCommandManager()
		loritta.loadServersFromFanClub()
		loritta.loadLocales()

		if (context.args.isNotEmpty() && context.args[0] == "listeners") {
			context.sendMessage("Recarregando listeners...")

			// Desregistrar listeners
			LorittaLauncher.loritta.lorittaShards.shards.forEach {
				val shard = it;
				it.registeredListeners.forEach {
					shard.removeEventListener(it)
				}
			}

			val discordListener = DiscordListener(LorittaLauncher.loritta)
			val eventLogListener = EventLogListener(LorittaLauncher.loritta)

			// Registrar novos listeners
			LorittaLauncher.loritta.lorittaShards.shards.forEach {
				it.addEventListener(discordListener)
				it.addEventListener(eventLogListener)
			}
		}

		val serverConfig = loritta.getServerConfigForGuild("167278734049673216")
		serverConfig.nsfwFilterConfig.isEnabled = true
		serverConfig.nsfwFilterConfig.reportOnChannelId = "315566454454091777"
		serverConfig.nsfwFilterConfig.removeMessage = true
		serverConfig.nsfwFilterConfig.warnMessage = "<:Wilson:304231637263319042> **|** {@user} Que feio, pra que enviar imagens obscenas no chat? Queria saber o que seus pais achariam se tivessem visto você fazendo isso. \uD83D\uDE22 (Se foi um falso positivo, então me ignore...)"
		serverConfig.nsfwFilterConfig.reportMessage = "@here **{@user} enviou uma imagem polêmica no chat!** (na verdade não tenho certeza... sei lá galerinha, analisa aí pfv)\n\nLink da imagem: <{url}>"

		loritta save serverConfig

		context.sendMessage("Loritta recarregada com sucesso!")
	}
}