package net.perfectdreams.loritta.cinnamon.showtime.backend.utils

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.coroutines.runBlocking
import kotlinx.html.TagConsumer
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.i
import kotlinx.html.li
import kotlinx.html.span
import kotlinx.html.stream.appendHTML
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.ul
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.showtime.backend.ShowtimeBackend
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.DiscordInviteWrapper.lorittaCommunityServerInvite
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.DiscordInviteWrapper.sparklyPowerServerInvite
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File

object ContentParser {
    private const val pathRegex = "[A-z0-9/-]+"
    private val wikilinksRegex = Regex("\\[\\[($pathRegex)]]")
    private val titlelinksRegex = Regex("<<($pathRegex)>>")
    private val readMorePostRegex = Regex("\\{\\{ read_more_post\\(\"($pathRegex)\"\\) }}")

    suspend fun parseContent(
        showtimeBackend: ShowtimeBackend,
        locale: BaseLocale,
        i18nContext: I18nContext,
        content: String
    ): String {
        var markdown = content
            .replace("{{ locale_path }}", locale.path)
            .replace("{{ ethereal_gambi_url }}", showtimeBackend.etherealGambiClient.baseUrl)
            .replace(
                "{{ loritta_friday }}",
                createHTML().div {
                    style = "text-align: center;"

                    div {
                        style = "font-size: 1.3em;"
                        + "As novidades da sua Morenitta favorita, tudo em um..."
                    }
                    div(classes = "has-rainbow-text") {
                        style = "font-size: 1.7em; font-weight: bold;"
                        +"Rolêzinho com a Loritta!"
                    }
                    div {
                        i {
                            +"O rolêzinho para saber sobre tudo e mais um pouco sobre as novidades da LorittaLand!"
                        }
                    }
                }
            )
            .replace("{{ in_content_ad }}", createHTML().div {
                adWrapper(showtimeBackend.svgIconManager) {
                    // Desktop Large
                    generateNitroPayAd(
                        "blog-post-large",
                        listOf(
                            NitroPayAdSize(
                                728,
                                90
                            ),
                            NitroPayAdSize(
                                970,
                                90
                            ),
                            NitroPayAdSize(
                                970,
                                250
                            )
                        ),
                        mediaQuery = NitroPayAdGenerator.DESKTOP_LARGE_AD_MEDIA_QUERY
                    )

                    generateNitroPayAd(
                        "blog-post-desktop",
                        listOf(
                            NitroPayAdSize(
                                728,
                                90
                            )
                        ),
                        mediaQuery = NitroPayAdGenerator.RIGHT_SIDEBAR_DESKTOP_MEDIA_QUERY
                    )

                    // We don't do tablet here because there isn't any sizes that would fit a tablet comfortably
                    generateNitroPayAd(
                        "blog-post-phone",
                        listOf(
                            NitroPayAdSize(
                                300,
                                250
                            ),
                            NitroPayAdSize(
                                320,
                                50
                            )
                        ),
                        mediaQuery = NitroPayAdGenerator.RIGHT_SIDEBAR_PHONE_MEDIA_QUERY
                    )
                }
            })
            .replace(
                "{{ lori_support_invite }}",
                createHTML().span {
                    lorittaCommunityServerInvite(showtimeBackend, i18nContext)
                }
            )
            .replace(
                "{{ lori_community_invite }}",
                createHTML().span {
                    lorittaCommunityServerInvite(showtimeBackend, i18nContext)
                }
            )
            .replace(
                "{{ sparklypower_invite }}",
                createHTML().span {
                    sparklyPowerServerInvite(showtimeBackend, i18nContext)
                }
            )
            .replace("{{ read_more }}", "")
            .replace("{{ table_of_contents }}", "<div class=\"table-of-contents\"></div>")

        markdown = parseKeepReadingPost(showtimeBackend, i18nContext, locale, markdown)
        markdown = parseEmotes(showtimeBackend, markdown)
        markdown = parseWikilinks(showtimeBackend, locale, markdown)
        markdown = parseTitlelinks(showtimeBackend, locale, markdown)

        val raw = showtimeBackend.renderer.render(
            showtimeBackend.parser.parse(markdown)
        )

        val document = Jsoup.parse(raw)

        document
            .body()
            .apply {
                getElementsByTag("img-resources")
                    .forEach {
                        it.replaceWith(
                            document.parser().parseFragmentInput(
                                createHTML()
                                    .span {
                                        imgSrcSetFromResources(
                                            it.attr("src"),
                                            it.attr("sizes")
                                        ) {
                                            it.attributes().forEach { attribute ->
                                                if (attribute.key != "src" && attribute.key != "sizes") {
                                                    attributes[attribute.key] = attribute.value
                                                }
                                            }
                                        }
                                    },
                                it,
                                document.baseUri()
                            )[0]
                        )
                    }
            }

        document
            .body()
            .apply {
                getElementsByTag("img-ethereal")
                    .forEach {
                        val src = it.attr("src")
                            .removePrefix("/")
                        val pathWithoutExtension = src.substringBeforeLast(".")
                        val extension = src.substringAfterLast(".")
                        val variants = showtimeBackend.getOrRetrieveImageInfo(pathWithoutExtension) ?: error("Couldn't find EtherealGambi ImageInfo of $src")

                        it.replaceWith(
                            document.parser().parseFragmentInput(
                                createHTML()
                                    .span {
                                        imgSrcSetFromEtherealGambi(
                                            showtimeBackend,
                                            variants,
                                            extension,
                                            it.attr("sizes")
                                        ) {
                                            it.attributes().forEach { attribute ->
                                                if (attribute.key != "src" && attribute.key != "sizes") {
                                                    attributes[attribute.key] = attribute.value
                                                }
                                            }
                                        }
                                    },
                                it,
                                document.baseUri()
                            )[0]
                        )
                    }
            }

        parseTableOfContents(showtimeBackend, document)

        return document.toString()
    }

    private suspend fun parseEmotes(showtimeBackend: ShowtimeBackend, _markdown: String): String {
        var markdown = _markdown

        val variants = showtimeBackend.getOrRetrieveImageInfos(
            *WebEmotes.emotes.map {
                "loritta/emotes/${it.value.substringBeforeLast(".")}"
            }.toTypedArray()
        )

        for ((emote, emoteFile) in WebEmotes.emotes) {
            // Should never be null! Well, I hope that it won't...
            val variantInfo = variants["loritta/emotes/${emoteFile.substringBeforeLast(".")}"]!!

            markdown = markdown.replace(
                ":$emote:",
                createHTML().span {
                    imgSrcSetFromEtherealGambi(
                        showtimeBackend,
                        variantInfo,
                        emoteFile.substringAfterLast("."),
                        "1.5em"
                    ) {
                        classes = setOf("inline-emoji")
                    }
                }
            )
        }

        return markdown
    }

    private fun parseWikilinks(showtimeBackend: ShowtimeBackend, locale: BaseLocale, _markdown: String): String {
        var markdown = _markdown

        wikilinksRegex.findAll(markdown)
            .forEach {
                val post = showtimeBackend.loadMultilanguageSourceContentsFromFolder(File(ShowtimeBackend.contentFolder, "${it.groupValues[1]}.post"))

                if (post != null) {
                    markdown = markdown.replace(
                        it.groupValues[0],
                        "[${post.getLocalizedVersion("pt").metadata.title}](/${locale.path}${post.path})"
                    )
                }
            }

        return markdown
    }

    private fun parseTitlelinks(showtimeBackend: ShowtimeBackend, locale: BaseLocale, _markdown: String): String {
        var markdown = _markdown

        titlelinksRegex.findAll(markdown)
            .forEach {
                val post = showtimeBackend.loadMultilanguageSourceContentsFromFolder(File(ShowtimeBackend.contentFolder, "${it.groupValues[1]}.post"))

                if (post != null) {
                    markdown = markdown.replace(
                        it.groupValues[0],
                        post.getLocalizedVersion("pt").metadata.title
                    )
                }
            }

        return markdown
    }

    private fun parseKeepReadingPost(showtimeBackend: ShowtimeBackend, i18nContext: I18nContext, locale: BaseLocale, _markdown: String): String {
        var markdown = _markdown

        readMorePostRegex.findAll(markdown)
            .forEach {
                val post = showtimeBackend.loadMultilanguageSourceContentsFromFolder(File(ShowtimeBackend.contentFolder, "${it.groupValues[1]}.post"))

                if (post != null) {
                    markdown = markdown.replace(
                        it.groupValues[0],
                        createHTML().div(classes = "read-more-about-feature") {
                            a(href = post.getLocalizedVersion("pt").path) {
                                + "« "

                                imgSrcSetFromEtherealGambi(
                                    showtimeBackend,
                                    // TODO: Fix this SUPER HACK:tm: - (However do we really care about this? While it does suspend, it should be 99,99% of the times in memory, right)
                                    runBlocking { showtimeBackend.getOrRetrieveImageInfo("loritta/emotes/lori-zap")!! },
                                    "png",
                                    "1em"
                                ) {
                                    classes = setOf("inline-emoji")
                                }

                                +" ${i18nContext.get(I18nKeysData.Website.Blog.KeepReadingFeature)} »"
                            }
                        }
                    )
                }
            }

        return markdown
    }

    private fun parseTableOfContents(showtimeBackend: ShowtimeBackend, document: Document) {
        document
            .body()
            .apply {
                getElementsByClass("table-of-contents")
                    .forEach { tableOfContentsElement ->
                        tableOfContentsElement.appendHTML {
                            div(classes = "toc-title") {
                                + "Atalhos "

                                imgSrcSetFromEtherealGambi(
                                    showtimeBackend,
                                    // TODO: Fix this SUPER HACK:tm: - (However do we really care about this? While it does suspend, it should be 99,99% of the times in memory, right)
                                    runBlocking { showtimeBackend.getOrRetrieveImageInfo("loritta/emotes/lori-zap")!! },
                                    "png",
                                    "2em"
                                ) {
                                    classes = setOf("inline-emoji")
                                }
                            }

                            ul {
                                document.body()
                                    .getElementsByTag("h2")
                                    .forEach {
                                        li {
                                            a(href = "#${it.id()}") {
                                                + it.text()
                                            }
                                        }
                                    }
                            }
                        }
                    }
            }
    }

    private fun Element.appendHTML(block: TagConsumer<StringBuilder>.() -> (Unit)) {
        val stringBuilder = StringBuilder()
        stringBuilder.appendHTML(
            prettyPrint = false,
            xhtmlCompatible = true
        ).apply(block)
        append(stringBuilder.toString())
    }
}