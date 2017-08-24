package com.mrpowergamerbr.loritta.frontend.views.configure;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext;
import com.mrpowergamerbr.loritta.userdata.RssFeedConfig;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.utils.FeedEntry;
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

public class RssFeedsConfigView {
	public static PebbleTemplate render(RenderContext context, TemmieDiscordAuth temmie, ServerConfig sc)
			throws PebbleException {
		context.contextVars().put("templateTags", "Talvez a sua feed RSS tenha mais opções! Para ver mais opções, salve e clique em \"Testar Feed RSS\"");
		context.contextVars().put("whereAmI", "rssFeedsConfig");

		if (context.arguments.length > 4) {
			String editor = context.arguments[4];

			if (editor.equalsIgnoreCase("editor")) {
				RssFeedConfig rssFeedConfig = sc.rssFeedConfig;
				RssFeedConfig.FeedInfo def = new RssFeedConfig.FeedInfo();

				if (context.arguments.length > 5) {
					String id = context.arguments[5];

					int intId = Integer.valueOf(id);

					def = sc.rssFeedConfig.getFeeds().get(intId);

					if (context.request.param("repostToChannelId").isSet()) {
						sc.rssFeedConfig.getFeeds().remove(def);
					}
				}

				if (context.request.param("repostToChannelId").isSet()) { // Salvando!
					def.setFeedUrl(context.request().param("feedUrl").value());
					def.setRepostToChannelId(context.request().param("repostToChannelId").value());
					def.setNewMessage(context.request().param("newMessage").value());
					rssFeedConfig.getFeeds().add(def);

					LorittaLauncher.getInstance().getDs().save(sc);
					try {
						context.response.redirect("https://loritta.website/config/servidor/" + sc.guildId + "/rss");
					} catch (Throwable e) {

					}
					PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("rss_editor.html");
					return template;
				} else {
					context.contextVars.put("feedInfo", def);
					try {
						FeedEntry firstResult = LorittaUtilsKotlin.getLastPostFromFeed(def.getFeedUrl());

						TextChannel channel = LorittaLauncher.loritta.getLorittaShards().getGuildById(sc.guildId).getTextChannelById(def.getRepostToChannelId());
						String message = def.getNewMessage();

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
					PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("rss_editor.html");
					return template;
				}
			}
		}

		if (context.request().param("deleteCommand").isSet()) { // O usuário está deletando uma feed?
			int feedId = context.request().param("deleteCommand").intValue();
			RssFeedConfig.FeedInfo feedInfo = sc.rssFeedConfig.getFeeds().isEmpty() ? new RssFeedConfig.FeedInfo() : sc.rssFeedConfig.getFeeds().get(0);
			RssFeedConfig rssFeedConfig = sc.rssFeedConfig;
			rssFeedConfig.getFeeds().remove(feedId);
			sc.rssFeedConfig(rssFeedConfig);
			LorittaLauncher.getInstance().getDs().save(sc);
		}

		context.contextVars().put("whereAmI", "rssFeedsConfig");
		context.contextVars().put("feedInfo", sc.rssFeedConfig.getFeeds().isEmpty() ? new RssFeedConfig.FeedInfo() : sc.rssFeedConfig.getFeeds().get(0));

		PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("rss_config.html");
		return template;
	}
}
