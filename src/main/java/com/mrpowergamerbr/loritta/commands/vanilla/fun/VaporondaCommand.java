package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.utils.VaporwaveUtils;

public class VaporondaCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "vaporonda";
	}

	public String getDescription() {
		return "Cria uma mensagem com ａｅｓｔｈｅｔｉｃｓ";
	}

	public String getUsage() {
		return "<mensagem>";
	}

	public List<String> getExample() {
		return Arrays.asList("Windows 95");
	}

	public Map<String, String> getDetailedUsage() {
		return ImmutableMap.<String, String>builder()
				.put("mensagem", "A mensagem que você deseja transformar")
				.build();
	}
	
	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}
	
	@Override
	public void run(CommandContext context) {
		if (context.getArgs().length >= 1) {			
			String vaporwave = VaporwaveUtils.vaporwave(String.join(" ", context.getArgs()).toLowerCase());
			context.sendMessage(context.getAsMention(true) + vaporwave);
		} else {
			this.explain(context.getConfig(), context.getEvent());
		}
	}
}
