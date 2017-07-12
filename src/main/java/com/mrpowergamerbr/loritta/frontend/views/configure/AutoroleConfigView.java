package com.mrpowergamerbr.loritta.frontend.views.configure;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext;
import com.mrpowergamerbr.loritta.userdata.AutoroleConfig;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class AutoroleConfigView {
	public static PebbleTemplate render(RenderContext context, TemmieDiscordAuth temmie, ServerConfig sc)
			throws PebbleException {
		if (context.request().param("autoroles").isSet()) { // O usuário está salvando as configurações?
			AutoroleConfig autoroleConfig = sc.autoroleConfig;
			autoroleConfig.setEnabled(context.request().param("enableModule").isSet());
			autoroleConfig.setRoles(Arrays.asList(context.request().param("autoroles").value().split(";")));
			sc.autoroleConfig(autoroleConfig);
			LorittaLauncher.getInstance().getDs().save(sc);
		}
		context.contextVars().put("whereAmI", "autoroleConfig");
		context.contextVars().put("currentAutoroles", StringUtils.join(sc.autoroleConfig().getRoles(), ";"));

		PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("autorole_config.html");
		return template;
	}
}
