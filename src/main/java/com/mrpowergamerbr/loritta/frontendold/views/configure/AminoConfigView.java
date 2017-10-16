package com.mrpowergamerbr.loritta.frontendold.views.configure;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.frontendold.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontendold.utils.RenderContext;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;

public class AminoConfigView {
	public static PebbleTemplate render(RenderContext context, TemmieDiscordAuth temmie, ServerConfig sc)
			throws PebbleException {
		if (context.request().param("repostToChannelId").isSet()) { // O usuário está salvando as configurações?
			/* AminoConfig aminoConfig = sc.aminoConfig;
			aminoConfig.setEnabled(context.request().param("enableModule").isSet());
			aminoConfig.setInviteUrl(context.request().param("inviteUrl").value());
			aminoConfig.setRepostToChannelId(context.request().param("repostToChannelId").value());
			aminoConfig.setFixAminoImages(context.request().param("fixAminoImages").isSet());

			try {
				Document document = Jsoup.connect(aminoConfig.getInviteUrl()).get(); // Mas antes vamos pegar o ID...

				Element deepLink = document.getElementsByClass("deeplink-holder").get(0);

				String narviiAppLink = deepLink.attr("data-link");

				String communityId = narviiAppLink.split("/")[2];

				aminoConfig.setCommunityId(communityId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			sc.aminoConfig(aminoConfig);
			LorittaLauncher.getInstance().getDs().save(sc); */
		}
		context.contextVars().put("whereAmI", "aminoConfig");

		PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("amino_config.html");
		return template;
	}
}
