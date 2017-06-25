package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import java.util.*

class AvaliarWaifuCommand : CommandBase() {
	override fun getLabel(): String {
		return "avaliarwaifu"
	}

	override fun getAliases(): List<String> {
		return listOf("ratemywaifu", "avaliarminhawaifu", "notawaifu");
	}

	override fun getDescription(): String {
		return "Receba uma nota para a sua Waifu!"
	}

	override fun getExample(): List<String> {
		return listOf("Loritta");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN;
	}

	override fun getUsage(): String {
		return "<usuário 1>"
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			var joined = context.args.joinToString(separator = " "); // Vamos juntar tudo em uma string
			if (context.message.mentionedUsers.isNotEmpty()) {
				joined = context.message.mentionedUsers[0].name;
			}
			var random = SplittableRandom(Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + joined.hashCode().toLong()) // Usar um random sempre com a mesma seed
			var nota = random.nextInt(0, 11);

			var reason = "Simplesmente perfeita! Não trocaria de Waifu se fosse você!"

			if (nota == 9) {
				reason = "Uma Waifu excelente, ótima escolha. <:osama:325332212255948802>";
			}
			if (nota == 8) {
				reason = "Uma Waifu que acerta em todos os pontos bons da vida.";
			}
			if (nota == 7) {
				reason = "Nem todas as Waifus são perfeitas, mas qual seria a graça de viver com alguém perfeito? 😊";
			}
			if (nota == 6) {
				reason = "Se fosse nota de escola sua Waifu ela seria \"acima da média\"";
			}
			if (nota == 5) {
				reason = "Nem tão ruim, nem tão boa, bem \"normal\"";
			}
			if (nota == 4) {
				reason = "Não que a sua Waifu seja ruim, pelo contrário! Ela tem potencial para ser algo mais *interessante*!";
			}
			if (nota == 3) {
				reason = "Sua Waifu precisa de mais substância.";
			}
			if (nota == 2) {
				reason = "Não é por nada não mas, se eu você fosse, eu trocaria de Waifu...";
			}
			if (nota == 1) {
				reason = "Sem chance, troca de Waifu hoje mesmo para garantir sua sanidade.";
			}
			if (nota == 0) {
				reason = "🤦 Troque de Waifu por favor.";
			}
			var strNota = nota.toString();
			if (joined == "Loritta") {
				strNota = "∞";
				reason = "Sou perfeita!"
			}
			if (joined == "Tatsumaki") {
				strNota = "10";
				reason = "Minha amiga mais velha, sabia que eu fui inspirada nela! Não trocaria de Waifu (e nem de bot) se fosse você!"
			}
			if (joined == "Mee6") {
				strNota = "6";
				reason = "Tem potencial para ser melhor... Mas atualmente é um bot muito usado mas que precisa de mais funções úteis."
			}
			if (joined == "Dyno") {
				strNota = "7";
				reason = "Eu acho que ele é bom para administração..."
			}
			if (joined == "NotSoBot") {
				strNota = "8";
				reason = "Uma boa Waifu, mas precisa ser alguém menos zueiro e, é claro, alguém que não fica travando toda hora."
			}
			context.sendMessage(context.getAsMention(true) + "Eu dou uma nota **$strNota/10** para `$joined`! **$reason**");
		} else {
			this.explain(context);
		}
	}
}