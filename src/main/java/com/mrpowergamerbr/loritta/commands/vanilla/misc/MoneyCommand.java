package com.mrpowergamerbr.loritta.commands.vanilla.misc;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;

public class MoneyCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "money";
	}

	@Override
	public String getDescription() {
		return "Transforma o valor de uma moeda em outra moeda. (Por exemplo: Ver quanto está valendo o dólar em relação ao real)";
	}

	@Override
	public List<String> getExample() {
		return Arrays.asList("USD BRL");
	}

	@Override
	public CommandCategory getCategory() {
		return CommandCategory.MISC;
	}

	@Override
	public void run(CommandContext context) {
		if (context.getArgs().length == 2) {
			String from = context.getArgs()[0].toUpperCase();
			String to = context.getArgs()[1].toUpperCase();

			String response = HttpRequest.get("http://api.fixer.io/latest?base=" + from).acceptJson().body();
			StringReader reader = new StringReader(response);
			JsonReader jsonReader = new JsonReader(reader);
			JsonObject fixerResponse = new JsonParser().parse(jsonReader).getAsJsonObject(); // Base

			if (fixerResponse.has("error")) {
				context.sendMessage(context.getAsMention(true) + from + " não é uma moeda válida! 💸");
				return;
			}
			JsonObject rates = fixerResponse.get("rates").getAsJsonObject();
			if (!rates.has(to)) {
				context.sendMessage(context.getAsMention(true) + to + " não é uma moeda válida! 💸");
				return;
			}
			
			double converted = rates.get(to).getAsDouble();
			context.sendMessage(context.getAsMention(true) + "💵 " + from + " para " + to + ": " + converted + " " + to);
		} else {
			this.explain(context.getConfig(), context.getEvent());
		}
	}
}
