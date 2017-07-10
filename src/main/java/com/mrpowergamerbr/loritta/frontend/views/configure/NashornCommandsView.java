package com.mrpowergamerbr.loritta.frontend.views.configure;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;

import java.util.ArrayList;

public class NashornCommandsView {
	public static PebbleTemplate render(RenderContext context, TemmieDiscordAuth temmie, ServerConfig sc)
			throws PebbleException {
		if (context.request().param("deleteCommand").isSet()) {
			ArrayList<NashornCommand> toRemove = new ArrayList<NashornCommand>();

			for (NashornCommand customCommand : sc.nashornCommands()) {
				if (customCommand.hashCode() == Integer.parseInt(context.request().param("deleteCommand").value())) {
					toRemove.add(customCommand);
				}
			}

			sc.nashornCommands().removeAll(toRemove);

			LorittaLauncher.getInstance().getDs().save(sc); // E agora salve! Yay, problema resolvido!
		}
		if (context.request().param("commandName").isSet()) {
			// Hora de criar um novo comando!
			NashornCommand customCommand = new NashornCommand(context.request().param("commandName").value(), context.request().param("commandResponse").value());

			sc.nashornCommands().add(customCommand); // E agora adicione o nosso novo comando customizado no ServerConfig...

			LorittaLauncher.getInstance().getDs().save(sc); // E agora salve! Yay, problema resolvido!
		}

		context.contextVars().put("whereAmI", "nashornCommands");

		PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("nashorn.html");
		return template;
	}
}
