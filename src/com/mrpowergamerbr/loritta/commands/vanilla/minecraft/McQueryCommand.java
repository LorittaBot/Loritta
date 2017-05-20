package com.mrpowergamerbr.loritta.commands.vanilla.minecraft;

import java.awt.Color;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

public class McQueryCommand extends CommandBase {
	@Override
	public String getLabel() {
		return "mcquery";
	}

	public String getDescription() {
		return "Mostra quantos players um servidor de Minecraft tem";
	}

	public CommandCategory getCategory() {
		return CommandCategory.MINECRAFT;
	}

	public String getUsage() {
		return "IP do servidor";
	}

	public List<String> getExample() {
		return Arrays.asList("jogar.sparklypower.net");
	}

	@Override
	public void run(CommandContext context) {
		if (context.getArgs().length == 1) {
			String ip = context.getArgs()[0];
			String hostname = ip;
			int port = 25565;
			if (ip.contains(":")) {
				// IP + Porta
				hostname = ip.split(":")[0];
				try {
					port = Integer.parseInt(ip.split(":")[1]);
				} catch (Exception e) {} // Calma cara, já entendi, essa porta tá errada
			}

			String body = HttpRequest.get("https://mcapi.ca/query/" + hostname + ":" + port + "/extensive").body();
			// Vamos tentar realizar primeiro uma extensive query
			StringReader reader = new StringReader(body);
			JsonReader jsonReader = new JsonReader(reader);
			jsonReader.setLenient(true);
			JsonObject serverResponse = new JsonParser().parse(jsonReader).getAsJsonObject(); // Base
			JsonObject plainResponse = null;

			// Nós também iremos pegar a "plain response", para alguns servidores aonde a extensive query é muito... "extensive" (exemplo: MOTD)
			body = HttpRequest.get("https://mcapi.ca/query/" + hostname + ":" + port + "/info").body();
			// Vamos tentar realizar primeiro uma extensive query
			reader = new StringReader(body);
			jsonReader = new JsonReader(reader);
			jsonReader.setLenient(true);
			plainResponse = new JsonParser().parse(body).getAsJsonObject(); // Response padrão então

			if (serverResponse.has("error")) {
				serverResponse = plainResponse; // Ok, se serverResponse (extensive) está com erro, vamos apenas "trocar" a resposta
			}
			if (serverResponse.has("error")) { // E se ainda está com erro... bem, desisto.
				// desisto :(
				context.sendMessage(context.getAsMention(true) + "Servidor **" + ip + ":" + port + "** não existe ou está offline!");
				try {
					jsonReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			EmbedBuilder builder = new EmbedBuilder();

			builder.setColor(Color.GREEN);

			builder.setTitle(hostname + ":" + port, null);

			addIfExists(builder, plainResponse, "MOTD", "motd", false); // Vamos usar o plain, o extensive pega muitas coisas do BungeeCord
			addIfExists(builder, plainResponse, "Ping", "ping", true); // Ping apenas no plain
			
			if (plainResponse.has("players")) { // Plain novamente, já que o extensive mostra o player count do BungeeCord... normalmente é 1
				builder.addField("Players", plainResponse.getAsJsonObject("players").get("online").getAsString() + "/" + plainResponse.getAsJsonObject("players").get("max").getAsString(), true);
			}
			
			addIfExists(builder, serverResponse, "Software", "software", true);
			addIfExists(builder, serverResponse, "Versão", "version", true);
			addIfExists(builder, plainResponse, "Protocolo", "protocol", true); // Protocolo só tem no plain
			
			if (serverResponse.has("list")) { // Players online
				StringBuilder list = new StringBuilder();
				
				boolean first = true;
				for (JsonElement str : serverResponse.get("list").getAsJsonArray()) {
					if (first) {
						list.append(str.getAsString().replace("_", "\\_")); // Um pouco de "strip markdown"
						first = false;
					} else {
						list.append(", " + str.getAsString().replace("_", "\\_"));
					}
				}
				builder.addField("Players", list.toString(), true);
			}
			
			if (serverResponse.has("plugins")) { // Players online
				String plugins = serverResponse.get("plugins").getAsString();
				builder.addField("Plugins", (plugins.isEmpty() ? "¯\\_(ツ)_/¯" : plugins.toString()), false);
			}
			
			builder.setThumbnail("https://mcapi.ca/query/" + hostname + ":" + port + "/icon"); // E agora o server-icon do servidor
			
			Message message = new MessageBuilder().append(context.getAsMention(true)).setEmbed(builder.build()).build();

			context.sendMessage(message);
			
			try {
				jsonReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			context.explain();
		}
	}

	private void addIfExists(EmbedBuilder builder, JsonObject serverResponse, String name, String get, boolean inline) {
		if (serverResponse.has(get)) {
			String val = serverResponse.get(get).getAsString();
			if (get.equals("motd")) {
				val = val.replaceAll("§[0-9a-fk-or]", "");
			}
			if (get.equals("ping")) {
				val = val + "ms";
			}
			builder.addField(name, val, inline);
		}
	}
}