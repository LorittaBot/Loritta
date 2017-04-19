package com.mrpowergamerbr.loritta.commands.vanilla.misc;

import java.util.Arrays;
import java.util.List;

import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.utils.webpaste.TemmieBitly;

public class EncurtarCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "encurtar";
	}

	@Override
	public String getUsage() {
		return "link";
	}

	@Override
	public List<String> getExample() {
		return Arrays.asList("https://mrpowergamerbr.com/");
	}

	@Override
	public String getDescription() {
		return "Encurta um link usando o bit.ly";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MISC;
	}

	@Override
	public void run(CommandContext context) {
		if (context.getArgs().length >= 1) {
			TemmieBitly temmie = new TemmieBitly("R_fb665e9e7f6a830134410d9eb7946cdf", "o_5s5av92lgs");
			context.sendMessage(context.getAsMention(true) + temmie.shorten(context.getArgs()[0]));
		} else {
			context.explain();
		}
	}
}
