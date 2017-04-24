package com.mrpowergamerbr.loritta.frontend.views;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.jooby.Request;
import org.jooby.Response;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderWrapper;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;
import com.mrpowergamerbr.temmiediscordauth.utils.TemmieGuild;

public class ChooseServerView {

	public static Object render(Request req, Response res, TemmieDiscordAuth temmie) {
		try {
			HashMap<String, Object> context = new HashMap<String, Object>();

			List<TemmieGuild> guilds = temmie.getUserGuilds();
			context.put("guilds", guilds.stream().filter((guild) -> LorittaWebsite.canManageGuild(guild)).collect(Collectors.toList()));
			PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("choose_server.html");

			return new RenderWrapper(template, context);
		} catch (PebbleException e) {
			// TODO Auto-generated catch block
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return e.toString();
		}

	}
}