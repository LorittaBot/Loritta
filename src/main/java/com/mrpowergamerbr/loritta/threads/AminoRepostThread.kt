package com.mrpowergamerbr.loritta.threads

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.aminoreapi.AminoClient
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import net.dv8tion.jda.core.EmbedBuilder
import org.slf4j.LoggerFactory
import java.awt.Color

class AminoRepostThread : Thread("Amino Repost Thread") {
	companion object {
		var storedLastIds = HashMap<String, MutableSet<String>>();
		val logger = LoggerFactory.getLogger(AminoRepostThread::class.java)
	}

	override fun run() {
		super.run()

		// Logar na conta da Loritta no Amino
		var aminoClient = AminoClient(Loritta.config.aminoEmail, Loritta.config.aminoPassword, Loritta.config.aminoDeviceId);
		aminoClient.login();

		while (true) {
			try {
				checkRepost(aminoClient);
			} catch (e: Exception) {
				logger.error("Erro ao verificar novos posts no Amino!", e)
			}
			Thread.sleep(10000);
		}
	}

	fun checkRepost(aminoClient: AminoClient) {
		// Carregar todos os server configs que tem o Amino Repost ativado
		val servers = loritta.serversColl.find(
				Filters.gt("aminoConfig.aminos", listOf<Any>())
		).iterator()

		// IDs das comunidades a serem verificados
		var communityIds = mutableSetOf<String>()
		val list = mutableListOf<ServerConfig>()

		servers.use {
			while (it.hasNext()) {
				val server = it.next()
				val aminoConfig = server.aminoConfig

				for (community in aminoConfig.aminos) {
					if (community.communityId == null)
						continue

					communityIds.add(community.communityId!!)
				}
				list.add(server)
			}
		}

		// Agora iremos verificar os canais
		val deferred = communityIds.map { communityId ->
			launch {
				try {
					var community = aminoClient.getCommunityById(communityId) ?: return@launch

					try {
						community.join(communityId)
					} catch (e: Exception) {
						try {
							community.join();
						} catch (e: Exception) {
							e.printStackTrace()
						}
					}

					var posts = community.getBlogFeed(0, 5)

					val postsIds = storedLastIds.getOrPut(communityId, { mutableSetOf() })

					for (post in posts) {
						if (postsIds.contains(post.blogId))
							continue

						for (server in list) {
							for (aminoInfo in server.aminoConfig.aminos.filter { it.communityId == communityId }) {
								val guild = lorittaShards.getGuildById(server.guildId) ?: return@launch

								val textChannel = guild.getTextChannelById(aminoInfo.repostToChannelId) ?: return@launch

								if (!textChannel.canTalk())
									return@launch

								// Enviar mensagem
								var embed = EmbedBuilder().apply {
									setAuthor(post.author.nickname, null, post.author.icon)
									setTitle(post.title)
									setDescription(post.content)
									setColor(Color(255, 112, 125))

									/* if (post.mediaList != null) {
									var obj = post.mediaList ?: ;
									var inside = obj[0];

									if (inside is List<*>) {
										var link = inside.get(1) as String;

										if (link.contains("narvii.com") && (link.endsWith("jpg") || link.endsWith("png") || link.endsWith("gif"))) {
											setImage(link);
										}
									}
								} */
									setFooter("Enviado as " + post.modifiedTime, null);
								}
								textChannel.sendMessage(embed.build()).complete()
							}
						}
					}
					postsIds.clear()

					posts.forEach {
						postsIds.add(it.blogId)
					}
				} catch (e: Exception) {

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