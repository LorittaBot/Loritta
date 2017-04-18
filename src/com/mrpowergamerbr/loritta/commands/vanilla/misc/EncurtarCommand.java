package com.mrpowergamerbr.loritta.commands.vanilla.misc;

import java.util.Arrays;
import java.util.List;

import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.utils.webpaste.BitlyURLShortener;

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
		if (context.getArgs().length == 1) {
			BitlyURLShortener bitly = new BitlyURLShortener();
			
			context.sendMessage(context.getAsMention(true) + bitly.shorten(context.getArgs()[0]));
		} else {
			context.explain();
		}
	}
}
