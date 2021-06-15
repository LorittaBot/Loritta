package net.perfectdreams.loritta.listeners

import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.sendMessageAsync
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.QuirkyConfig
import net.perfectdreams.loritta.utils.Emotes
import java.io.File
import java.net.URL

class AddReactionListener(val config: QuirkyConfig) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
		if (!config.addFanArts.enabled
				|| event.channel.idLong !in config.addFanArts.channels
				|| event.reactionEmote.idLong != config.addFanArts.emoteId
				|| !loritta.config.isOwner(event.member.idLong))
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val message = event.channel.retrieveMessageById(event.messageIdLong).await()

				val attachment = message.attachments.firstOrNull() ?: return@launch

				val userId = message.author.idLong

				val userName = message.author.name

				var artistId: String? = null

				logger.info { "Tentando adicionar fan art de $userId ($userName) - URL: ${attachment.url} " }

				for (it in File(loritta.instanceConfig.loritta.folders.fanArts).listFiles()) {
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

				if (artistId.isBlank())
					artistId = userId.toString()

				logger.info { "Artist ID para $userId ($userName) é $artistId" }

				val date = "${message.timeCreated.year}-${message.timeCreated.monthValue.toString().padStart(2, '0')}-${message.timeCreated.dayOfMonth.toString().padStart(2, '0')}"

				val fanArtUrl = attachment.url

				val ext = if (fanArtUrl.endsWith("jpg"))
					"jpg"
				else if (fanArtUrl.endsWith("gif"))
					"gif"
				else
					"png"

				var artistNameOnFiles = userName.replace(Regex("[^a-zA-Z0-9]"), "")
						.trim()
						.replace(" ", "_")

				if (artistNameOnFiles.isBlank())
					artistNameOnFiles = userId.toString()

				logger.info { "Nome do arquivo para $userId ($userName) é $artistNameOnFiles" }

				val fanArtName = run {
					val first = File(config.addFanArts.fanArtFiles, "Loritta_-_$artistNameOnFiles.$ext")
					if (!first.exists())
						first.name
					else {
						var recursiveness = 2
						var f: File
						do {
							f = File(config.addFanArts.fanArtFiles, "Loritta_${recursiveness}_-_$artistNameOnFiles.$ext")
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
				val imageFile = File(config.addFanArts.fanArtFiles, fanArtName).apply {
					this.writeBytes(contents)
				}

				logger.info { "Fan Art de $userId ($userName) - URL: ${attachment.url} foi salva em $imageFile!" }

				val artistFile = File(loritta.instanceConfig.loritta.folders.fanArts, "$artistId.conf")

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

					artistFile.writeText(lines.joinToString("\n"))
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

					artistFile.writeText(fullArtistTemplate)
				}

				loritta.loadFanArts()

				val fanArtArtist = loritta.fanArtArtists.first { it.id == artistId }

				val userMessage = StringBuilder()
				userMessage.append("A sua Fan Art (<https://loritta.website/assets/img/fanarts/$fanArtName>) foi adicionada no website! ${Emotes.LORI_TEMMIE}")
				userMessage.append("\n\n")
				userMessage.append("Aonde será que eu irei colocar a sua fan art... Talvez eu irei colocar ${config.addFanArts.placesToPlaceStuff.random()}!")
				userMessage.append("\n\n")
				userMessage.append("Você agora tem ${fanArtArtist.fanArts.size} fan art${if (fanArtArtist.fanArts.size != 1) { "s" } else ""} no meu website! <https://loritta.website/fanarts>")
				userMessage.append("\n\n")

				val fanArtArtistGuildMember = message.member
				if (fanArtArtistGuildMember != null) {
					val role = event.guild.getRoleById(config.addFanArts.firstFanArtRoleId)

					if (role != null) {
						if (fanArtArtistGuildMember.roles.contains(role)) {
							userMessage.append("Obrigada por ser uma pessoa incrível!! Te amooo!! (como amiga, é clarooo!) ${Emotes.LORI_HAPPY}")
							userMessage.append("\n\n")
							userMessage.append("Agora você tem permissão para mandar mais fan arts para mim em <#583406099047252044>, mandar outros desenhos fofis em <#510601125221761054> e conversar com outros artistas em <#574387310129315850>! ${Emotes.LORI_OWO}")
							event.guild.removeRoleFromMember(fanArtArtistGuildMember, role).await()

							// Enviar a fan art no canal de fan arts da Lori
							val channel = event.guild.getTextChannelById(583406099047252044L)

							channel?.sendMessageAsync(
									"Fan Art de <@$userId> <:lori_heart:640158506049077280> https://loritta.website/assets/img/fanarts/$fanArtName"
							)
						} else {
							userMessage.append("Obrigada por ser uma pessoa incrível e por continuar a fazer fan arts de mim (tô até emocionada ${Emotes.LORI_CRYING})... Te amooo!! (como amiga, é clarooo!) ${Emotes.LORI_HAPPY}")
						}
					}
				}
				userMessage.append("\n\n")
				userMessage.append("Sério, obrigada pela fan art, continue assim e continue a transformar o mundo em um lugar melhor! ${Emotes.LORI_HEART}")

				if (event.channel.idLong == config.addFanArts.firstFanArtChannelId)
					message.delete().await()

				event.user.openPrivateChannel().await().sendMessage("A incrível Fan Art foi adicionada com sucesso! :3 https://loritta.website/assets/img/fanarts/$fanArtName").await()

				try {
					fanArtArtistGuildMember?.user?.openPrivateChannel()?.await()?.sendMessageAsync(userMessage.toString())
				} catch (e: Exception) {}
			} catch (e: Exception) {
				logger.error(e) { "Erro ao adicionar fan art" }
			}
		}
	}
}