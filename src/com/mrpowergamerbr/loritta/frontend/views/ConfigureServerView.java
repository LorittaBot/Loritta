package com.mrpowergamerbr.loritta.frontend.views;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;

import org.jooby.Request;
import org.jooby.Response;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderWrapper;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;
import com.mrpowergamerbr.temmiediscordauth.utils.TemmieGuild;

public class ConfigureServerView {

	public static Object render(Request req, Response res, TemmieDiscordAuth temmie, String guildId) {
		try {
			HashMap<String, Object> context = new HashMap<String, Object>();

			List<TemmieGuild> guilds = temmie.getUserGuilds();
			boolean allowed = false;
			for (TemmieGuild guild : guilds) {
				if (guild.getId().equals(guildId)) {
					allowed = guild.isOwner();
					break;
				}
			}
			
			if (allowed) {
				ServerConfig sc = LorittaLauncher.getInstance().getServerConfigForGuild(guildId);
				
				if (req.param("commandPrefix").isSet()) {
					sc.commandPrefix(req.param("commandPrefix").value());
					LorittaLauncher.getInstance().getDs().save(sc);
				}
				
				if (req.param("commandMagic").isSet()) {
					sc.explainOnCommandRun(req.param("explainOnCommandRun").isSet());
					sc.mentionOnCommandOutput(req.param("mentionOnCommandOutput").isSet());
					LorittaLauncher.getInstance().getDs().save(sc);
				}
				
				context.put("commandPrefix", sc.commandPrefix());
				context.put("serverConfig", sc);
				
				PebbleTemplate template = LorittaWebsite.engine.getTemplate("server_config.html");

				return new RenderWrapper(template, context);
			} else {
				try {
					res.redirect(LorittaWebsite.websiteUrl);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (PebbleException e) {
			// TODO Auto-generated catch block
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return e.toString();
		}
		return null;
	}
}