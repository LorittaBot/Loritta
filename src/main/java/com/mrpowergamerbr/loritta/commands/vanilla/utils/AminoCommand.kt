package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.aminoreapi.AminoClient
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color


class AminoCommand : CommandBase() {
	override fun getLabel(): String {
		return "amino"
	}

	override fun getDescription(): String {
		return "Comandos relacionados ao Amino! ([http://aminoapps.com/](http://aminoapps.com/))"
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS;
	}

	override fun run(context: CommandContext) {
		if (context.args.size > 1 && context.args[0] == "pesquisar") {
			// Pesquisar uma comunidade no Amino
			var aminoClient = AminoClient(Loritta.config.aminoEmail, Loritta.config.aminoPassword, Loritta.config.aminoDeviceId);
			aminoClient.login();

			var args = context.args.sliceArray(1..context.rawArgs.size - 1).joinToString(" ");
			var communities = aminoClient.searchCommunities(args, 0, 1, "pt", 1);

			if (communities.isEmpty()) {
				// Ok, não encontramos nada... mas que tal nós pesquisarmos em inglês?
				communities = aminoClient.searchCommunities(args, 0, 1, "en", 1);
			}
			if (communities.isNotEmpty()) {
				var community = communities[0];

				var embed = EmbedBuilder();
				embed.setTitle("<:amino:329308203684724737> " + community.name)
				embed.setDescription(community.tagline);
				embed.addField("Link", community.link, true);
				embed.addField("ID", community.ndcId.toString(), true);
				embed.addField("Membros", community.membersCount.toString(), true);
				embed.addField("Linguagem", community.primaryLanguage.toString(), true);
				embed.setColor(Color(255, 112, 125));
				embed.setThumbnail(community.icon)

				context.sendMessage(embed.build());
			} else {
				context.sendMessage(LorittaUtils.ERROR + " | " + context.getAsMention(true) + "Não encontrei nenhuma comunidade chamada `$args`!")
			}
		} else {
			context.explain()
		}
	}
}