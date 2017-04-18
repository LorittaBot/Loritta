package com.mrpowergamerbr.loritta.commands.vanilla.misc;

import java.io.File;
import java.io.IOException;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandContext;

import net.dv8tion.jda.core.MessageBuilder;

public class AngelCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "angel";
	}
	
	@Override
	public String getDescription() {
		return "Mostra o meu avatar!";
	}

	@Override
	public void run(CommandContext context) {
		try {
			context.sendFile(new File(Loritta.FOLDER + "angel.png"), "angel.png", new MessageBuilder().append(" ").build());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
