package com.mrpowergamerbr.loritta.commands.vanilla.magic;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand;

public class NashornTestCommand extends CommandBase {
	@Override public String getLabel() {
		return "nashorn";
	}

	@Override public CommandCategory getCategory() {
		return CommandCategory.MAGIC;
	}

	@Override public void run(CommandContext context) {
		if (context.getUserHandle().getId().equals(Loritta.getConfig()
				.getOwnerId())) { // Somente o dono do bot pode usar eval! Eval é uma ferramenta muito poderosa que não deve ser usada por qualquer um!
			String javaScript = String.join(" ", context.getArgs());

			NashornCommand nashornCmd = new NashornCommand("teste", javaScript);

			System.out.println("Executando NashornCommand...");

			nashornCmd.handle(context.getEvent(), context.getConfig().commandPrefix + "teste", context.getConfig());
		} else {
			// Sem permissão
		}
	}
}
