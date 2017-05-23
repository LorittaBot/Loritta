package com.mrpowergamerbr.loritta.commands.vanilla.utils;

import java.util.Arrays;
import java.util.List;

import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.utils.ColorUtils;

public class HexCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "hex";
	}

	@Override
	public String getUsage() {
		return "vermelho verde azul";
	}

	@Override
	public List<String> getExample() {
		return Arrays.asList("255 165 0");
	}

	@Override
	public String getDescription() {
		return "Transforme uma cor RGB para hexadecimal";
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.UTILS;
	}

	@Override
	public void run(CommandContext context) {
		if (context.getArgs().length == 3) {
			try {
				int r = Integer.parseInt(context.getArgs()[0]);
				int g = Integer.parseInt(context.getArgs()[1]);
				int b = Integer.parseInt(context.getArgs()[2]);

				String hex = String.format("#%02x%02x%02x", r, g, b);
				
				context.sendMessage(context.getAsMention(true) + String.format(" transformei a sua cor %s, %s, %s (%s) para hexadecimal! %s", r, g, b, new ColorUtils().getColorNameFromRgb(r, g, b), hex));
			} catch (Exception e) {
				context.sendMessage(context.getAsMention(true) + " todos os argumentos devem ser números!");
			}
		} else {
			context.explain();
		}
	}
}
