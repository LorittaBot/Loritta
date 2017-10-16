package com.mrpowergamerbr.loritta.threads

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.aminoreapi.AminoClient
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color

class AminoRepostThread : Thread("Amino Repost Thread") {
	var storedLastIds = HashMap<String, String>();

	override fun run() {
		super.run()

		// Logar na conta da Loritta no Amino
		var aminoClient = AminoClient(Loritta.config.aminoEmail, Loritta.config.aminoPassword, Loritta.config.aminoDeviceId);
		aminoClient.login();

		while (true) {
			checkRepost(aminoClient);
			Thread.sleep(10000);
		}
	}

	fun checkRepost(aminoClient: AminoClient) {
		try {
			// Carregar todos os server configs que tem o Amino Repost ativado
			var servers = LorittaLauncher.loritta.mongo
					.getDatabase("loritta")
					.getCollection("servers")
					.find(Filters.and(Filters.eq("aminoConfig.isEnabled", true), Filters.eq("aminoConfig.syncAmino", true)))

			for (server in servers) {
				var config = LorittaLauncher.loritta.ds.get(ServerConfig::class.java, server.get("_id"));

				var aminoConfig = config.aminoConfig;

				if (aminoConfig.isEnabled && aminoConfig.syncAmino) { // Está ativado? (Nem sei para que verificar de novo mas vai que né)
					var guild = LorittaLauncher.loritta.lorittaShards.getGuildById(config.guildId)

					if (guild != null) {
						for (amino in aminoConfig.aminos) {
							var textChannel = guild.getTextChannelById(amino.repostToChannelId)

							if (textChannel != null) { // Wow, diferente de null!
								if (textChannel.canTalk()) { // Eu posso falar aqui? Se sim...
									// Vamos fazer polling dos posts então!
									var communityId = amino.communityId

									if (communityId == null)
										continue

									// E agora nós iremos fazer o polling de verdade
									var community = aminoClient.getCommunityById(communityId);

									try {
										community.join(communityId)
									} catch (e: Exception) {
										try {
											community.join();
										} catch (e: Exception) {
											e.printStackTrace()
										}
									}

									var posts = community.getBlogFeed(0, 5);

									var lastIdSent = storedLastIds.getOrDefault(config.guildId, null);

									for (post in posts) {
										if (post.blogId == lastIdSent) {
											break;
										}
										// Enviar mensagem
										var embed = EmbedBuilder().apply {
											setAuthor(post.author.nickname, null, post.author.icon)
											setTitle(post.title)
											setDescription(post.content)
											setColor(Color(255, 112, 125))

											if (post.mediaList != null) {
												var obj = post.mediaList;
												var inside = obj[0];

												if (inside is List<*>) {
													var link = inside.get(1) as String;

													if (link.contains("narvii.com") && (link.endsWith("jpg") || link.endsWith("png") || link.endsWith("gif"))) {
														setImage(link);
													}
												}
											}
											setFooter("Enviado as " + post.modifiedTime, null);
										}
										textChannel.sendMessage(embed.build()).complete()
									}

									if (posts.isNotEmpty()) {
										storedLastIds.put(config.guildId, posts[0].blogId)
									}
								}
							}
						}
					}
				}
			}
		} catch (e: Exception) {
			e.printStackTrace();
		}
	}
}