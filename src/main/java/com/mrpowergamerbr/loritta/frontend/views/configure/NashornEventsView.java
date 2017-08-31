package com.mrpowergamerbr.loritta.frontend.views.configure;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext;
import com.mrpowergamerbr.loritta.listeners.nashorn.NashornEventHandler;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public class NashornEventsView {
	public static PebbleTemplate render(RenderContext context, TemmieDiscordAuth temmie, ServerConfig sc)
			throws PebbleException {
		context.contextVars().put("whereAmI", "nashornCommands");

		if (context.arguments.length > 4) {
			String editor = context.arguments[4];

			if (editor.equalsIgnoreCase("editor")) {
				NashornEventHandler def = new NashornEventHandler();
				def.javaScript = "function onMessageReceived(event) {\n    reply(\"Loritta \uD83D\uDE18\");\n}";
				context.contextVars().put("editingCommand", def);

				if (context.request.param("commandResponse").isSet()) {
					// Salvando!
					NashornEventHandler toSave = new NashornEventHandler();
					ArrayList<NashornEventHandler> toRemove = new ArrayList<>();

					for (NashornEventHandler nash : sc.nashornEventHandlers) {
						if (nash.getId().toString().equals(context.request.param("commandId").value())) {
							toSave = nash;
							toRemove.add(nash);
						}
					}

					sc.nashornEventHandlers.removeAll(toRemove);
					toSave.javaScript = context.request.param("commandResponse").value();
					// toSave.setJsLabel(context.request.param("commandName").value());
					sc.nashornEventHandlers.add(toSave);

					LorittaLauncher.getInstance().getDs().save(sc); // E agora salve! Yay, problema resolvido!

					try {
						context.response.redirect("https://loritta.website/config/servidor/" + sc.guildId + "/events");
					} catch (Throwable e) {

					}
				} else {
					if (context.arguments.length > 5) {
						String id = context.arguments[5];

						for (NashornEventHandler nash : sc.nashornEventHandlers) {
							if (nash.getId().toString().equals(id)) {
								context.contextVars().put("editingCommand", nash);
							}
						}
					}

					PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("event_editor.html");

					return template;
				}
			}
		}
		if (context.request().param("deleteCommand").isSet()) {
			ArrayList<NashornEventHandler> toRemove = new ArrayList<NashornEventHandler>();

			for (NashornEventHandler customCommand : sc.nashornEventHandlers) {
				if (customCommand.getId().equals(new ObjectId(context.request().param("deleteCommand").value()))) {
					toRemove.add(customCommand);
				}
			}

			sc.nashornEventHandlers.removeAll(toRemove);

			LorittaLauncher.getInstance().getDs().save(sc); // E agora salve! Yay, problema resolvido!
		}

		PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("events.html");
		return template;
	}
}
