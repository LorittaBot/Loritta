package com.mrpowergamerbr.loritta.commands.vanilla.magic;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;

public class ReloadCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "reload";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MAGIC;
	}
	
	@Override
	public void run(CommandContext context) {
		if (context.getUserHandle().getId().equals(Loritta.botOwnerId)) {
			LorittaLauncher.getInstance().loadCommandManager();
			context.sendMessage("Loritta recarregada com sucesso!");
		} else {
			// Sem permissão
		}
	}
}
