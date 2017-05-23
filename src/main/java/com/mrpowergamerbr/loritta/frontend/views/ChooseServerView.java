package com.mrpowergamerbr.loritta.frontend.views;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;
import com.mrpowergamerbr.temmiediscordauth.utils.TemmieGuild;

public class ChooseServerView {

	public static Object render(RenderContext context, TemmieDiscordAuth temmie) {
		try {
			List<TemmieGuild> guilds = temmie.getUserGuilds();
			context.contextVars().put("guilds", guilds.stream().filter((guild) -> LorittaWebsite.canManageGuild(guild)).collect(Collectors.toList()));
			PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("choose_server.html");

			return template;
		} catch (PebbleException e) {
			// TODO Auto-generated catch block
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return e.toString();
		}

	}
}