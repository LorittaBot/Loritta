package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandContext;

public class CaraCoroaCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "girarmoeda";
	}

	@Override
	public void run(CommandContext context) {
		context.sendMessage(context.getAsMention(true) + (Loritta.getRandom().nextBoolean() ? "Cara!" : "Coroa!"));
	}

}
