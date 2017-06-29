package com.mrpowergamerbr.loritta.frontend.views.configure;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.userdata.YouTubeConfig;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class YouTubeConfigView {
	public static PebbleTemplate render(RenderContext context, TemmieDiscordAuth temmie, ServerConfig sc)
			throws PebbleException {
		if (context.request().param("repostToChannelId").isSet()) { // O usuário está salvando as configurações?
			YouTubeConfig youTubeConfig = sc.youTubeConfig;
			youTubeConfig.setEnabled(context.request().param("enableModule").isSet());
			youTubeConfig.setChannelUrl(context.request().param("channelUrl").value());
			youTubeConfig.setRepostToChannelId(context.request().param("repostToChannelId").value());
			youTubeConfig.setVideoSentMessage(context.request().param("videoSentMessage").value());

			try {
				Document jsoup = Jsoup.connect(youTubeConfig.getChannelUrl()).get(); // Hora de pegar a página do canal...

				String id = jsoup.getElementsByAttribute("data-channel-external-id").get(0).attr("data-channel-external-id"); // Que possuem o atributo "data-channel-external-id" (que é o ID do canal)

				youTubeConfig.setChannelId(id); // E salvar o ID!
			} catch (Exception e) {
				e.printStackTrace();
			}
			sc.youTubeConfig(youTubeConfig);
			LorittaLauncher.getInstance().getDs().save(sc);
		}
		context.contextVars().put("whereAmI", "youTubeConfig");

		PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("youtube_config.html");
		return template;
	}
}
