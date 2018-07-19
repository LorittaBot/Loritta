package com.mrpowergamerbr.loritta.threads

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*


class NewRssFeedThread : Thread("RSS Feed Query Thread") {
	companion object {
		val lastItemTime = HashMap<String, RssFeedCheck>(); // HashMap usada para guardar a data do útimo item na RSS
		val logger = LoggerFactory.getLogger(NewRssFeedThread::class.java)
	}

	override fun run() {
		super.run()

		while (true) {
			try {
				checkRssFeeds();
			} catch (e: Exception) {
				logger.error("Erro ao verificar novas RSS feeds!", e)
			}
			Thread.sleep(10000); // Só 10s de delay!
		}
	}

	fun checkRssFeeds() {
		val servers = loritta.serversColl.find(
				Filters.gt("rssFeedConfig.feeds", listOf<Any>())
		)

		logger.info("Verificando RSS feeds de ${servers.count()} servidores...")

		try {
			servers.iterator().use {
				while (it.hasNext()) {
					val config = it.next()

					try {
						var rssFeedConfig = config.rssFeedConfig;

						var guild = LorittaLauncher.loritta.lorittaShards.getGuildById(config.guildId)

						if (guild != null) {
							for (feedInfo in rssFeedConfig.feeds) {
								try {
									var textChannel = guild.getTextChannelById(feedInfo.repostToChannelId);

									val feedUrl = feedInfo.feedUrl;

									if (textChannel != null && feedUrl != null) { // Wow, diferente de null!
										if (textChannel.canTalk()) { // Eu posso falar aqui? Se sim...
											val feedEntry = LorittaUtilsKotlin.getLastPostFromFeed(feedUrl) ?: continue

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

												val customTokens = mutableMapOf<String, String>()
												if (feedEntry.description != null) {
													customTokens["descrição"] = feedEntry.description
													customTokens["description"] = feedEntry.description
												}

												customTokens["título"] = feedEntry.title
												customTokens["title"] = feedEntry.title
												customTokens["link"] = feedEntry.link

												// E só por diversão, vamos salvar todas as tags do entry!
												for (element in feedEntry.entry.select("*")) {
													customTokens["rss_${element.tagName()}"] = element.text()
												}

												val generatedMessage = MessageUtils.generateMessage(message, null, guild, customTokens) ?: continue

												textChannel.sendMessage(generatedMessage).complete() // Envie a mensagem
											} else {
												// Se nunca verificamos esta feed, vamos só salvar a data atual
												val tz = TimeZone.getTimeZone("UTC")
												val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // Quoted "Z" to indicate UTC, no timezone offset
												df.timeZone = tz
												checkedRssFeeds.checked.put(feedUrl, df.format(Date()));
												lastItemTime.put(guild.id, checkedRssFeeds)
											}
										}
									}
								} catch (e: Exception) {
									logger.error("Erro ao atualizar RSS feed!", e)
								}
							}
						}
					} catch (e: Exception) {
						logger.error("Erro ao processar RSS feeds!", e)
					}
				}
			}
		} catch (e: Exception) {
			logger.error("Erro ao pegar RSS feeds!", e)
		}
	}

	data class RssFeedCheck(
			var checked: HashMap<String, String>
	) {
		constructor() : this(HashMap<String, String>())
	}
}