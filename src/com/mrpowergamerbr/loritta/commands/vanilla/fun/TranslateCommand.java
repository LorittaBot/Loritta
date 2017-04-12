package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import java.util.Arrays;
import java.util.List;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;

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
			Language lang = null;
			for (Language aux : Language.values()) {
				if (aux.toString().equalsIgnoreCase(strLang)) {
					lang = aux;
					break;
				}
			}

			if (lang != null) {
				try {
					String key = Loritta.getMicrosoftTranslateServiceKey();
					Translate.setClientId(key.split(";")[0]);
					Translate.setClientSecret(key.split(";")[1]);

					String translatedText = Translate.execute(text, Language.AUTO_DETECT, lang);

					context.sendMessage(context.getAsMention(true) + translatedText);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					context.sendMessage(context.getAsMention(true) + "Linguagem \"" + strLang + "\" não é suportada!\nLinguas suportadas: " + String.join(", ", Language.getLanguageCodesForTranslation()));
				} catch (Exception e) {} // Tantos exceptions...
			}
		} else {
			context.explain();
		}
	}
}
