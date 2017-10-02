package com.mrpowergamerbr.loritta.frontendold.views;

import java.io.PrintWriter;
import java.io.StringWriter;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.frontendold.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontendold.utils.RenderContext;

public class HomeView {

	public static Object render(RenderContext context) {
		try {			
			PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("home.html");

			return template;
		} catch (PebbleException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return e.toString();
		}

	}
}