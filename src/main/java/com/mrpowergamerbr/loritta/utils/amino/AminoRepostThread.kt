package com.mrpowergamerbr.loritta.utils.amino

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.aminoreapi.AminoClient
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import net.dv8tion.jda.core.EmbedBuilder
import org.jsoup.Jsoup
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
			Thread.sleep(5000);
		}
	}

	fun checkRepost(aminoClient: AminoClient) {
		try {
			// Carregar todos os server configs que tem o Amino Repost ativado
			var servers = LorittaLauncher.loritta.mongo
					.getDatabase("loritta")
					.getCollection("servers")
					.find(Filters.eq("aminoConfig.isEnabled", true))

			for (server in servers) {
				var config = LorittaLauncher.loritta.ds.get(ServerConfig::class.java, server.get("_id"));

				var aminoConfig = config.aminoConfig;

				if (aminoConfig.isEnabled) { // Está ativado? (Nem sei para que verificar de novo mas vai que né)
					var guild = LorittaLauncher.loritta.lorittaShards.getGuildById(config.guildId)

					if (guild != null) {
						var textChannel = guild.getTextChannelById(aminoConfig.repostToChannelId);

						if (textChannel != null) { // Wow, diferente de null!
							if (textChannel.canTalk()) { // Eu posso falar aqui? Se sim...
								// Vamos fazer polling dos posts então!
								var communityId = aminoConfig.communityId;

								if (communityId == null) {
									try {
										var document = Jsoup.connect(aminoConfig.inviteUrl).get(); // Mas antes vamos pegar o ID...

										var deepLink = document.getElementsByClass("deeplink-holder")[0];

										var narviiAppLink = deepLink.attr("data-link");

										communityId = narviiAppLink.split("/")[2];

										LorittaLauncher.loritta.ds.save(config);
									} catch (e: Exception) {
										// Se deu ruim, desative o módulo!
										aminoConfig.isEnabled = false;
										LorittaLauncher.loritta.ds.save(config); // Vamos salvar a config
										continue;
									}
								}

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
									var embed = EmbedBuilder();
									embed.setAuthor(post.author.nickname, null, post.author.icon)
									embed.setTitle(post.title)
									embed.setDescription(post.content)
									embed.setColor(Color.WHITE);

									if (post.mediaList != null) {
										var obj = post.mediaList;
										var inside = obj.get(0);

										if (inside is List<*>) {
											var link = inside.get(1) as String;

											if (link.contains("narvii.com") && (link.endsWith("jpg") || link.endsWith("png") || link.endsWith("gif"))) {
												embed.setImage(link);
											}
										}
									}
									embed.setFooter("Enviado as " + post.modifiedTime, null);
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
		} catch (e: Exception) {
			e.printStackTrace();
		}
	}
}