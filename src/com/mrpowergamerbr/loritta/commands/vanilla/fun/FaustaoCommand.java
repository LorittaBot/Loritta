package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import java.util.Arrays;
import java.util.List;
import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;

public class FaustaoCommand extends CommandBase {
	private static List<String> frases = Arrays.asList(
			"Que isso bixo, ó u cara lá ó",
			"Vamos ver as vídeos cassetadas",
			"Voltamos já com vídeos cassetadas",
			"ERRRROOOOOOOOOUUUUUUUU!!!!",
			"E agora, pra desligar essa merda aí, meu. Porra ligou, agora desliga! Tá pegando fogo bixo!",
			"Está fera ai bixo",
			"Olha o tamanho da Criança",
			"Oito e sete",
			"Ô loco meu!",
			"É brincadera bicho.",
			"Se vira nos 30!",
			"Quem sabe faz ao vivo!");

	private static List<String> avatars = Arrays.asList(
			"https://cdn.mensagenscomamor.com/content/images/img/f/faustao_projac_rasteira.jpg",
			"https://observatoriodatelevisao.bol.uol.com.br/wp-content/uploads/2016/12/faust%C3%A3os.jpg",
			"http://www.g17.com.br/imagens/noticias/2013/abril/faustao.jpg",
			"https://s-media-cache-ak0.pinimg.com/originals/42/48/f7/4248f7daa9601a880fb8d252f3b716d3.jpg",
			"http://static2.blastingnews.com/media/photogallery/2015/12/14/290x290/b_290x290/faustao-recebe-advertencia-da-globo_529139.jpg");

	@Override
	public String getLabel() {
		return "faustão";
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
		TemmieWebhook temmie = Loritta.getOrCreateWebhook(context.getEvent().getTextChannel(), "Faustão");

		temmie.sendMessage(DiscordMessage.builder()
				.username("Faustão")
				.content(frases.get(Loritta.getRandom().nextInt(frases.size())))
				.avatarUrl(avatars.get(Loritta.getRandom().nextInt(avatars.size())))
				.build());
	}
}
