package com.mrpowergamerbr.loritta.commands.vanilla.magic;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;

public class ChangeGameCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "changegame";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MAGIC;
	}
	
	@Override
	public void run(CommandContext context) {
		if (context.getUserHandle().getId().equals(Loritta.botOwnerId)) {
			String text = String.join(" ",context.getArgs());
			Loritta.setPlaying(text);
			context.sendMessage(context.getAsMention(true) + "Alterado com sucesso!");
		} else {
			// Sem permissão
		}
	}
}
