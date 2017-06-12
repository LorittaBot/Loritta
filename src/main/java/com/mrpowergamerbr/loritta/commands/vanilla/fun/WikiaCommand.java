package com.mrpowergamerbr.loritta.commands.vanilla.fun;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import net.dv8tion.jda.core.EmbedBuilder;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import java.awt.*;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WikiaCommand extends CommandBase {
    @Override
    public String getLabel() {
        return "wikia";
    }

    @Override
    public String getDescription() {
        return "Procure algo em uma fandom na wikia";
    }

    public String getUsage() {
        return "url conteúdo";
    }

    public List<String> getExample() {
        return Arrays.asList("parappatherapper Katy Kat");
    }

    public Map<String, String> getDetailedUsage() {
        return ImmutableMap.<String, String>builder()
                .put("url", "URL de uma Wikia, se a URL de uma Wikia é \"http://naruto.wikia.com\", você deverá colocar \"naruto\"")
                .put("conteúdo", "O que você deseja procurar na Wikia")
                .build();
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public void run(CommandContext context) {
        if (context.getArgs().length >= 2) {
            String websiteId = context.getArgs()[0];
            try {
                String query = StringUtils.join(context.getArgs(), " ", 1, context.getArgs().length);
                String body = HttpRequest.get("http://" + websiteId + ".wikia.com/api/v1/Search/List/?query=" + URLEncoder.encode(query, "UTF-8") + "&limit=1&namespaces=0%2C14").body();

                // Resolvi usar JsonParser em vez de criar um objeto para o Gson desparsear..
                StringReader reader = new StringReader(body);
                JsonReader jsonReader = new JsonReader(reader);
                jsonReader.setLenient(true);
                JsonObject wikiaResponse = new JsonParser().parse(jsonReader).getAsJsonObject(); // Base

                if (wikiaResponse.has("exception")) {
                    context.sendMessage(context.getAsMention(true) + "Não consegui encontrar nada relacionado á **" + query + "** 😞");
                } else {
                    JsonObject item = wikiaResponse.get("items").getAsJsonArray().get(0).getAsJsonObject(); // Nós iremos pegar o 0, já que é o primeiro resultado

                    String pageTitle = item.get("title").getAsString();
                    String pageExtract = Jsoup.parse(item.get("snippet").getAsString()).text();
                    String pageUrl = item.get("url").getAsString();

                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle(pageTitle, pageUrl)
                            .setColor(Color.BLUE)
                            .setDescription(pageExtract.length() > 2048 ? pageExtract.substring(0, 2044) + "..." : pageExtract);

                    context.sendMessage(embed.build()); // Envie a mensagem!
                }
            } catch (Exception e) {
                e.printStackTrace();
                context.sendMessage(context.getAsMention(true) + "**Deu ruim!**");
            }
        } else {
            context.explain();
        }
    }
}
