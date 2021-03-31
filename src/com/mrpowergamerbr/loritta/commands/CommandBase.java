package com.mrpowergamerbr.loritta.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.userdata.ServerConfig;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public abstract class CommandBase {
	public abstract String getLabel();

	public String getDescription() {
		return "Insira descrição do comando aqui";
	}

	public CommandCategory getCategory() {
		return CommandCategory.MISC;
	}

	public String getUsage() {
		return null;
	}

	public Map<String, String> getDetailedUsage() {
		return new HashMap<String, String>();
	}

	public List<String> getExample() {
		return Arrays.asList();
	}

	public boolean hasCommandFeedback() {
		return true;
	}
	
	public abstract void run(CommandContext context);

	public String getExtendedDescription() {
		return getDescription();
	}
	
	public boolean handle(MessageReceivedEvent ev, ServerConfig conf) {
		String message = ev.getMessage().getContentDisplay();
		if (message.startsWith(conf.commandPrefix() + getLabel())) {
			if (hasCommandFeedback()) {
				if (!ev.getTextChannel().canTalk()) { // Se a Loritta não pode falar no canal de texto, avise para o dono do servidor para dar a permissão para ela
					Loritta.warnOwnerNoPermission(ev.getGuild(), ev.getTextChannel(), conf);
					return true;
				} else {
					ev.getChannel().sendTyping().complete();
				}
			}
			String cmd = conf.commandPrefix() + getLabel();
			String onlyArgs = message.substring(message.indexOf(cmd) + cmd.length()); // wow, such workaround, very bad
			String[] args = Arrays.asList(onlyArgs.split(" ")).stream().filter((str) -> !str.isEmpty()).collect(Collectors.toList()).toArray(new String[0]);
			if (args.length >= 1 && args[0].equals("🤷")) { // Usar a ajuda caso 🤷 seja usado
				explain(conf, ev);
				return true;
			}
			CommandContext context = new CommandContext(conf, ev, this, args);
			run(context);
			return true;
		}
		return false;
	}

	public void explain(ServerConfig conf, MessageReceivedEvent ev) {
		if (conf.explainOnCommandRun()) {
			EmbedBuilder embed = new EmbedBuilder();

			embed.setAuthor("Comando: " + conf.commandPrefix() + this.getLabel(), null, "http://emojipedia-us.s3.amazonaws.com/cache/69/c0/69c0b62308243947c2ac885e1cd3d853.png");

			String usage = getUsage() != null ? " `" + getUsage() + "`" : "";

			String cmdInfo = "**Descrição:** " + getDescription() + "\n\n";

			cmdInfo += "**Como Usar:** " + conf.commandPrefix() + this.getLabel() + usage + "\n";

			if (!this.getDetailedUsage().isEmpty()) {
				for (Entry<String, String> entry : this.getDetailedUsage().entrySet()) {
					cmdInfo += "`" + entry.getKey() + "` - " + entry.getValue() + "\n";
				}
			}

			cmdInfo += "\n";

			if (this.getExample().isEmpty()) {
				cmdInfo += "**Exemplo:**\n" + conf.commandPrefix() + this.getLabel();
			} else {
				cmdInfo += "**Exemplo" + (this.getExample().size() == 1 ? "" : "s") + ":**\n";
				for (String example : this.getExample()) {
					cmdInfo += conf.commandPrefix() + this.getLabel() + (example.isEmpty() ? "" : " `" + example + "`") + "\n";
				}
			}
			embed.setDescription(cmdInfo);

			if (conf.explainInPrivate()) {
				ev.getAuthor().openPrivateChannel().complete().sendMessage(embed.build()).complete();
			} else {
				ev.getChannel().sendMessage(embed.build()).complete();
			}
		}
	}
}