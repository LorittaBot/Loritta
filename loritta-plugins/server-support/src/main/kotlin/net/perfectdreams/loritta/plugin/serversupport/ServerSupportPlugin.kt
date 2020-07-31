package net.perfectdreams.loritta.plugin.serversupport

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.editMessageIfContentWasChanged
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.serversupport.modules.PleaseDontMentionStaffModule
import net.perfectdreams.loritta.plugin.serversupport.modules.ServerSupportModule
import net.perfectdreams.loritta.plugin.serversupport.responses.FakeMessage
import net.perfectdreams.loritta.plugin.serversupport.responses.LorittaResponse
import net.perfectdreams.loritta.plugin.serversupport.responses.RegExResponse
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Paths
import java.util.*
import java.util.jar.Attributes
import java.util.jar.JarFile

class ServerSupportPlugin(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	var responses = listOf<LorittaResponse>()

	override fun onEnable() {
		super.onEnable()

		loritta as Loritta

		if (loritta.config.loritta.environment == EnvironmentType.CANARY) {
			// https://www.reddit.com/r/Kotlin/comments/8qdd4x/kotlin_script_engine_and_your_classpaths_what/
			val jar = JarFile(File("Loritta.jar"))
			val mf = jar.manifest
			val mattr = mf.mainAttributes
			// Yes, you SHOULD USE Attributes.Name.CLASS_PATH! Don't try using "Class-Path", it won't work!
			val manifestClassPath = mattr[Attributes.Name.CLASS_PATH] as String

			// The format within the Class-Path attribute is different than the one expected by the property, so let's fix it!
			// By the way, don't forget to append your original JAR at the end of the string!
			val clazz = LorittaLauncher::class.java
			val protectionDomain = clazz.protectionDomain
			val propClassPath = manifestClassPath.replace(" ", ":") + ":${Paths.get(protectionDomain.codeSource.location.toURI()).fileName}:plugins/server-support.jar"

			// Now we set it to our own classpath
			System.setProperty("kotlin.script.classpath", propClassPath)

			addMessageReceivedModule(ServerSupportModule(this))
			addMessageReceivedModule(PleaseDontMentionStaffModule(this))
			loadResponsesAndUpdateMessages(loritta)

			launch {
				while (true) {
					val channel = lorittaShards.getGuildById(297732013006389252L)
							?.getTextChannelById(398987569485971466L)

					if (channel != null) {
						val lastSentMessage = channel.history.retrievePast(1)
								.await()
								.firstOrNull()

						var resend = true
						if (lastSentMessage != null) {
							if (lastSentMessage.author.idLong == 395935916952256523L && lastSentMessage.contentRaw.contains("LEIA ANTES DE PERGUNTAR ALGO!")) {
								resend = false
							} else {
								// last message check
								val diff = System.currentTimeMillis() - (lastSentMessage.timeCreated.toEpochSecond() * 1000)

								resend = diff >= 300000L // Only resend after five minutes since the last message
							}
						}

						if (resend) {
							val replies = listOf(
									LoriReply(
											"<a:rat_jam:720643637033304442> **LEIA ANTES DE PERGUNTAR ALGO!** <a:rat_jam:720643637033304442>",
											prefix = "<:lori_nice:726845783344939028>",
											mentionUser = false
									),
									LoriReply(
											"**Se for uma dúvida sobre a Loritta:** Veja se a resposta da sua pergunta está no <#574308431029207060>! Caso não esteja lá, envie a sua pergunta aqui e, na mensagem, mencione o <@&399301696892829706>, nós iremos tentar te ajudar o mais breve possível!",
											"<:lori_ok:731873534036541500>",
											mentionUser = false
									),
									LoriReply(
											"**Lembre-se que aqui é o *suporte da Loritta*:** Nós não iremos te ajudar com problemas diversos (por exemplo: problemas em outros bots, problemas no Discord, etc)",
											"<a:lori_fight:731871119400894525>",
											mentionUser = false
									),
									LoriReply(
											"**Se você irá perguntar \"aaaah lori caiu <:smol_lori_putassa:395010059157110785>\":** Veja o <#610094449737072660> e as <#302976807135739916> para mais informações!",
											"<:smol_lori_putassa:395010059157110785>",
											mentionUser = false
									),
									LoriReply(
											"**Se você irá perguntar se algo foi mudado/adicionado/removido:** Veja as <#302976807135739916> para saber!",
											"<a:lori_dabbing:727888868711334287>",
											mentionUser = false
									)
							)

							channel.sendMessage(
									MessageBuilder()
											.setAllowedMentions(listOf(Message.MentionType.USER, Message.MentionType.CHANNEL, Message.MentionType.EMOTE))
											.setContent(replies.joinToString("\n", transform = { it.build() } ))
											.build()
							).await()
						}
					}

					delay(60_000)
				}
			}
		}
	}

	override fun onDisable() {
		super.onDisable()
	}

	fun loadResponsesAndUpdateMessages(loritta: Loritta) {
		GlobalScope.launch(loritta.coroutineDispatcher) {
			loadResponses(loritta)
			updateKnownResponses()
		}
	}

	fun loadResponses(loritta: Loritta) {
		logger.info("Carregando respostas para o módulo de suporte...")

		// Senão carrega em outro classloader e tudo dá problema
		val thisClazzLoader = javaClass
		val cl = thisClazzLoader.classLoader
		Thread.currentThread().contextClassLoader = cl

		val responses = mutableListOf<LorittaResponse>()

		val responsesFolder = File(dataFolder, "responses")

		if (!responsesFolder.exists())
			throw FileNotFoundException("Pasta de respostas não existe!")

		val requestSemaphore = Semaphore(4)

		val deferred = responsesFolder.listFiles().filter { it.extension == "kts" }.map { file ->
			GlobalScope.async(loritta.coroutineDispatcher) {
				try {
					// Senão carrega em outro classloader e tudo dá problema
					val cl = thisClazzLoader.classLoader
					Thread.currentThread().contextClassLoader = cl

					logger.info("Carregando ${file.name}...")
					val scriptContent = file.readText()
					val content = """
						import net.perfectdreams.loritta.plugin.serversupport.responses.*
						import com.mrpowergamerbr.loritta.Loritta
						import com.mrpowergamerbr.loritta.LorittaLauncher
						import com.mrpowergamerbr.loritta.commands.CommandContext
						import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
						import com.mrpowergamerbr.loritta.utils.loritta
						import com.mrpowergamerbr.loritta.utils.lorittaShards
						import com.mrpowergamerbr.loritta.utils.Constants
						import com.mrpowergamerbr.loritta.utils.LorittaImage
						import com.mrpowergamerbr.loritta.utils.toBufferedImage
						import com.mrpowergamerbr.loritta.utils.*
						import com.mrpowergamerbr.loritta.utils.locale.*
						import com.mrpowergamerbr.loritta.dao.*
						import com.mrpowergamerbr.loritta.tables.*
						import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
						import com.mrpowergamerbr.loritta.utils.LoriReply
						import java.awt.image.BufferedImage
						import java.io.File
						import javax.imageio.ImageIO
						import net.dv8tion.jda.api.entities.*
						import net.dv8tion.jda.api.*
						import java.util.regex.Pattern
						
						${scriptContent.replace("init {", """init {
							|val WHERE_IT_IS = "como|onde|qual.*(e|é)|existe|tem( )?jeito"
							|val ACTIVATE_OR_CHANGE = "pega|pego|coloc|clc|faço|faco|fasso|alter|boto|bota|alter"
							|
						""".trimMargin())}
						"""

					requestSemaphore.withPermit {
						val cl = thisClazzLoader.classLoader
						Thread.currentThread().contextClassLoader = cl

						val response = KtsObjectLoader().load<LorittaResponse>(content)
						logger.info("${file.name} foi carregado com sucesso!")
						response
					}
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

	suspend fun updateKnownResponses() {
		loritta as Loritta

		val channel = lorittaShards.getTextChannelById("703358497777123359") ?: return

		val messages = channel.history.retrievePast(100).await()

		val sortedResponsesByPriority = responses.sortedByDescending { it.getPriority() }

		val selfUser = channel.guild.jda.selfUser
		val serverConfig = loritta.getOrCreateServerConfig(channel.guild.idLong)
		val lorittaUser = LorittaUser(selfUser, EnumSet.noneOf(LorittaPermission::class.java), loritta.getOrCreateLorittaProfile(selfUser.idLong))

		// 		val author: User,
		//		val member: Member?,
		//		val message: Message,
		//		val messageId: String,
		//		val guild: Guild?,
		//		val channel: MessageChannel,
		//		val textChannel: TextChannel?,
		//		val serverConfig: ServerConfig,
		//		val legacyServerConfig: MongoServerConfig,
		//		val locale: LegacyBaseLocale,
		//		val lorittaUser: LorittaUser

		val builtMessages = sortedResponsesByPriority.map {
			buildString {
				this.append("**")
				this.append(it::class.simpleName)
				this.append("**")
				this.append("\n")

				if (it is RegExResponse) {
					it.patterns.forEach {
						this.append("`")
						this.append("$it")
						this.append("`")
						this.append(" ")
					}

					this.append("\n")
				}

				this.append("\n")
				this.append(
						it.getResponse(
								LorittaMessageEvent(
										channel.guild.jda.selfUser,
										channel.guild.selfMember,
										// uuuh, meow?
										FakeMessage(),
										"0",
										channel.guild,
										channel,
										channel,
										serverConfig,
										loritta.getLegacyLocaleById("default"),
										lorittaUser
								),
								"OwO whats this?"
						)?.split("\n")?.map { "> $it" }?.joinToString("\n")
				)
				this.append("\n")
			}
		}

		val availableMessages = messages.toMutableList()

		var currentMessage = ""
		for (message in builtMessages) {
			val oldMessage = currentMessage

			currentMessage += message
			currentMessage += "\n"

			if (currentMessage.length >= 2000) {
				val discordMessage = availableMessages.firstOrNull()
				discordMessage?.editMessageIfContentWasChanged(oldMessage) ?: channel.sendMessage(oldMessage).await()

				currentMessage = ""
				currentMessage += message
				currentMessage += "\n"

				if (discordMessage != null)
					availableMessages.removeAt(0)
			}
		}

		val message = availableMessages.firstOrNull()
		message?.editMessageIfContentWasChanged(currentMessage) ?: channel.sendMessage(currentMessage).await()

		if (message != null)
			availableMessages.removeAt(0)

		availableMessages.forEach {
			it.delete().queue()
		}
	}
}