package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import java.util.Arrays;
import java.util.List;
import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;

public class MagicBallCommand extends CommandBase {
	List<String> responses = Arrays.asList(
			"Vai incomodar outra pessoa, obrigado.",
			"Não sei, mas eu sei que eu moro lá no Cambuci.",
			"Do jeito que eu vejo, sim.",
			"Hmmmm... 🤔",
			"Não posso falar sobre isso.",
			"Não.",
			"Sim.",
			"Eu responderia, mas não quero ferir seus sentimentos.",
			"Provavelmente sim",
			"Provavelmente não",
			"Minhas fontes dizem que sim",
			"Minhas fontes dizem que não",
			"Você pode acreditar nisso",
			"Minha resposta é não",
			"Minha resposta é sim",
			"Do jeito que eu vejo, não.",
			"Melhor não falar isto para você agora...",
			"Sim, com certeza!",
			"Também queria saber...",
			"A minha resposta não importa, o que importa é você seguir o seu coração. 😘",
			"Talvez...",
			"Acho que sim.",
			"Acho que não.",
			"Talvez sim.",
			"Talvez não.",
			"Sim!",
			"Não!",
			"¯\\_(ツ)_/¯");

	@Override
	public String getLabel() {
		return "vieirinha";
	}

	@Override
	public String getDescription() {
		return "Pergunte algo para o Vieirinha";
	}

	@Override
	public List<String> getExample() {
		return Arrays.asList("você me ama?");
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
		if (context.getArgs().length >= 1) {
			TemmieWebhook temmie = Loritta.getOrCreateWebhook(context.getEvent().getTextChannel(), "Vieirinha");

			context.sendMessage(temmie, DiscordMessage.builder()
					.username("Vieirinha")
					.content(context.getAsMention(true) + responses.get(Loritta.getRandom().nextInt(responses.size())))
					.avatarUrl("http://i.imgur.com/rRtHdti.png")
					.build());
		} else {
			context.explain();
		}
	}
}
