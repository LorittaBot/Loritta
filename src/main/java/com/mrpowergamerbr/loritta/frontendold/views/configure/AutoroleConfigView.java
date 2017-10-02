package com.mrpowergamerbr.loritta.frontendold.views.configure;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.frontendold.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontendold.utils.RenderContext;
import com.mrpowergamerbr.loritta.userdata.AutoroleConfig;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoroleConfigView {
	public static PebbleTemplate render(RenderContext context, TemmieDiscordAuth temmie, ServerConfig sc)
			throws PebbleException {
		// Filtrar roles inválidas
		AutoroleConfig autoroleConfig = sc.autoroleConfig;
		boolean dirty = false;
		if (context.request().param("autoroles").isSet()) { // O usuário está salvando as configurações?
			autoroleConfig.setEnabled(context.request().param("enableModule").isSet());
			autoroleConfig.setRoles(new ArrayList<String>(Arrays.asList(context.request().param("autoroles").value().split(";"))));
			sc.autoroleConfig(autoroleConfig);
			dirty = true;
		}
		List<String> toRemove = new ArrayList<String>();
		Guild guild = (Guild) context.contextVars().get("currentServerJda");
		for (String roleId : autoroleConfig.getRoles()) {
			try {
				Role role = guild.getRoleById(roleId);
				if (role == null) {
					toRemove.add(roleId);
				}
			} catch (Exception e) {
				e.printStackTrace();
				toRemove.add(roleId);
			}
		}
		if (!toRemove.isEmpty()) {
			autoroleConfig.getRoles().removeAll(toRemove);
			sc.autoroleConfig(autoroleConfig);
			dirty = true;
		}
		if (dirty) {
			LorittaLauncher.getInstance().getDs().save(sc);
		}
		context.contextVars().put("whereAmI", "autoroleConfig");
		context.contextVars().put("currentAutoroles", StringUtils.join(sc.autoroleConfig().getRoles(), ";"));

		PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("autorole_config.html");
		return template;
	}
}
