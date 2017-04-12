package com.mrpowergamerbr.loritta.commands.vanilla.misc;

import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.kevinsawicki.http.HttpRequest;
import com.mrpowergamerbr.loritta.Loritta;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import com.mrpowergamerbr.loritta.utils.correios.EncomendaResponse;
import com.mrpowergamerbr.loritta.utils.correios.EncomendaResponse.PackageUpdate;

public class PackageInfoCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "correios";
	}

	@Override
	public String getDescription() {
		return "Mostra o status de uma encomenda dos correios, funciona com os Correios (Brasil) e a CTT (Portugal)";
	}

	@Override
	public List<String> getExample() {
		return Arrays.asList("correios");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MISC;
	}

	@Override
	public void run(CommandContext context) {
		if (context.getArgs().length == 1) {
			String packageId = context.getArgs()[0];
			// DU892822537BR
			Document doc = null;
			try {
				if (packageId.endsWith("PT")) { // Portugal
					String packageHtml = HttpRequest.get("http://pesquisarencomendas.com/ws/?ref=" + packageId).body();
					
					EncomendaResponse encRes = Loritta.getGson().fromJson(packageHtml, EncomendaResponse.class);
					
					String base = "";
					
					for (PackageUpdate update : encRes.getLocations()) {
						base += String.format("%s %s - %s - %s\n", update.getDate().replace(":", "/") /* deixar mais bonito */, update.getTime(), update.getLocation(), update.getState());
					}
					
					context.sendMessage(context.getAsMention(true) + "**Status para pacote \"" + packageId + "\"**\n" +
							"```" + base + "```");
				} else {
					doc = Jsoup.connect("http://websro.correios.com.br/sro_bin/txect01$.QueryList?P_ITEMCODE=&P_LINGUA=001&P_TESTE=&P_TIPO=001&P_COD_UNI=" + packageId).get();

					Elements elements = doc.select("tr"); // Hora de pegar todos os trs na página
					boolean first = true;
					String base = "";
					for (Element element : elements) {
						if (first) { first = false; continue; } // Ninguém liga para o primeiro tr
						Element hora = element.select("td").get(0); // Hora
						if (hora.hasAttr("colspan")) {
							// colspan = aconteceu no mesmo horário
							// então só é na verdade um update de status
							base += hora.text() + "\n";
							continue;
						}
						Element local = element.select("td").get(1); // Local
						Element status = element.select("td").get(2); // Status
						base += hora.text() + " - " + local.text() + " - " + status.text() + "\n";
					}
					context.sendMessage(context.getAsMention(true) + "**Status para pacote \"" + packageId + "\"**\n" +
							"```" + base + "```");
				}
			} catch (Exception e) {
				context.sendMessage(context.getAsMention(true) + "**Código de rastreio inválido!**");
			}
		} else {
			context.explain();
		}
	}
}
