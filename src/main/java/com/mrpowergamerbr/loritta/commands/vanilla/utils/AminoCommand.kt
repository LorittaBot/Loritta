package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.aminoreapi.AminoClient
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.humanize
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.time.ZoneOffset


class AminoCommand : CommandBase() {
	override fun getLabel(): String {
		return "amino"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.AMINO_DESCRIPTION
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS;
	}

	override fun getExtendedExamples(): Map<String, String> {
		return mapOf("pesquisar Undertale Brasil" to "Pesquisa \"Undertale Brasil\" no Amino",
				"converter" to "Converte uma imagem \".Amino\" para uma imagem normal")
	}

	override fun run(context: CommandContext) {
		if (context.args.size > 0) {
			if (context.args.size > 1) {
				if (context.args[0] == context.locale.SEARCH) {
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
						embed.addField("\uD83D\uDD17 Link", community.link, true);
						embed.addField("\uD83D\uDCBB ID", community.ndcId.toString(), true);
						embed.addField("\uD83D\uDC65 ${context.locale.AMINO_MEMBERS}", community.membersCount.toString(), true);
						embed.addField("\uD83C\uDF0E ${context.locale.AMINO_LANGUAGE}", community.primaryLanguage, true);
						embed.addField("\uD83D\uDD25 ${context.locale.AMINO_COMMUNITY_HEAT}", community.communityHeat, true);
						embed.addField("\uD83D\uDCC5 ${context.locale.AMINO_CREATED_IN}", javax.xml.bind.DatatypeConverter.parseDateTime(community.createdTime).toInstant().atOffset(ZoneOffset.UTC).humanize(), true);
						embed.setColor(Color(255, 112, 125));
						embed.setThumbnail(community.icon)

						context.sendMessage(context.asMention, embed.build());
					} else {
						context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.AMINO_COULDNT_FIND.msgFormat(args))
					}
				}
			} else {
				if (context.args[0] == context.locale.AMINO_CONVERT) { // Converter imagens .Amino para imagens normais
					if (context.message.attachments.isNotEmpty()) {
						val attachment = context.message.attachments[0]

						if (attachment.isImage) {
							val imagem = LorittaUtils.downloadImage(attachment.url)

							context.sendFile(imagem, "amino.png", "\uD83D\uDDBC **|** " + context.getAsMention(true) + context.locale.AMINO_YOUR_IMAGE.msgFormat(context.message.attachments[0].fileName))
							return;
						}
					}
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.AMINO_NO_IMAGE_FOUND)
					return;
				}
				context.explain()
			}
		} else {
			context.explain()
		}
	}
}