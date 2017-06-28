package com.mrpowergamerbr.loritta.commands.vanilla.misc;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mrpowergamerbr.loritta.commands.CommandBase;
import com.mrpowergamerbr.loritta.commands.CommandCategory;
import com.mrpowergamerbr.loritta.commands.CommandContext;
import net.dv8tion.jda.core.EmbedBuilder;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class WikipediaCommand extends CommandBase {
    @Override
    public String getLabel() {
        return "wikipedia";
    }

    @Override
    public String getDescription() {
        return "Mostra uma versão resumida de uma página do Wikipedia";
    }

    public String getUsage() {
        return "[linguagem] conteúdo";
    }

    public List<String> getExample() {
        return Arrays.asList("Minecraft", "[en] Shantae");
    }

    public Map<String, String> getDetailedUsage() {
        return ImmutableMap.<String, String>builder()
                .put("linguagem", "*(Opcional)* Código de linguagem para procurar no Wikipédia, entre [], por padrão ele irá procurar na Wikipedia de Portugal [pt]")
                .put("conteúdo", "O que você deseja procurar no Wikipédia")
                .build();
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MISC;
    }

    @Override
    public void run(CommandContext context) {
        if (context.getArgs().length >= 1) {
            String languageId = "pt";
            String inputLanguageId = context.getArgs()[0];
            boolean hasValidLanguageId = false;
            if (inputLanguageId.startsWith("[") && inputLanguageId.endsWith("]")) {
                languageId = inputLanguageId.substring(1, inputLanguageId.length() - 1);
                hasValidLanguageId = true;
            }
            try {
                String query = StringUtils.join(context.getArgs(), " ", hasValidLanguageId ? 1 : 0, context.getArgs().length);
                String wikipediaResponse = HttpRequest.get("https://" + languageId + ".wikipedia.org/w/api.php?format=json&action=query&prop=extracts&redirects=1&exintro=&explaintext=&titles=" + URLEncoder.encode(query, "UTF-8")).body();

                // Resolvi usar JsonParser em vez de criar um objeto para o Gson desparsear... a response do Wikipedia é meio "estranha"
                StringReader reader = new StringReader(wikipediaResponse);
                JsonReader jsonReader = new JsonReader(reader);
                jsonReader.setLenient(true);
                JsonObject wikipedia = new JsonParser().parse(jsonReader).getAsJsonObject(); // Base
                JsonObject wikiQuery = wikipedia.getAsJsonObject("query"); // Query
                JsonObject wikiPages = wikiQuery.getAsJsonObject("pages"); // Páginas
                Entry<String, JsonElement> entryWikiContent = wikiPages.entrySet().iterator().next(); // Conteúdo

                if (entryWikiContent.getKey().equals("-1")) { // -1 = Nenhuma página encontrada
                    context.sendMessage(context.getAsMention(true) + "Não consegui encontrar nada relacionado á **" + query + "** 😞");
                } else {
                    // Se não é -1, então é algo que existe! Yay!
                    String pageTitle = entryWikiContent.getValue().getAsJsonObject().get("title").getAsString();
                    String pageExtract = entryWikiContent.getValue().getAsJsonObject().get("extract").getAsString();

                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle(pageTitle, null)
                            .setColor(Color.BLUE)
                            .setDescription(pageExtract.length() > 512 ? pageExtract.substring(0, 509) + "..." : pageExtract);

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
