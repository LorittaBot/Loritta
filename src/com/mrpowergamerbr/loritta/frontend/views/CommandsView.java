package com.mrpowergamerbr.loritta.frontend.views;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite;
import com.mrpowergamerbr.loritta.frontend.utils.RenderContext;

public class CommandsView {

	public static Object render(RenderContext context) {
		try {
			context.contextVars().put("availableCmds", LorittaLauncher.getInstance().getCommandManager().getCommandMap());
			context.contextVars().put("categories", CommandCategory.values());
			
			Map<String, List<CommandBase>> commandsByCategories = new HashMap<String, List<CommandBase>>();
			
			for (CommandCategory category : CommandCategory.values()) {
				List<CommandBase> commands = new ArrayList<CommandBase>();
				for (CommandBase command : LorittaLauncher.getInstance().getCommandManager().getCommandMap()) {
					if (command.getCategory() == category) {
						commands.add(command);
					}
				}
				commandsByCategories.put(category.name(), commands);
			}
			context.contextVars().put("commandsByCategories", commandsByCategories);
			
			PebbleTemplate template = LorittaWebsite.getEngine().getTemplate("commands.html");
			return template;
		} catch (PebbleException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return e.toString();
		}
	}
}