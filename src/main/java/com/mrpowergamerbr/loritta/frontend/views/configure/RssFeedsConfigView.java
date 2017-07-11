package com.mrpowergamerbr.loritta.frontend.views.configure;

import com.github.kevinsawicki.http.HttpRequest;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext;
import com.mrpowergamerbr.loritta.userdata.RssFeedConfig;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;

public class RssFeedsConfigView {
	public static PebbleTemplate render(RenderContext context, TemmieDiscordAuth temmie, ServerConfig sc)
			throws PebbleException {
		context.contextVars().put("templateTags", "Talvez a sua feed RSS tenha mais opções! Para ver mais opções, salve e clique em \"Testar Feed RSS\"");

		if (context.request().param("repostToChannelId").isSet()) { // O usuário está salvando as configurações?
			RssFeedConfig.FeedInfo feedInfo = sc.rssFeedConfig.getFeeds().isEmpty() ? new RssFeedConfig.FeedInfo() : sc.rssFeedConfig.getFeeds().get(0);
			RssFeedConfig rssFeedConfig = sc.rssFeedConfig;
			rssFeedConfig.setEnabled(context.request().param("enableModule").isSet());
			feedInfo.setFeedUrl(context.request().param("feedUrl").value());
			feedInfo.setRepostToChannelId(context.request().param("repostToChannelId").value());
			feedInfo.setNewMessage(context.request().param("newMessage").value());
			rssFeedConfig.getFeeds().clear();
			rssFeedConfig.getFeeds().add(feedInfo);
			sc.rssFeedConfig(rssFeedConfig);
			LorittaLauncher.getInstance().getDs().save(sc);
		}

		try {
			RssFeedConfig.FeedInfo feedInfo = sc.rssFeedConfig.getFeeds().isEmpty() ?
					new RssFeedConfig.FeedInfo() :
					sc.rssFeedConfig.getFeeds().get(0);
			String rssFeed = HttpRequest.get(feedInfo.getFeedUrl()).header("Cache-Control", "max-age=0, no-cache") // Nunca pegar o cache
					.useCaches(false) // Também não usar cache
					.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0").body();

			// Parsear a nossa RSS feed
			Document jsoup = Jsoup.parse(rssFeed, "", Parser.xmlParser());

			String title = null;
			String link = null;
			Element entryItem = null;
			if (jsoup.select("feed").attr("xmlns").equals("http://www.w3.org/2005/Atom")) {
				// Atom Feed
				title = jsoup.select("feed entry title").first().text();
				link = jsoup.select("feed entry link").first().attr("href");
				entryItem = jsoup.select("feed entry").first();
			} else {
				// Provavelemente é uma feed RSS então :)
				title = jsoup.select("channel item title").first().text();
				link = jsoup.select("channel item link").first().text();
				entryItem = jsoup.select("channel item").first();
			}

			String description = null;

			// Enquanto a maioria das feeds RSS colocam title e link... a maioria não coloca a descrição corretamente
			// Então vamos verificar de duas maneiras


			if (description != null) {
				description = Jsoup.clean(description, "", Whitelist.simpleText(), new Document.OutputSettings().escapeMode(Entities.EscapeMode.xhtml));
			}

			TextChannel channel = LorittaLauncher.loritta.getLorittaShards().getGuildById(sc.guildId).getTextChannelById(feedInfo.getRepostToChannelId());
			String message = feedInfo.getNewMessage();

			if (description != null) {
				message = message.replace("{descrição}", description);
			}
			message = message.replace("{título}", title);
			message = message.replace("{link}", link);

			String templateTags = "";
			// E só por diversão, vamos salvar todas as tags do entry!
			for (Element element : entryItem.select("*")) {
				message = message.replace("{rss_" + element.tagName() + "}", element.ownText());
				templateTags += "{rss_" + element.tagName() + "} - " + element.ownText() + "\n";
			}

			context.contextVars().put("templateTags", templateTags);

			if (context.request().param("testAction").isSet()) { // Testar
				channel.sendMessage(message).complete(); // Envie a mensagem
			}
		} catch (Exception e) {}

		context.contextVars().put("whereAmI", "rssFeedsConfig");
		context.contextVars().put("feedInfo", sc.rssFeedConfig.getFeeds().isEmpty() ? new RssFeedConfig.FeedInfo() : sc.rssFeedConfig.getFeeds().get(0));

		PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("rss_config.html");
		return template;
	}
}
