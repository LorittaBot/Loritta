package com.mrpowergamerbr.loritta.frontendold.views.configure;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.frontendold.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontendold.utils.RenderContext;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.loritta.userdata.StarboardConfig;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;

public class StarboardConfigView {
	public static PebbleTemplate render(RenderContext context, TemmieDiscordAuth temmie, ServerConfig sc)
			throws PebbleException {
		if (context.request().param("starboardId").isSet()) { // O usuário está salvando as configurações?
			StarboardConfig starboardConfig = sc.starboardConfig;
			starboardConfig.setEnabled(context.request().param("enableModule").isSet());
			starboardConfig.setStarboardId(context.request().param("starboardId").value());
			sc.starboardConfig(starboardConfig);
			LorittaLauncher.getInstance().getDs().save(sc);
		}
		context.contextVars().put("whereAmI", "starboardConfig");

		PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("starboard_config.html");
		return template;
	}
}
