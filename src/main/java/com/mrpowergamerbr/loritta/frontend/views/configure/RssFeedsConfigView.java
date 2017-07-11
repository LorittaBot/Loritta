package com.mrpowergamerbr.loritta.frontend.views.configure;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext;
import com.mrpowergamerbr.loritta.userdata.RssFeedConfig;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;

public class RssFeedsConfigView {
	public static PebbleTemplate render(RenderContext context, TemmieDiscordAuth temmie, ServerConfig sc)
			throws PebbleException {
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
		context.contextVars().put("whereAmI", "rssFeedsConfig");
		context.contextVars().put("feedInfo", sc.rssFeedConfig.getFeeds().isEmpty() ? new RssFeedConfig.FeedInfo() : sc.rssFeedConfig.getFeeds().get(0));

		PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("rss_config.html");
		return template;
	}
}
