package net.perfectdreams.showtime.backend.views

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.datetime.toJavaInstant
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.i
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.unsafe
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.showtime.backend.ShowtimeBackend
import net.perfectdreams.showtime.backend.content.ContentBase
import net.perfectdreams.showtime.backend.content.MultilanguageContent
import net.perfectdreams.showtime.backend.utils.DiscordInviteWrapper.lorittaCommunityServerInvite
import net.perfectdreams.showtime.backend.utils.NitroPayAdGenerator
import net.perfectdreams.showtime.backend.utils.NitroPayAdSize
import net.perfectdreams.showtime.backend.utils.WebEmotes
import net.perfectdreams.showtime.backend.utils.adWrapper
import net.perfectdreams.showtime.backend.utils.generateNitroPayAd
import net.perfectdreams.showtime.backend.utils.imgSrcSetFromResources
import net.perfectdreams.showtime.backend.utils.imgSrcSetFromResourcesOrFallbackToImgIfNotPresent
import net.perfectdreams.showtime.backend.utils.innerContent
import org.jsoup.Jsoup
import java.io.File
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class BlogPostView(
    showtimeBackend: ShowtimeBackend,
    websiteTheme: WebsiteTheme,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String,
    val content: MultilanguageContent,
    val localizedContent: ContentBase
) : NavbarView(
    showtimeBackend,
    websiteTheme,
    locale,
    i18nContext,
    path
) {
    companion object {
        fun parseContent(showtimeBackend: ShowtimeBackend, locale: BaseLocale, i18nContext: I18nContext, content: String): String {
            var markdown = content
                .replace("{{ locale_path }}", locale.path)
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

            for ((emote, emoteFile) in WebEmotes.emotes) {
                markdown = markdown.replace(
                    ":$emote:",
                    createHTML().span {
                        imgSrcSetFromResourcesOrFallbackToImgIfNotPresent(
                            "/v3/assets/img/emotes/$emoteFile",
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

            return document.toString()
        }
    }

    override val hasDummyNavbar = true

    override fun getTitle() = localizedContent.metadata.title

    override fun DIV.generateContent() {
        innerContent {
            div(classes = "odd-wrapper") {
                div(classes = "post-wrapper") {
                    div(classes = "post-content") {
                        div {
                            div(classes = "post-header") {
                                a(href = "/${locale.path}${content.path}") {
                                    h1 {
                                        +localizedContent.metadata.title
                                    }
                                }

                                val time = content.metadata.date.toJavaInstant().atZone(ZoneId.of("America/Sao_Paulo"))
                                if (time != null) {
                                    div(classes = "post-info") {
                                        val f: DateTimeFormatter = DateTimeFormatter
                                            .ofLocalizedDate(FormatStyle.FULL)
                                            .withLocale(Locale.forLanguageTag(i18nContext.language.info.formattingLanguageId))
                                        val output = time.toLocalDate().format(f)

                                        +output
                                    }
                                }
                            }

                            unsafe {
                                raw(parseContent(showtimeBackend, locale, i18nContext, localizedContent.content))
                            }

                            hr {}

                            // Put an ad at the end of the blog post
                            adWrapper(iconManager) {
                                // Desktop Large
                                generateNitroPayAd(
                                    "end-of-blog-post-large",
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
                                    "end-of-blog-post-desktop",
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
                                    "end-of-blog-post-phone",
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
                        }
                    }
                }
            }
        }
    }

    override fun getPublicationDate() = content.metadata.date.toJavaInstant()
    override fun getImageUrl() = content.metadata.imageUrl
}