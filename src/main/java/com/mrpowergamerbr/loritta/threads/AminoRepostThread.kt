package com.mrpowergamerbr.loritta.threads

import com.mrpowergamerbr.aminoreapi.AminoClient
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LORITTA_SHARDS
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color

class AminoRepostThread : Thread("Amino Repost Thread") {
	var storedLastIds = HashMap<String, MutableSet<String>>();

	override fun run() {
		super.run()

		// Logar na conta da Loritta no Amino
		var aminoClient = AminoClient(Loritta.config.aminoEmail, Loritta.config.aminoPassword, Loritta.config.aminoDeviceId);
		aminoClient.login();

		while (true) {
			try {
				checkRepost(aminoClient);
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(10000);
		}
	}

	fun checkRepost(aminoClient: AminoClient) {
		if (true) { return }
		// Carregar todos os server configs que tem o Amino Repost ativado
		var servers = loritta.ds
				.find(ServerConfig::class.java)
				.field("aminoConfig.aminos")
				.exists()

		// IDs das comunidades a serem verificados
		var communityIds = mutableSetOf<String>()

		for (server in servers) {
			val aminoConfig = server.aminoConfig

			for (community in aminoConfig.aminos) {
				if (community.communityId == null)
					continue

				communityIds.add(community.communityId!!)
			}
		}

		// Agora iremos verificar os canais
		val deferred = communityIds.map { communityId ->
			launch {
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

					for (server in servers) {
						for (aminoInfo in server.aminoConfig.aminos.filter { it.communityId == communityId }) {
							val guild = LORITTA_SHARDS.getGuildById(server.guildId) ?: return@launch

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
			}
		}

		runBlocking {
			deferred.onEach {
				it.join()
			}
		}
	}
}