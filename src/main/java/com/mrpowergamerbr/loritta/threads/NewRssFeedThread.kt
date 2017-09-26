package com.mrpowergamerbr.loritta.threads

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import java.text.SimpleDateFormat
import java.util.*


class NewRssFeedThread : Thread("RSS Feed Query Thread") {
	val lastItemTime = HashMap<String, RssFeedCheck>(); // HashMap usada para guardar a data do útimo item na RSS

	override fun run() {
		super.run()

		while (true) {
			checkRssFeeds();
			Thread.sleep(10000); // Só 10s de delay!
		}
	}

	fun checkRssFeeds() {
		try {
			var servers = LorittaLauncher.loritta.mongo
					.getDatabase("loritta")
					.getCollection("servers")
					.find(Filters.exists("rssFeedConfig.feeds", true))

			for (server in servers) {
				var config = LorittaLauncher.loritta.ds.get(ServerConfig::class.java, server["_id"]);

				var rssFeedConfig = config.rssFeedConfig;

				var guild = LorittaLauncher.loritta.lorittaShards.getGuildById(config.guildId)

				if (guild != null) {
					for (feedInfo in rssFeedConfig.feeds) {
						var textChannel = guild.getTextChannelById(feedInfo.repostToChannelId);

						val feedUrl = feedInfo.feedUrl;

						if (textChannel != null && feedUrl != null) { // Wow, diferente de null!
							if (textChannel.canTalk()) { // Eu posso falar aqui? Se sim...
								val feedEntry = LorittaUtilsKotlin.getLastPostFromFeed(feedUrl)

								if (feedEntry == null) { continue; }

								val checkedRssFeeds = lastItemTime.getOrDefault(guild.id, RssFeedCheck());

								if (checkedRssFeeds.checked[feedUrl] != null) {
									val lastDate = checkedRssFeeds.checked[feedUrl];

									// Data do último item na RSS Feed
									val lastCalendar = javax.xml.bind.DatatypeConverter.parseDateTime(lastDate);

									if (feedEntry.date.before(lastCalendar) || feedEntry.date.equals(lastCalendar)) {
										continue; // Na verdade o vídeo atual é mais velho! Ignore então! :)
									}

									// Ssalve a nova data
									val tz = TimeZone.getTimeZone("UTC")
									val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // Quoted "Z" to indicate UTC, no timezone offset
									df.timeZone = tz

									checkedRssFeeds.checked.put(feedUrl, df.format(Date()));
									lastItemTime.put(guild.id, checkedRssFeeds)

									// E envie para os canais necessários o nosso texto
									var message = feedInfo.newMessage

									if (message.isEmpty()) {
										message = "{link}"
										feedInfo.newMessage = message
										loritta save config
									}

									if (feedEntry.description != null) {
										message = message.replace("{descrição}", feedEntry.description);
									}

									message = message.replace("{título}", feedEntry.title);
									message = message.replace("{link}", feedEntry.link);

									// E só por diversão, vamos salvar todas as tags do entry!
									for (element in feedEntry.entry.select("*")) {
										message = message.replace("{rss_${element.tagName()}}", element.text());
									}

									textChannel.sendMessage(message).complete(); // Envie a mensagem
									continue;
								} else {
									// Se nunca verificamos esta feed, vamos só salvar a data atual
									val tz = TimeZone.getTimeZone("UTC")
									val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // Quoted "Z" to indicate UTC, no timezone offset
									df.timeZone = tz
									checkedRssFeeds.checked.put(feedUrl, df.format(Date()));
									lastItemTime.put(guild.id, checkedRssFeeds)
									continue;
								}
							}
						}
					}
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	data class RssFeedCheck(
			var checked: HashMap<String, String>
	) {
		constructor() : this(HashMap<String, String>())
	}
}