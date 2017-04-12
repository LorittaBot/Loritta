package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;

public class QualidadeCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "qualidade";
	}

	public String getDescription() {
		return "Cria uma mensagem com Q U A L I D A D E & S I N C R O N I A";
	}

	public String getUsage() {
		return "<mensagem>";
	}

	public List<String> getExample() {
		return Arrays.asList("qualidade & sincronia");
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
			String qualidade = String.join(" ", context.getArgs());

			StringBuilder sb = new StringBuilder();
			for (char ch : qualidade.toCharArray()) {
				if (Character.isLetterOrDigit(ch)) {
					sb.append(Character.toUpperCase(ch));
					sb.append(" ");
				} else {
					sb.append(ch);
				}
			}
			context.sendMessage(context.getAsMention(true) + sb.toString());
		} else {
			this.explain(context.getConfig(), context.getEvent());
		}
	}

}
