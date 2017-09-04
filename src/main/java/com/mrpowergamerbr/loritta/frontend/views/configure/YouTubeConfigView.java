package com.mrpowergamerbr.loritta.frontend.views.configure;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext;
import com.mrpowergamerbr.loritta.userdata.RssFeedConfig;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.userdata.YouTubeConfig;
import com.mrpowergamerbr.loritta.utils.FeedEntry;
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTubeConfigView {
	public static PebbleTemplate render(RenderContext context, TemmieDiscordAuth temmie, ServerConfig sc)
			throws PebbleException {
		context.contextVars().put("whereAmI", "youTubeConfig");

		if (context.arguments.length > 4) {
			String editor = context.arguments[4];

			if (editor.equalsIgnoreCase("editor")) {
				YouTubeConfig youTubeConfig = sc.youTubeConfig;
				YouTubeConfig.YouTubeInfo def = new YouTubeConfig.YouTubeInfo("", "", "", "");

				if (context.arguments.length > 5) {
					String id = context.arguments[5];

					int intId = Integer.valueOf(id);

					def = sc.youTubeConfig.getChannels().get(intId);

					if (context.request.param("repostToChannelId").isSet()) {
						sc.youTubeConfig.getChannels().remove(def);
					}
				}

				if (context.request.param("repostToChannelId").isSet()) { // Salvando!
					def.setChannelUrl(context.request().param("channelUrl").value());
					def.setRepostToChannelId(context.request().param("repostToChannelId").value());
					def.setVideoSentMessage(context.request().param("videoSentMessage").value());

					if (!def.getChannelUrl().startsWith("http")) {
						def.setChannelUrl("http://" + def.getChannelUrl());
					}

					try {
						Document jsoup = Jsoup.connect(def.getChannelUrl()).get(); // Hora de pegar a página do canal...

						Pattern pattern = Pattern.compile("\"browseId\":\"([A-z0-9_-]+)\"");

						Matcher matcher = pattern.matcher(jsoup.html());

						if (matcher.find()) {
							String id = matcher.group(1);

							def.setChannelId(id); // E salvar o ID!
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					youTubeConfig.getChannels().add(def);
					sc.youTubeConfig = youTubeConfig;
					LorittaLauncher.getInstance().getDs().save(sc);

					try {
						context.response.redirect("https://loritta.website/config/servidor/" + sc.guildId + "/youtube");
					} catch (Throwable e) {

					}
					PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("empty.html");
					return template;
				} else {
					context.contextVars.put("config", def);

					PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("youtube_editor.html");
					return template;
				}
			}
		}

		if (context.request().param("deleteCommand").isSet()) { // O usuário está deletando uma feed?
			int feedId = context.request().param("deleteCommand").intValue();
			YouTubeConfig youTubeConfig = sc.youTubeConfig;
			youTubeConfig.getChannels().remove(feedId);
			sc.youTubeConfig = youTubeConfig;
			LorittaLauncher.getInstance().getDs().save(sc);
		}

		context.contextVars().put("whereAmI", "youTubeConfig");

		PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("youtube_config.html");
		return template;
	}
}
