package com.mrpowergamerbr.loritta.commands.vanilla.misc;

import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandContext;

public class PingCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "ping";
	}

	@Override
	public String getDescription() {
		return "Um comando de teste para ver se eu estou funcionando, recomendo que você deixe isto ligado para testes!";
	}
	
	@Override
	public void run(CommandContext context) {
		context.sendMessage(context.getAsMention(true) + "🏓 Pong! " + LorittaLauncher.getInstance().getJda().getPing() + "ms");
	}
}
