package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import java.util.Arrays;
import java.util.List;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.utils.translate.GoogleTranslateUtils;

public class TranslateCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "traduzir";
	}

	@Override
	public String getDescription() {
		return "Traduz uma frase para outra linguagem";
	}

	@Override
	public String getUsage() {
		return "língua texto";
	}

	@Override
	public List<String> getExample() {
		return Arrays.asList("pt Hello World!");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public void run(CommandContext context) {
		if (context.getArgs().length >= 2) {	
			String strLang = context.getArgs()[0];
			context.getArgs()[0] = ""; // Super workaround
			String text = String.join(" ",context.getArgs());

			try {
				String translatedText = GoogleTranslateUtils.translate(text, "auto", strLang);

				context.sendMessage(context.getAsMention(true) + translatedText);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			context.explain();
		}
	}
}
