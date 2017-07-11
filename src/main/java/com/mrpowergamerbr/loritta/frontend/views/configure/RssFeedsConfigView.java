package com.mrpowergamerbr.loritta.frontend.views.configure;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext;
import com.mrpowergamerbr.loritta.userdata.RssFeedConfig;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.utils.FeedEntry;
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jsoup.nodes.Element;

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
			FeedEntry firstResult = LorittaUtilsKotlin.getLastPostFromFeed(feedInfo.getFeedUrl());

			TextChannel channel = LorittaLauncher.loritta.getLorittaShards().getGuildById(sc.guildId).getTextChannelById(feedInfo.getRepostToChannelId());
			String message = feedInfo.getNewMessage();

			if (firstResult.getDescription() != null) {
				message = message.replace("{descrição}", firstResult.getDescription());
			}
			message = message.replace("{título}", firstResult.getTitle());
			message = message.replace("{link}", firstResult.getLink());

			String templateTags = "";
			// E só por diversão, vamos salvar todas as tags do entry!
			for (Element element : firstResult.getEntry().select("*")) {
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
