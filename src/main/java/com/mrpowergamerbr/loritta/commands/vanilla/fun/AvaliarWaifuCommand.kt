package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.util.*

class AvaliarWaifuCommand : CommandBase("avaliarwaifu") {
	override fun getAliases(): List<String> {
		return listOf("ratemywaifu", "avaliarminhawaifu", "notawaifu");
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.RATEWAIFU_DESCRIPTION
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

			var reason = context.locale.RATEWAIFU_10.f()

			if (nota == 9) {
				reason = "${context.locale.RATEWAIFU_9.f()} <:osama:325332212255948802>";
			}
			if (nota == 8) {
				reason = context.locale.RATEWAIFU_8.f();
			}
			if (nota == 7) {
				reason = "${context.locale.RATEWAIFU_7.f()} 😊";
			}
			if (nota == 6) {
				reason = context.locale.RATEWAIFU_6.f();
			}
			if (nota == 5) {
				reason = context.locale.RATEWAIFU_5.f();
			}
			if (nota == 4) {
				reason = context.locale.RATEWAIFU_4.f();
			}
			if (nota == 3) {
				reason = context.locale.RATEWAIFU_3.f();
			}
			if (nota == 2) {
				reason = context.locale.RATEWAIFU_2.f();
			}
			if (nota == 1) {
				reason = context.locale.RATEWAIFU_1.f();
			}
			if (nota == 0) {
				reason = "🤦 ${context.locale.RATEWAIFU_0.f()}";
			}
			var strNota = nota.toString();
			if (joined == "Loritta") {
				strNota = "∞";
				reason = "${context.locale.RATEWAIFU_IM_PERFECT.f()} <:loritta_quebrada:338679008210190336>!"
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
			context.sendMessage(context.getAsMention(true) + context.locale.RATEWAIFU_RESULT.f(strNota, joined, reason));
		} else {
			this.explain(context);
		}
	}
}