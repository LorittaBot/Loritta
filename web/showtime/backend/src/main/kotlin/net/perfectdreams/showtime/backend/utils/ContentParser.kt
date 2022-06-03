package net.perfectdreams.showtime.backend.utils

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.i
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.showtime.backend.ShowtimeBackend
import net.perfectdreams.showtime.backend.utils.DiscordInviteWrapper.lorittaCommunityServerInvite
import org.jsoup.Jsoup
import java.io.File

object ContentParser {
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
                        + "Mais uma sexta-feira, mais um..."
                    }
                    div(classes = "has-rainbow-text") {
                        style = "font-size: 1.7em; font-weight: bold;"
                        +"Rolêzinho com a Loritta!"
                    }
                    div {
                        i {
                            +"O rolêzinho para saber sobre tudo e mais um pouco que aconteceu nessa semana na LorittaLand!"
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
                    lorittaCommunityServerInvite(i18nContext)
                }
            )
            .replace(
                "{{ lori_community_invite }}",
                createHTML().span {
                    lorittaCommunityServerInvite(i18nContext)
                }
            )
            .replace("{{ read_more }}", "")

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

        val wikilinksRegex = Regex("\\[\\[([A-z0-9/-]+)]]")

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

        return document.toString()
    }
}