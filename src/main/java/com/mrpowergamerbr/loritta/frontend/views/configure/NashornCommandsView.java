package com.mrpowergamerbr.loritta.frontend.views.configure;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;
import com.mrpowergamerbr.temmiediscordauth.TemmieDiscordAuth;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public class NashornCommandsView {
	public static PebbleTemplate render(RenderContext context, TemmieDiscordAuth temmie, ServerConfig sc)
			throws PebbleException {
		context.contextVars().put("whereAmI", "nashornCommands");

		if (context.arguments.length > 4) {
			String editor = context.arguments[4];

			if (editor.equalsIgnoreCase("editor")) {
				NashornCommand def = new NashornCommand();
				def.javaScript = "responder(\"Loritta \uD83D\uDE18\");";
				context.contextVars().put("editingCommand", def);

				if (context.request.param("commandResponse").isSet()) {
					// Salvando!
					NashornCommand toSave = new NashornCommand();
					ArrayList<NashornCommand> toRemove = new ArrayList<>();

					for (NashornCommand nash : sc.nashornCommands) {
						if (nash.id.toString().equals(context.request.param("commandId").value())) {
							toSave = nash;
							toRemove.add(nash);
						}
					}

					sc.nashornCommands.removeAll(toRemove);
					toSave.javaScript = context.request.param("commandResponse").value();
					toSave.label = context.request.param("commandName").value();
					sc.nashornCommands.add(toSave);

					LorittaLauncher.getInstance().getDs().save(sc); // E agora salve! Yay, problema resolvido!

					try {
						context.response.redirect("https://loritta.website/config/servidor/" + sc.guildId + "/nashorn");
					} catch (Throwable e) {

					}
				} else {
					if (context.arguments.length > 5) {
						String id = context.arguments[5];

						for (NashornCommand nash : sc.nashornCommands) {
							if (nash.id.toString().equals(id)) {
								context.contextVars().put("editingCommand", nash);
							}
						}
					}

					PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("command_editor.html");

					return template;
				}
			}
		}
		if (context.request().param("deleteCommand").isSet()) {
			ArrayList<NashornCommand> toRemove = new ArrayList<NashornCommand>();

			for (NashornCommand customCommand : sc.nashornCommands()) {
				if (customCommand.getId().equals(new ObjectId(context.request().param("deleteCommand").value()))) {
					toRemove.add(customCommand);
				}
			}

			sc.nashornCommands().removeAll(toRemove);

			LorittaLauncher.getInstance().getDs().save(sc); // E agora salve! Yay, problema resolvido!
		}

		PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("nashorn.html");
		return template;
	}
}
