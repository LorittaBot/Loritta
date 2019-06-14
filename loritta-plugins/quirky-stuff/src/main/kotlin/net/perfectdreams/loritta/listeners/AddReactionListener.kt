package net.perfectdreams.loritta.listeners

import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.io.File
import java.net.URL

class AddReactionListener : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
		if (event.channel.idLong != 583406099047252044L
				|| event.reactionEmote.idLong != 521721811298156558L
				|| event.member.idLong != 123170274651668480L)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val message = event.channel.retrieveMessageById(event.messageIdLong).await()

				val attachment = message.attachments.firstOrNull() ?: return@launch

				val userId = message.author.idLong

				val userName = message.author.name

				var artistId: String? = null

				logger.info { "Tentando adicionar fan art de $userId ($userName) - URL: ${attachment.url} " }

				for (it in File("/home/loritta_canary/test_website/fan_arts/").listFiles()) {
					if (it.extension == "conf") {
						val text = it.readText()
						if (text.contains("        id = \"$userId\"")) {
							logger.info { "Arquivo encontrado para $userId ($userName) - $it" }
							artistId = it.nameWithoutExtension
							break
						}
					}
				}

				artistId = artistId ?: userName.replace(Regex("[^a-zA-Z0-9]"), "")
						.toLowerCase()
						.trim()
						.replace(" ", "_")

				logger.info { "Artist ID para $userId ($userName) é $artistId" }

				val date = "${message.timeCreated.year}-${message.timeCreated.monthValue.toString().padStart(2, '0')}-${message.timeCreated.dayOfMonth.toString().padStart(2, '0')}"

				val fanArtUrl = attachment.url

				val ext = if (fanArtUrl.endsWith("jpg"))
					"jpg"
				else
					"png"

				val artistNameOnFiles = userName.replace(Regex("[^a-zA-Z0-9]"), "")
						.trim()
						.replace(" ", "_")

				logger.info { "Nome do arquivo para $userId ($userName) é $artistNameOnFiles" }

				val fanArtName = run {
					val first = File("/home/loritta/frontend/static/assets/img/fanarts/Loritta_-_$artistNameOnFiles.$ext")
					if (!first.exists())
						first.name
					else {
						var recursiveness = 2
						var f: File
						do {
							f = File("/home/loritta/frontend/static/assets/img/fanarts/Loritta_${recursiveness}_-_$artistNameOnFiles.$ext")
							recursiveness++
						} while (f.exists())

						f.name
					}
				}

				logger.info { "ID do usuário (Discord): $userId" }
				logger.info { "Nome do usuário: $userName" }
				logger.info { "ID do artista: $artistId" }
				logger.info { "Data da Fan Art: ${date}" }
				logger.info { "URL da Fan Art: $fanArtUrl" }
				logger.info { "Nome da Fan Art: $fanArtName" }

				val contents = URL(fanArtUrl).openConnection().getInputStream().readAllBytes()
				val imageFile = File("/home/loritta/frontend/static/assets/img/fanarts/$fanArtName").apply {
					this.writeBytes(contents)
				}

				logger.info { "Fan Art de $userId ($userName) - URL: ${attachment.url} foi salva em $imageFile!" }

				val artistFile = File("/home/loritta_canary/test_website/fan_arts/$artistId.conf")

				val fanArtSection = """    {
        |        file-name = "$fanArtName"
        |        created-at = "$date"
        |        tags = []
        |    }
    """.trimMargin()

				if (artistFile.exists()) {
					logger.info { "Arquivo do artista já existe! Vamos apenas inserir a fan art..." }
					val artistTemplate = artistFile.readText()
					val lines = artistTemplate.lines().toMutableList()

					val insertAt = lines.indexOf("]")
					lines.addAll(insertAt, fanArtSection.lines())

					// println("Isto está OK?")
					// println(lines.joinToString("\n"))
					// readLine()
					artistFile.writeText(lines.joinToString("\n"))
					// println("Finalizado! :3")
				} else {
					logger.info { "Criando um arquivo de artista para a fan art..." }

					val fullArtistTemplate = """id = "$artistId"

info {
    name = "$userName"
}

fan-arts = [
$fanArtSection
]

networks = [
    {
        type = "discord"
        id = "$userId"
    }
]
"""

					// println("Isto está OK?")
					// println(fullArtistTemplate)
					// readLine()
					artistFile.writeText(fullArtistTemplate)
					// println("Finalizado! :3")
				}

				event.user.openPrivateChannel().await().sendMessage("A incrível Fan Art foi adicionada com sucesso! :3").await()
			} catch (e: Exception) {
				logger.error(e) { "Erro ao adicionar fan art" }
			}
		}
	}
}