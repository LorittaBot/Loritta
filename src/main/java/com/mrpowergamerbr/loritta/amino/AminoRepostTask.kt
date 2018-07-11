package com.mrpowergamerbr.loritta.amino

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

class AminoRepostTask : Runnable {
	companion object {
		var storedLastIds = ConcurrentHashMap<String, String>()
		val logger = LoggerFactory.getLogger(AminoRepostTask::class.java)
	}

	override fun run() {
		// Carregar todos os server configs que tem o Amino Repost ativado
		val servers = loritta.serversColl.find(
				Filters.gt("aminoConfig.aminos", listOf<Any>())
		).iterator()

		// IDs das comunidades a serem verificados
		val communityIds = mutableSetOf<String>()
		val list = mutableListOf<ServerConfig>()

		val pattern = Regex("aminoapps\\.com/c/([A-z0-9\\-_]+)")
				.toPattern()

		servers.use {
			while (it.hasNext()) {
				val server = it.next()
				val aminoConfig = server.aminoConfig

				for (community in aminoConfig.aminos) {
					val matcher = pattern.matcher(community.inviteUrl)

					if (matcher.find()) {
						communityIds.add(matcher.group(1))
					}
				}

				list.add(server)
			}
		}

		logger.info("Existem ${communityIds.size} comunidades do Amino que eu irei verificar! Atualmente eu conheço ${storedLastIds.size} posts!")

		// Agora iremos verificar os canais
		val deferred = communityIds.map { communityId ->
			launch(loritta.coroutineDispatcher, start = CoroutineStart.LAZY) {
				try {
					logger.info("Verificando comunidade ${communityId}...")
					val connection = Jsoup.connect("https://aminoapps.com/c/$communityId/recent/")
							.userAgent(Constants.USER_AGENT)
							.ignoreHttpErrors(true)
							.execute()

					val statusCode = connection.statusCode()

					if (statusCode != 200) {
						logger.error("Erro ao verificar comunidade $communityId, status code: $statusCode")
						return@launch
					}

					val document = connection.parse()

					val listItems = document.getElementsByClass("list-item")

					var firstLink: String? = null

					val linksFound = mutableListOf<String>()

					val lastLoadedUrl = storedLastIds.getOrDefault(communityId, null)

					for (item in listItems) {
						val postLink = item.getElementsByTag("a")
								.first { !it.attr("href").contains("/user/") }
								.attr("href")

						try {
							if (postLink.isEmpty())
								continue

							if (firstLink == null) {
								firstLink = postLink
							}

							if (lastLoadedUrl == null) {
								storedLastIds.put(communityId, postLink)
								break
							} else if (lastLoadedUrl == postLink) {
								break
							}

							linksFound.add(postLink)
						} catch (e: Exception) {
							logger.error(postLink, e)
						}
					}

					if (firstLink == null) {
						logger.error("Erro ao verificar comunidade $communityId, firstLink == null")
						return@launch
					}

					storedLastIds[communityId] = firstLink

					val links = linksFound.reversed()

					for (link in links) {
						val post = try {
							Jsoup.connect(link).get()
						} catch (e: IllegalArgumentException) {
							continue
						}

						try {
							val titleDiv = post.getElementsByClass("main-post").first()

							val title = titleDiv.getElementsByTag("header").first().getElementsByTag("h3").text()
							val nickname = titleDiv.getElementsByClass("overflow-hidden").first().getElementsByClass("nickname").text()
							val avatar = titleDiv.getElementsByTag("section").first().getElementsByClass("avatar").firstOrNull()?.attr("data-src")

							val richContent = titleDiv.getElementsByAttributeValue("data-vce", "post-content-body").first()

							if (richContent == null) {
								logger.error("Post não tem post-content-body! $link")
								continue
							}

							val firstImage = richContent.getElementsByTag("img").firstOrNull()
							val imageUrl = firstImage?.attr("src")

							for (server in list) {
								for (aminoInfo in server.aminoConfig.aminos.filter {
									val matcher = pattern.matcher(it.inviteUrl)
									if (matcher.find())
										matcher.group(1) == communityId
									else
										false
								}) {
									val guild = lorittaShards.getGuildById(server.guildId) ?: return@launch

									val textChannel = guild.getTextChannelById(aminoInfo.repostToChannelId)
											?: return@launch

									if (!textChannel.canTalk())
										continue

									// Enviar mensagem
									val embed = EmbedBuilder().apply {
										val avatarUrl = "https:$avatar"
										if (avatarUrl.length > MessageEmbed.URL_MAX_LENGTH || !EmbedBuilder.URL_PATTERN.matcher(avatarUrl).matches()) {
											setAuthor(nickname, null, null)
										} else {
											setAuthor(nickname, null, avatarUrl)
										}

										setTitle("<:amino:375313236234469386> $title", link)
										setDescription(richContent.text().substringIfNeeded())

										if (imageUrl != null) {
											try {
												if (imageUrl.startsWith("http")) {
													setImage(imageUrl)
												} else {
													setImage("https:$imageUrl")
												}
											} catch (e: IllegalArgumentException) {
											} // Se a imagem não é um link http ou https, vamos ignorar para não dar nenhum erro
										}

										setColor(Color(255, 112, 125))

										setFooter(communityId, null)
									}
									textChannel.sendMessage(embed.build()).complete()
								}
							}
						} catch (e: Exception) {
							logger.error("Erro ao verificar post $link de $communityId", e)
						}
					}
				} catch (e: Exception) {
					logger.error(communityId, e)
				}
			}
		}

		runBlocking {
			deferred.onEach {
				it.join()
			}
		}
	}
}