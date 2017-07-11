package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Entities
import org.jsoup.parser.Parser
import org.jsoup.safety.Whitelist
import java.text.SimpleDateFormat
import java.util.*


class NewRssFeedThread : Thread("RSS Feed Query Thread") {
	val lastItemTime = HashMap<String, RssFeedCheck>(); // HashMap usada para guardar a data do útimo item na RSS

	override fun run() {
		super.run()

		while (true) {
			checkRssFeeds();
			Thread.sleep(5000); // Só 5s de delay!
		}
	}

	fun checkRssFeeds() {
		try {
			var servers = LorittaLauncher.loritta.mongo
					.getDatabase("loritta")
					.getCollection("servers")
					.find(Filters.eq("rssFeedConfig.isEnabled", true))

			for (server in servers) {
				var config = LorittaLauncher.loritta.ds.get(ServerConfig::class.java, server.get("_id"));

				var rssFeedConfig = config.rssFeedConfig;

				if (rssFeedConfig.isEnabled) { // Está ativado?
					var guild = LorittaLauncher.loritta.lorittaShards.getGuildById(config.guildId)

					if (guild != null) {
						for (feedInfo in rssFeedConfig.feeds) {
							var textChannel = guild.getTextChannelById(feedInfo.repostToChannelId);

							val feedUrl = feedInfo.feedUrl;

							if (textChannel != null && feedUrl != null) { // Wow, diferente de null!
								if (textChannel.canTalk()) { // Eu posso falar aqui? Se sim...
									val rssFeed = HttpRequest.get(feedUrl)
											.header("Cache-Control", "max-age=0, no-cache") // Nunca pegar o cache
											.useCaches(false) // Também não usar cache
											.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0")
											.body();

									// Parsear a nossa RSS feed
									val jsoup = Jsoup.parse(rssFeed, "", Parser.xmlParser())

									var dateRss: String? = null;

									if (jsoup.select("feed entry published").isNotEmpty()) {
										dateRss = jsoup.select("feed entry published").first().text();
									} else if (jsoup.select("feed entry updated").isNotEmpty()) {
										dateRss = jsoup.select("feed entry updated").first().text();
									}

									if (dateRss == null) {
										continue;
									}

									val rssCalendar = javax.xml.bind.DatatypeConverter.parseDateTime(dateRss);

									var entryItem = jsoup.select("feed entry").first()
									val title = jsoup.select("feed entry title").first().text()
									val link = jsoup.select("feed entry link").first().attr("href")

									var description: String? = null;

									// Enquanto a maioria das feeds RSS colocam title e link... a maioria não coloca a descrição corretamente
									// Então vamos verificar de duas maneiras
									if (jsoup.select("feed entry description").isNotEmpty()) {
										description = jsoup.select("feed entry description").first().text()
									} else if (jsoup.select("feed entry content").isNotEmpty()) {
										description = jsoup.select("feed entry content").first().text()
									}

									if (description != null) {
										description = Jsoup.clean(description, "", Whitelist.simpleText(), Document.OutputSettings().escapeMode(Entities.EscapeMode.xhtml))
									}
									
									val checkedRssFeeds = lastItemTime.getOrDefault(guild.id, RssFeedCheck());

									if (checkedRssFeeds.checked[feedUrl] != null) {
										val lastDate = checkedRssFeeds.checked[feedUrl];

										// Data do último item na RSS Feed
										val lastCalendar = javax.xml.bind.DatatypeConverter.parseDateTime(lastDate);

										if (rssCalendar.before(lastCalendar) || rssCalendar.equals(lastCalendar)) {
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

										if (description != null) {
											message = message.replace("{descrição}", description);
										}
										message = message.replace("{título}", title);
										message = message.replace("{link}", link);

										// E só por diversão, vamos salvar todas as tags do entry!
										for (element in entryItem.allElements) {
											message = message.replace("{rss_${element.tagName()}", element.text());
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