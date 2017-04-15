package com.mrpowergamerbr.loritta.frontend.views;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.jooby.Request;
import org.jooby.Response;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandOptions;
import com.mrpowergamerbr.loritta.commands.vanilla.fun.TristeRealidadeCommand;
import com.mrpowergamerbr.loritta.commands.vanilla.fun.TristeRealidadeCommand.TristeRealidadeCommandOptions;
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
					context.put("currentServer", guild);
					break;
				}
			}

			if (allowed) {
				PebbleTemplate template = null;
				ServerConfig sc = LorittaLauncher.getInstance().getServerConfigForGuild(guildId);
				context.put("serverConfig", sc);
				if (req.path().endsWith("commands")) {
					if (req.param("editingCmds").isSet()) {
						ArrayList<String> enabledModules = new ArrayList<String>();
						for (CommandBase cmdBase : LorittaLauncher.getInstance().getCommandManager().getCommandMap()) {
							if (req.param(cmdBase.getClass().getSimpleName()).isSet()) {
								enabledModules.add(cmdBase.getClass().getSimpleName());
							}
						}
						sc.modules(enabledModules);
					}
					if (req.param("editingTristeRealidade").isSet()) {
						TristeRealidadeCommandOptions cmdOpti = new TristeRealidadeCommand.TristeRealidadeCommandOptions();
						cmdOpti.mentionEveryone(req.param("mentionEveryone").isSet());
						cmdOpti.hideDiscordTags(req.param("hideDiscordTags").isSet());
						sc.commandOptions().put("TristeRealidadeCommand", cmdOpti);
					}
					if (req.param("activateAllCommands").isSet()) {
						ArrayList<String> enabledModules = new ArrayList<String>();
						for (CommandBase cmdBase : LorittaLauncher.getInstance().getCommandManager().getCommandMap()) {
							enabledModules.add(cmdBase.getClass().getSimpleName());
						}
						sc.modules(enabledModules);
					}
					LorittaLauncher.getInstance().getDs().save(sc);
					for (CommandBase cmdBase : LorittaLauncher.getInstance().getCommandManager().getCommandMap()) {
						context.put("commandOption" + cmdBase.getClass().getSimpleName(), new CommandOptions());
					}
					context.put("commandOptionTristeRealidadeCommand", new TristeRealidadeCommand.TristeRealidadeCommandOptions());
					for (Entry<String, CommandOptions> entry : sc.commandOptions().entrySet()) {
						context.put("commandOption" + entry.getKey(), entry.getValue());
					}
					
					template = LorittaWebsite.engine.getTemplate("module_config.html");
					context.put("availableCmds", LorittaLauncher.getInstance().getCommandManager().getCommandMap());
					context.put("activeCmds", LorittaLauncher.getInstance().getCommandManager().getCommandsAvailableFor(sc));
				} else {
					if (req.param("commandPrefix").isSet()) {
						sc.commandPrefix(req.param("commandPrefix").value());
						LorittaLauncher.getInstance().getDs().save(sc);
					}

					if (req.param("commandMagic").isSet()) {
						sc.explainOnCommandRun(req.param("explainOnCommandRun").isSet());
						sc.mentionOnCommandOutput(req.param("mentionOnCommandOutput").isSet());
						sc.debugOptions().enableAllModules(req.param("enableAllModules").isSet());
						LorittaLauncher.getInstance().getDs().save(sc);
					}

					context.put("commandPrefix", sc.commandPrefix());
					template = LorittaWebsite.engine.getTemplate("server_config.html");
				}

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