package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.utils.VaporwaveUtils;

public class VaporQualidadeCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "vaporqualidade";
	}

	public String getDescription() {
		return "Quando você mistura Q U A L I D A D E e ａｅｓｔｈｅｔｉｃｓ";
	}

	public String getUsage() {
		return "<mensagem>";
	}

	public List<String> getExample() {
		return Arrays.asList("kk eae men, o sam é brabo");
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
			String qualidade = VaporwaveUtils.vaporwave(String.join(" ", context.getArgs()));

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
