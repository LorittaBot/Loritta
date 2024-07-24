package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import dev.kord.common.Color
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.commands.Command
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import org.jsoup.Jsoup
import java.net.URLEncoder

class DictionaryCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Dictionary
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.UTILS) {
        enableLegacyMessageSupport = true

        alternativeLegacyLabels.apply {
            add("dicio")
            add("dicionário")
            add("dicionario")
            add("definir")
        }

        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        executor = DictionaryExecutor()
    }

    inner class DictionaryExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val language = string("language", I18N_PREFIX.Options.Language) {
                choice(I18N_PREFIX.Languages.PtBr, "pt-br")
            }

            val word = string("word", I18N_PREFIX.Options.Word)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            // TODO: More languages
            val language = args[options.language]
            val wordToBeSearched = args[options.word]

            val httpResponse = loritta.http.get(
                "https://www.dicio.com.br/pesquisa.php?q=${
                    URLEncoder.encode(
                        wordToBeSearched,
                        "UTF-8"
                    )
                }"
            )

            if (httpResponse.status == HttpStatusCode.NotFound)
                context.fail(true) {
                    styled(
                        context.i18nContext.get(DictionaryCommand.I18N_PREFIX.WordNotFound),
                        Emotes.Error
                    )
                }

            val response = httpResponse.bodyAsText()

            var jsoup = Jsoup.parse(response)

            // Ao usar pesquisa.php, ele pode retornar uma página de pesquisa ou uma página com a palavra, por isto iremos primeiro descobrir se estamos na página de pesquisa
            val resultadosClass = jsoup.getElementsByClass("resultados")
            val resultados = resultadosClass.firstOrNull()

            if (resultados != null) {
                val resultadosLi = resultados.getElementsByTag("li").firstOrNull()
                    ?: context.fail(true) {
                        styled(
                            context.i18nContext.get(DictionaryCommand.I18N_PREFIX.WordNotFound),
                            Emotes.Error
                        )
                    }

                val linkElement = resultadosLi.getElementsByClass("_sugg").first()!!
                val link = linkElement.attr("href")

                val httpRequest2 = loritta.http.get("https://www.dicio.com.br$link")

                // This should *never* happen because we are getting it directly from the search results, but...
                if (httpRequest2.status == HttpStatusCode.NotFound)
                    context.fail(true) {
                        styled(
                            context.i18nContext.get(DictionaryCommand.I18N_PREFIX.WordNotFound),
                            Emotes.Error
                        )
                    }

                val response2 = httpRequest2.bodyAsText()

                jsoup = Jsoup.parse(response2)
            }

            // Se a página não possui uma descrição ou se ela possui uma descrição mas começa com "Ainda não temos o significado de", então é uma palavra inexistente!
            val descriptionQuery = jsoup.select("p.significado")
            if (descriptionQuery.isEmpty())
                context.fail(true) {
                    styled(
                        context.i18nContext.get(DictionaryCommand.I18N_PREFIX.WordNotFound),
                        Emotes.Error
                    )
                }

            val description = descriptionQuery[0]
            val type = description.getElementsByTag("span")[0]
            val word = jsoup.select("h1").first()!!
            val what = description.getElementsByTag("span").getOrNull(1)
            val etim = description.getElementsByClass("etim").firstOrNull()

            // *Technically* the image is always
            // https://s.dicio.com.br/word_here.jpg
            // Example: https://s.dicio.com.br/bunda.jpg
            // However we wouldn't be sure if the image really exists, so let's check in the page
            val wordImage = jsoup.getElementsByClass("imagem-palavra").firstOrNull()?.attr("src")?.ifBlank { null }
            val frase = if (jsoup.getElementsByClass("frase").isNotEmpty()) {
                jsoup.getElementsByClass("frase")[0]
            } else {
                null
            }

            context.reply(true) {
                embed {
                    color = Color(25, 89, 132).rgb  // TODO: Move this to a object

                    title = "${Emotes.BlueBook} ${context.i18nContext.get(DictionaryCommand.I18N_PREFIX.MeaningOf(word.text()))}"

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
                        image = wordImage

                    if (etim != null)
                        footer(etim.text())
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (context.args.isEmpty()) {
                context.explain()
                return null
            }

            return mapOf(
                options.language to "pt-br",
                options.word to args.joinToString(separator = " ")
            )
        }
    }
}