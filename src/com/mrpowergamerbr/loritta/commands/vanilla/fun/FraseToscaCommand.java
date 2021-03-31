package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import java.util.Arrays;
import java.util.List;

import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.LorittaLauncher;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;

public class FraseToscaCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "frasetosca";
	}

	@Override
	public String getDescription() {
		return "Cria uma frase tosca utilizando várias mensagens recicladas recebidas pela Loritta";
	}

	@Override
	public List<String> getExample() {
		return Arrays.asList("wow");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public boolean hasCommandFeedback() {
		return false;
	}
	
	@Override
	public void run(CommandContext context) {
		String text = null;
		if (context.getArgs().length >= 1) {
			text = LorittaLauncher.getInstance().getHal().getSentence(String.join(" ", context.getArgs()));
		} else {
			text = LorittaLauncher.getInstance().getHal().getSentence();
		}
		text = text.length() > 400 ? text.substring(0, 400) + "..." : text;
		TemmieWebhook webhook = Loritta.getOrCreateWebhook(context.getEvent().getTextChannel(), "Frase Tosca");
		webhook.sendMessage(DiscordMessage.builder()
				.username("Gabriela, a amiga da Loritta")
				.content(context.getAsMention(true) + text.replace("@", ""))
				.avatarUrl("http://i.imgur.com/aATogAg.png")
				.build());
	}
}
