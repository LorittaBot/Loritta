package net.perfectdreams.loritta.website.backend.routes

import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.html.*
import kotlinx.html.div
import kotlinx.html.stream.createHTML
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.perfectdreams.dokyo.RoutePath
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.utils.extras.ExtrasUtils
import net.perfectdreams.loritta.website.backend.utils.extras.TooltipsConfig
import net.perfectdreams.loritta.website.backend.utils.userTheme
import net.perfectdreams.loritta.website.backend.views.ExtrasView
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class ExtrasRoute(val showtime: LorittaWebsiteBackend) : LocalizedRoute(showtime, RoutePath.EXTRAS) {
    override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
        try {
            val authors = ExtrasUtils.loadAuthors(showtime)
            val categories = ExtrasUtils.loadWikiEntries(showtime, locale)

            // The result will be something like
            // hello/hello.md
            val parameters = call.parameters.getAll("renderPage")
                ?.joinToString("/")
                ?.ifBlank { "" } // Useful because the index page is stored as "index.md" but the path is just "", so we replace with a empty string

            val render = categories.flatMap { it.entries }.firstOrNull { it.path == parameters } ?: return

            if (render is ExtrasUtils.MarkdownExtrasEntry) {
                var contentToBeTransformedToMarkdown = LorittaWebsiteBackend::class.java.getResourceAsStream("/extras/${render.file}")!!
                    .readAllBytes()
                    .toString(Charsets.UTF_8)
                    .substringAfter("---")

                println(contentToBeTransformedToMarkdown)

                // TODO: Readd ad here (to the contentToBeTransformedToMarkdown)

                // Transform Content if needed
                val renderDiscordMessageLines =
                    contentToBeTransformedToMarkdown.lines().filter { it.startsWith("{{ renderDiscordMessage(\"") }

                for (renderDiscordMessageLine in renderDiscordMessageLines) {
                    val arguments = renderDiscordMessageLine.substringAfter("(")
                        .substringBefore(")")
                        .split(", ")
                        .map { it.removePrefix("\"").removeSuffix("\"") }
                    println(arguments)
                    renderDiscordMessageLine.split(", ")

                    // Load HTML
                    val messageInput = Jsoup.parse(
                        LorittaWebsiteBackend::class.java.getResourceAsStream("/extras/messages/${arguments[0]}")!!
                            .readAllBytes()
                            .toString(Charsets.UTF_8)
                    ).body()

                    // Remove Button to Supress Embeds
                    messageInput.getElementsByClass("embedSuppressButton-1FonMn")
                        .forEach { it.remove() }

                    // Remove Button Container (Reactions)
                    messageInput.getElementsByClass("buttonContainer-DHceWr")
                        .forEach { it.remove() }

                    // Fix svg emoticons
                    messageInput.getElementsByTag("img")
                        .filter { it.attr("src")?.startsWith("/assets/") == true }
                        .forEach { it.attr("src", "https://discord.com" + it.attr("src")) }

                    arguments.drop(2).forEach {
                        println("VarArg: $it")
                        when (it) {
                            "remove-reply" -> {
                                println("Removing reply")
                                messageInput.getElementsByClass("repliedMessage-VokQwo")
                                    .forEach { it.remove() }
                            }
                            "remove-message-content" -> {
                                println("Removing msg content")
                                messageInput.getElementsByClass("messageContent-2qWWxC")
                                    .forEach { it.remove() }
                            }
                            "remove-mention-background" -> {
                                messageInput.getElementsByClass("mentioned-xhSam7")
                                    .forEach { it.removeClass("mentioned-xhSam7") }
                            }
                        }
                    }

                    // Load tooltip file
                    println("Loading tooltip file ${arguments[1]}")
                    val tooltipsConfig = Hocon.decodeFromConfig<TooltipsConfig>(
                        ConfigFactory.parseResources(
                            LorittaWebsiteBackend::class.java,
                            "/extras/messages/${arguments[1]}"
                        )
                            .resolve()
                    )

                    for (tooltip in tooltipsConfig.tooltips) {
                        println("finding ${tooltip.match}")
                        val matchedElementForTooltip = messageInput.selectFirst(tooltip.match)!!

                        // Now we are going to add the tooltip stuff!
                        matchedElementForTooltip.addClass("tooltip")
                            .addClass("tooltip-glow")

                        matchedElementForTooltip.insertChildren(
                            0,
                            Element("div")
                                .addClass("tooltip-text")
                                .html(tooltip.content)
                        )
                    }

                    // Wrap in a message preview div
                    val element = Element("div")
                        .addClass("message-preview")
                        .addClass("theme-light")

                    messageInput.children().forEach {
                        element.appendChild(it)
                    }

                    println(element)

                    val document = Document("/")
                    document.appendChild(element)
                    document.outputSettings().prettyPrint(false)
                    // println(document)

                    // Replace content
                    contentToBeTransformedToMarkdown = contentToBeTransformedToMarkdown.replace(
                        renderDiscordMessageLine,
                        document.outerHtml()
                    )
                }

                val contentInMarkdown =
                    showtime.renderer.render(showtime.parser.parse(contentToBeTransformedToMarkdown))

                call.respondHtml(
                    block = ExtrasView(
                        showtime,
                        call.request.userTheme,
                        locale,
                        i18nContext,
                        "/extras",
                        ExtrasUtils.RenderEntry(
                            contentInMarkdown,
                            render
                        ),
                        authors,
                        categories
                    ).generateHtml()
                )
            } else if (render is ExtrasUtils.DynamicExtrasEntry) {
                val content = createHTML().div { render.generator.generateContent(this) }

                call.respondHtml(
                    block = ExtrasView(
                        showtime,
                        call.request.userTheme,
                        locale,
                        i18nContext,
                        "/extras",
                        ExtrasUtils.RenderEntry(
                            content,
                            render
                        ),
                        authors,
                        categories
                    ).generateHtml()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}