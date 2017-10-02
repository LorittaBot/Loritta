package com.mrpowergamerbr.loritta.frontendold.views;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.frontendold.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontendold.utils.RenderContext;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;

import java.io.PrintWriter;
import java.io.StringWriter;

public class BannedView {

	public static Object render(RenderContext context, TemmieDiscordAuth temmie) {
		try {
			PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("banned.html");

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