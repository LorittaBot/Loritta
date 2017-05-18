package com.mrpowergamerbr.loritta.commands.vanilla.discord;

import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;

public class InviteCommand extends CommandBase {
	public String getDescription() {
		return "Envia o link do convite para adicionar a Loritta em outros servidores!";
	}

	public CommandCategory getCategory() {
		return CommandCategory.DISCORD;
	}

	@Override
	public String getLabel() {
		return "convite";
	}

	@Override
	public void run(CommandContext context) {
		context.sendMessage(context.getAsMention(true) + "https://discordapp.com/oauth2/authorize?client_id=297153970613387264&scope=bot&permissions=2080374975");
	}
}
