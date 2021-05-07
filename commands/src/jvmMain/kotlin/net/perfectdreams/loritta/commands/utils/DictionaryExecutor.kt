package net.perfectdreams.loritta.commands.utils

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.perfectdreams.loritta.commands.utils.declarations.DictionaryCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.embed.LorittaColor
import org.jsoup.Jsoup
import java.net.URLEncoder

class DictionaryExecutor(val emotes: Emotes, val http: HttpClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(DictionaryExecutor::class) {
        object Options : CommandOptions() {
            val language = string("language", LocaleKeyData("${DictionaryCommand.LOCALE_PREFIX}.options.language"))
                .also {
                    it.choice("pt-br", LocaleKeyData("${DictionaryCommand.LOCALE_PREFIX}.languages.ptBr"))
                }
                .register()

            val word = string("word", LocaleKeyData("${DictionaryCommand.LOCALE_PREFIX}.options.text"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        // TODO: More languages
        val language = args[options.language]
        val wordToBeSearched = args[options.word]

        val httpResponse = http.get<HttpResponse>("https://www.dicio.com.br/pesquisa.php?q=${URLEncoder.encode(wordToBeSearched, "UTF-8")}")

        if (httpResponse.status == HttpStatusCode.NotFound)
            context.fail(context.locale["${DictionaryCommand.LOCALE_PREFIX}.wordNotFound"], emotes.error)

        val response = httpResponse.readText()

        var jsoup = Jsoup.parse(response)

        // Ao usar pesquisa.php, ele pode retornar uma página de pesquisa ou uma página com a palavra, por isto iremos primeiro descobrir se estamos na página de pesquisa
        val resultadosClass = jsoup.getElementsByClass("resultados")
        val resultados = resultadosClass.firstOrNull()

        if (resultados != null) {
            val resultadosLi = resultados.getElementsByTag("li").firstOrNull()
                ?: context.fail(context.locale["${DictionaryCommand.LOCALE_PREFIX}.wordNotFound"], emotes.error)

            val linkElement = resultadosLi.getElementsByClass("_sugg").first()
            val link = linkElement.attr("href")

            val httpRequest2 = http.get<HttpResponse>("https://www.dicio.com.br$link")

            // This should *never* happen because we are getting it directly from the search results, but...
            if (httpRequest2.status == HttpStatusCode.NotFound)
                context.fail(context.locale["${DictionaryCommand.LOCALE_PREFIX}.wordNotFound"], emotes.error)

            val response2 = httpRequest2.readText()

            jsoup = Jsoup.parse(response2)
        }

        // Se a página não possui uma descrição ou se ela possui uma descrição mas começa com "Ainda não temos o significado de", então é uma palavra inexistente!
        if (jsoup.select("p[itemprop = description]").isEmpty() || jsoup.select("p[itemprop = description]")[0].text().startsWith("Ainda não temos o significado de"))
            context.fail(context.locale["${DictionaryCommand.LOCALE_PREFIX}.wordNotFound"], emotes.error)

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
                body {
                    color = LorittaColor(25, 89, 132)

                    // TODO: Localization
                    title = "📙 ${context.locale["${DictionaryCommand.LOCALE_PREFIX}.meaningOf", word.text()]}"

                    this.description = buildString {
                        append("*${type.text()}*")

                        if (what != null)
                            append("\n\n**${what.text()}**")
                    }

                    // The first in the page will always be "synonyms"
                    // While the second in the page will always be "opposites"
                    // TODO: Localization
                    jsoup.getElementsByClass("sinonimos").getOrNull(0)?.let {
                        field("\uD83D\uDE42 ${context.locale["${DictionaryCommand.LOCALE_PREFIX}.synonyms"]}", it.text())
                    }

                    jsoup.getElementsByClass("sinonimos").getOrNull(1)?.let {
                        field("\uD83D\uDE41 ${context.locale["${DictionaryCommand.LOCALE_PREFIX}.opposite"]}", it.text())
                    }

                    frase?.let {
                        field("\uD83D\uDD8B ${context.locale["${DictionaryCommand.LOCALE_PREFIX}.sentence"]}", it.text())
                    }
                }

                if (wordImage != null)
                    images {
                        // We need to use "data-src" because the page uses lazy loading
                        this.image = wordImage.attr("data-src")
                    }

                if (etim != null)
                    footer(etim.text()) {}
            }
        }
    }
}