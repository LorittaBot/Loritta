package net.perfectdreams.loritta.cinnamon.platform.commands.utils

import dev.kord.common.Color
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.DictionaryCommand
import org.jsoup.Jsoup
import java.net.URLEncoder

class DictionaryExecutor(loritta: LorittaCinnamon, val http: HttpClient) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val language = string("language", DictionaryCommand.I18N_PREFIX.Options.Language) {
            choice(DictionaryCommand.I18N_PREFIX.Languages.PtBr, "pt-br")
        }

        val word = string("word", DictionaryCommand.I18N_PREFIX.Options.Word)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        // TODO: More languages
        val language = args[options.language]
        val wordToBeSearched = args[options.word]

        val httpResponse = http.get(
            "https://www.dicio.com.br/pesquisa.php?q=${
                URLEncoder.encode(
                    wordToBeSearched,
                    "UTF-8"
                )
            }"
        )

        if (httpResponse.status == HttpStatusCode.NotFound)
            context.failEphemerally(
                context.i18nContext.get(DictionaryCommand.I18N_PREFIX.WordNotFound),
                Emotes.Error
            )

        val response = httpResponse.bodyAsText()

        var jsoup = Jsoup.parse(response)

        // Ao usar pesquisa.php, ele pode retornar uma página de pesquisa ou uma página com a palavra, por isto iremos primeiro descobrir se estamos na página de pesquisa
        val resultadosClass = jsoup.getElementsByClass("resultados")
        val resultados = resultadosClass.firstOrNull()

        if (resultados != null) {
            val resultadosLi = resultados.getElementsByTag("li").firstOrNull()
                ?: context.failEphemerally(
                    context.i18nContext.get(DictionaryCommand.I18N_PREFIX.WordNotFound),
                    Emotes.Error
                )

            val linkElement = resultadosLi.getElementsByClass("_sugg").first()
            val link = linkElement.attr("href")

            val httpRequest2 = http.get("https://www.dicio.com.br$link")

            // This should *never* happen because we are getting it directly from the search results, but...
            if (httpRequest2.status == HttpStatusCode.NotFound)
                context.failEphemerally(
                    context.i18nContext.get(DictionaryCommand.I18N_PREFIX.WordNotFound),
                    Emotes.Error
                )

            val response2 = httpRequest2.bodyAsText()

            jsoup = Jsoup.parse(response2)
        }

        // Se a página não possui uma descrição ou se ela possui uma descrição mas começa com "Ainda não temos o significado de", então é uma palavra inexistente!
        if (jsoup.select("p[itemprop = description]").isEmpty() || jsoup.select("p[itemprop = description]")[0].text()
                .startsWith("Ainda não temos o significado de")
        )
            context.failEphemerally(
                context.i18nContext.get(DictionaryCommand.I18N_PREFIX.WordNotFound),
                Emotes.Error
            )

        val description = jsoup.select("p[itemprop = description]")[0]

        val type = description.getElementsByTag("span")[0]
        val word = jsoup.select("h1[itemprop = name]")
        val what = description.getElementsByTag("span").getOrNull(1)
        val etim = description.getElementsByClass("etim").firstOrNull()

        // *Technically* the image is always
        // https://s.dicio.com.br/word_here.jpg
        // Example: https://s.dicio.com.br/bunda.jpg
        // However we wouldn't be sure if the image really exists, so let's check in the page
        val wordImage = jsoup.getElementsByClass("imagem-palavra").firstOrNull()
        val frase = if (jsoup.getElementsByClass("frase").isNotEmpty()) {
            jsoup.getElementsByClass("frase")[0]
        } else {
            null
        }

        context.sendMessage {
            embed {
                color = Color(25, 89, 132)  // TODO: Move this to a object

                title = "📙 ${context.i18nContext.get(DictionaryCommand.I18N_PREFIX.MeaningOf(word.text()))}"

                this.description = buildString {
                    append("*${type.text()}*")

                    if (what != null)
                        append("\n\n**${what.text()}**")
                }

                // The first in the page will always be "synonyms"
                // While the second in the page will always be "opposites"
                jsoup.getElementsByClass("sinonimos").getOrNull(0)?.let {
                    field("\uD83D\uDE42 ${context.i18nContext.get(DictionaryCommand.I18N_PREFIX.Synonyms)}", it.text())
                }

                jsoup.getElementsByClass("sinonimos").getOrNull(1)?.let {
                    field("\uD83D\uDE41 ${context.i18nContext.get(DictionaryCommand.I18N_PREFIX.Opposite)}", it.text())
                }

                frase?.let {
                    field("\uD83D\uDD8B ${context.i18nContext.get(DictionaryCommand.I18N_PREFIX.Sentence)}", it.text())
                }

                if (wordImage != null)
                // We need to use "data-src" because the page uses lazy loading
                    image = wordImage.attr("data-src")

                if (etim != null)
                    footer(etim.text())
            }
        }
    }
}