package com.mrpowergamerbr.loritta.commands.vanilla.misc;

import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandContext;

public class PingCommand extends CommandBase {

	@Override
	public String getLabel() {
		return "ping";
	}

	@Override
	public void run(CommandContext context) {
		context.sendMessage(context.getAsMention(true) + "🏓 Pong!");
	}
}
