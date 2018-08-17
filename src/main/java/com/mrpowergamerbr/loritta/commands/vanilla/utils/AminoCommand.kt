package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.aminoreapi.AminoClient
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.commons.io.IOUtils
import java.awt.Color
import java.net.HttpURLConnection
import java.net.URL
import java.time.ZoneOffset


class AminoCommand : AbstractCommand("amino", category = CommandCategory.UTILS) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["AMINO_DESCRIPTION"]
	}

	override fun getExtendedExamples(): Map<String, String> {
		return mapOf("pesquisar Undertale Brasil" to "Pesquisa \"Undertale Brasil\" no Amino",
				"converter" to "Converte uma imagem \".Amino\" para uma imagem normal")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			if (context.args.size > 1) {
				if (context.args[0] == context.locale["SEARCH"]) {
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
						embed.setTitle("<:amino:375313236234469386> " + community.name)
						embed.setDescription(community.tagline);
						embed.addField("\uD83D\uDD17 Link", community.link, true);
						embed.addField("\uD83D\uDCBB ID", community.ndcId.toString(), true);
						embed.addField("\uD83D\uDC65 ${context.locale["AMINO_MEMBERS"]}", community.membersCount.toString(), true);
						embed.addField("\uD83C\uDF0E ${context.locale["AMINO_LANGUAGE"]}", community.primaryLanguage, true);
						embed.addField("\uD83D\uDD25 ${context.locale["AMINO_COMMUNITY_HEAT"]}", community.communityHeat, true);
						embed.addField("\uD83D\uDCC5 ${context.locale["AMINO_CREATED_IN"]}", javax.xml.bind.DatatypeConverter.parseDateTime(community.createdTime).toInstant().atOffset(ZoneOffset.UTC).humanize(locale), true);
						embed.setColor(Color(255, 112, 125));
						embed.setThumbnail(community.icon)

						context.sendMessage(context.asMention, embed.build());
					} else {
						context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["AMINO_COULDNT_FIND", args])
					}
				}
			} else {
				if (context.args[0] == context.locale["AMINO_CONVERT"]) { // Converter imagens .Amino para imagens normais
					if (context.message.attachments.isNotEmpty()) {
						val imageUrl = URL(context.message.attachments.first().url)
						val connection = imageUrl.openConnection() as HttpURLConnection
						connection.setRequestProperty("User-Agent",
								Constants.USER_AGENT)

						val byteArray = IOUtils.toByteArray(connection.inputStream)
						// Nós não conseguimos detectar se a imagem é uma GIF a partir do content type...
						val isGif = byteArray.let {
							val byte0 = it[0].toInt()
							val byte1 = it[1].toInt()
							val byte2 = it[2].toInt()
							val byte3 = it[3].toInt()
							val byte4 = it[4].toInt()
							val byte5 = it[5].toInt()

							byte0 == 0x47 && byte1 == 0x49 && byte2 == 0x46 && byte3 == 0x38 && byte4 == 0x39 && byte5 == 0x61 // GIF89a
						}

						val extension = if (isGif) "gif" else "png"
						context.sendFile(byteArray.inputStream(), "amino.$extension", "\uD83D\uDDBC **|** " + context.getAsMention(true) + context.locale["AMINO_YOUR_IMAGE", context.message.attachments[0].fileName])
						return
					}
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["AMINO_NO_IMAGE_FOUND"])
					return
				}
				context.explain()
			}
		} else {
			context.explain()
		}
	}
}