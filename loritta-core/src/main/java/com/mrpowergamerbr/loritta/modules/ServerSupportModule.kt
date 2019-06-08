package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.KtsObjectLoader
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.response.LorittaResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.File
import java.io.FileNotFoundException

class ServerSupportModule : MessageReceivedModule {
	companion object {
		var responses = listOf<LorittaResponse>()
		private val logger = KotlinLogging.logger {}

		fun loadResponses() {
			logger.info("Carregando respostas para o módulo de suporte...")
			val responses = mutableListOf<LorittaResponse>()

			val responsesFolder = File("./responses")

			if (!responsesFolder.exists())
				throw FileNotFoundException("Pasta de respostas não existe!")

			val deferred = responsesFolder.listFiles().filter { it.extension == "kts" }.map { file ->
				GlobalScope.async(loritta.coroutineDispatcher) {
					try {
						logger.info("Carregando ${file.name}...")
						val scriptContent = file.readText()
						val content = """
						import com.mrpowergamerbr.loritta.utils.response.*
						import com.mrpowergamerbr.loritta.utils.response.responses.*
						import com.mrpowergamerbr.loritta.Loritta
						import com.mrpowergamerbr.loritta.LorittaLauncher
						import com.mrpowergamerbr.loritta.commands.CommandContext
						import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
						import com.mrpowergamerbr.loritta.utils.loritta
						import com.mrpowergamerbr.loritta.utils.lorittaShards
						import com.mrpowergamerbr.loritta.utils.save
						import com.mrpowergamerbr.loritta.utils.Constants
						import com.mrpowergamerbr.loritta.utils.LorittaImage
						import com.mrpowergamerbr.loritta.utils.toBufferedImage
						import com.mrpowergamerbr.loritta.utils.*
						import com.mrpowergamerbr.loritta.utils.locale.*
						import com.mrpowergamerbr.loritta.dao.*
						import com.mrpowergamerbr.loritta.tables.*
						import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth.*
						import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
						import com.mrpowergamerbr.loritta.utils.LoriReply
						import java.awt.image.BufferedImage
						import java.io.File
						import javax.imageio.ImageIO
						import net.dv8tion.jda.api.entities.*
						import net.dv8tion.jda.api.*
						import java.util.regex.Pattern

						$scriptContent
						"""
						val response = KtsObjectLoader().load<LorittaResponse>(content)
						logger.info("${file.name} foi carregado com sucesso!")
						response
					} catch (e: Exception) {
						logger.error(e) { "Erro ao carregar ${file.name}" }
						null
					}
				}
			}

			runBlocking {
				deferred.onEach {
					val response = it.await()
					if (response != null)
						responses.add(response)
				}
			}

			this.responses = responses.sortedByDescending { it.getPriority() }
			logger.info("${responses.size} respostas automáticas foram carregadas com sucesso!")
		}
	}

	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		return (event.channel.id == "398987569485971466" || event.channel.id == "393332226881880074") && loritta.config.loritta.environment == EnvironmentType.CANARY
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: MongoServerConfig, locale: LegacyBaseLocale): Boolean {
		val content = event.message.contentRaw
				.replace("\u200B", "")
				.replace("\\", "")
				.toLowerCase()

		for (response in responses) {
			if (response.handleResponse(event, content)) {
				event.channel.sendMessage(response.getResponse(event, content)!!).queue()
				return false
			}
		}

		return false
	}
}