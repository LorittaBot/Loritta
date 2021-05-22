package net.perfectdreams.loritta.plugin.stafflorittaban.listeners

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.vanilla.administration.AdminUtils
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.sendMessageAsync
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.legacy.entities.jda.JDAUser
import net.perfectdreams.loritta.plugin.stafflorittaban.StaffLorittaBanConfig
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

class CheckReactionsForPrivateSpamListener(val config: StaffLorittaBanConfig) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
		if (!config.enabled
				|| !event.reactionEmote.isEmote
				|| event.guild.idLong != 297732013006389252L
				|| !event.member.roles.any { it.idLong == 351473717194522647L })
			return

		if (event.reactionEmote.idLong != 694273286284116029L)
			return
		
		GlobalScope.launch(loritta.coroutineDispatcher) {
			val message = event.channel.retrieveMessageById(event.messageIdLong)
					.await() ?: return@launch

			val firstImage = message.attachments.firstOrNull() ?: return@launch

			val image = LorittaUtils.downloadImage(firstImage.url) ?: return@launch

			val description = firstImage.retrieveInputStream().await().use {
				val baos = ByteArrayOutputStream()
				ImageIO.write(image, "png", baos)

				val json = jsonObject(
						"requests" to jsonArray(
								jsonObject(
										"features" to jsonArray(
												jsonObject(
														"maxResults" to 1,
														"type" to "TEXT_DETECTION"
												)
										),
										"image" to jsonObject(
												"content" to Base64.getEncoder().encodeToString(baos.toByteArray())
										)
								)
						)
				)

				val response = loritta.http.post<io.ktor.client.statement.HttpResponse>("https://content-vision.googleapis.com/v1/images:annotate?key=${loritta.config.googleVision.apiKey}&alt=json") {
					contentType(ContentType.Application.Json)
					userAgent("Google-API-Java-Client Google-HTTP-Java-Client/1.21.0 (gzip)")

					body = gson.toJson(json)
				}

				val body = response.receive<String>()

				val parsedResponse = JsonParser.parseString(body)

				try {
					parsedResponse["responses"][0]["textAnnotations"][0]["description"].string
				} catch (e: Exception) { null }
			}

			val split = message.contentRaw.split(" ")
			val foundUsersFromMessageInGuild = mutableListOf<Member>()
			val foundUsersFromImageInGuild = mutableListOf<Member>()

			val isInviteImage = description?.contains("VOCÊ FOI CONVIDADO", true) == true || description?.contains("BEEN INVITED TO", true) == true || description?.contains("YOU RECEIVED AN INVITE", true) == true || description?.contains("VOCÊ RECEBEU UM CONVITE", true) == true || description?.contains("discord.gg", true) == true

			for (str in split) {
				if (str.isNotBlank()) {
					if (str.isValidSnowflake()) {
						val member = event.guild.retrieveMemberById(str.toLong()).await() ?: continue
						foundUsersFromMessageInGuild.add(member)
					}

					if (str.contains("#")) {
						val member = event.guild.getMemberByTag(str) ?: continue
						foundUsersFromMessageInGuild.add(member) ?: continue
					}
				}
			}

			if (description != null) {
				val lineWithUsername = description.lines().firstOrNull { (it.contains("Hoje") || it.contains("Today") || it.contains("Ontem") || it.contains("Yesterday")) && it.contains(":") }
				if (lineWithUsername != null) {
					val username = lineWithUsername.split(Regex("Hoje|Today|Ontem|Yesterday")).first().trim()

					// Para pesquisar, vamos começar das primeiras palavras até o final, assim é possível detectar mesmo se tenha algo errado no nome
					if (username.isNotBlank()) {
						val splitUsername = username.split(" ")

						var detectedFromSearch = listOf<Member>()

						repeat(splitUsername.size) {
							val take = splitUsername.take(it + 1)
							val search = event.guild.members.filter { it.user.name.startsWith(take.joinToString(" "), true) }
							if (search.isEmpty())
								return@repeat
							detectedFromSearch = search
						}

						foundUsersFromImageInGuild += detectedFromSearch
					}
				}
			}

			val whoWillBeBanned = foundUsersFromMessageInGuild.firstOrNull() ?: foundUsersFromImageInGuild.firstOrNull()
			val canBan = whoWillBeBanned != null

			val content = listOf(
					LorittaReply(
							"**Análise**",
							"<:analise:694273286284116029>"
					),
					LorittaReply(
							"A imagem tem um convite? $isInviteImage",
							prefix = "\uD83D\uDDBC",
							mentionUser = false
					),
					LorittaReply(
							"Membros detectados pela mensagem: ${foundUsersFromMessageInGuild.joinToString(transform = { "`" + transformToString(it) + "`" })}",
							prefix = "<:wumpus_basic:516315292821880832>",
							mentionUser = false
					),
					LorittaReply(
							"Membros detectados pela imagem: ${foundUsersFromImageInGuild.joinToString(transform = { "`" + transformToString(it) + "`" })}",
							prefix = "<:loritta:331179879582269451>",
							mentionUser = false
					),
					if (canBan && whoWillBeBanned != null) {
						LorittaReply(
								"Clique para confirmar o ban de: `${transformToString(whoWillBeBanned)}`",
								prefix = "<:vsf_ban:632363628539936804>",
								mentionUser = false
						)
					} else {
						LorittaReply(
								"Nenhum usuário detectado... será que ele está no servidor?",
								mentionUser = false
						)
					}
			).joinToString("\n", transform = { it.build(JDAUser(event.user)) })

			val msg = event.channel.sendMessageAsync(
					content
			)

			if (canBan && whoWillBeBanned != null) {
				msg.onReactionAddByAuthor(event.userIdLong) {
					val settings = AdminUtils.retrieveModerationInfo(loritta.getOrCreateServerConfig(event.guild.idLong))
					BanCommand.ban(settings, event.guild, event.user, loritta.localeManager.getLocaleById("default"), whoWillBeBanned.user, "Não é permitido divulgar conteúdos sem que a equipe permita, isto inclui divulgar via mensagem direta para outras pessoas e no seu nome/nickname. Enviar convites na DM é considerado spam e é contra os termos de uso do Discord! ${firstImage.url}", false, 0)
					event.channel.sendMessage("Usuário banido com sucesso! :3").queue()
				}

				msg.addReaction("sad_cat_thumbs_up:686370257308483612").queue()
			}
		}
	}

	fun transformToString(member: Member) = "${member.user.name}#${member.user.discriminator} (${member.user.idLong})"
}