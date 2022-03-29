package net.perfectdreams.showtime.backend.views

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.DIV
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
import net.perfectdreams.showtime.backend.content.parsedDate
import net.perfectdreams.showtime.backend.utils.NitroPayAdGenerator
import net.perfectdreams.showtime.backend.utils.NitroPayAdSize
import net.perfectdreams.showtime.backend.utils.adWrapper
import net.perfectdreams.showtime.backend.utils.generateNitroPayAd
import net.perfectdreams.showtime.backend.utils.imgSrcSetFromResources
import net.perfectdreams.showtime.backend.utils.innerContent
import java.io.File

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
        private val VALID_EMOTES = mapOf(
            "lori_sunglasses" to "lori-sunglasses.png",
            "lori_sob" to "lori-sob.png",
            "lori_kiss" to "lori-kiss.png",
            "lori_hm" to "lori-hm.png",
            "lori_bonk" to "lori-bonk.png",
            "lori_card" to "lori-card.png",
            "lori_what" to "lori-what.png",
            "lori_zap" to "lori-zap.png",
            "lori_nem_ligo" to "lori-nem-ligo.png",
            "lori_rage" to "lori-rage.png",
            "lori_clown" to "lori-clown.png"
        )
    }

    override val hasDummyNavbar = true

    override fun getTitle() = localizedContent.metadata.title

    override fun DIV.generateContent() {
        innerContent {
            div(classes = "odd-wrapper") {
                div(classes = "media") {
                    div(classes = "media-body") {
                        h1 {
                            +localizedContent.metadata.title
                        }

                        var markdown = localizedContent.content
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
                                adWrapper(iconManager) {
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

                        for ((emote, emoteFile) in VALID_EMOTES) {
                            markdown = markdown.replace(
                                ":$emote:",
                                createHTML().span {
                                    imgSrcSetFromResources(
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

                        unsafe {
                            raw(raw)
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

    override fun getPublicationDate() = content.metadata.parsedDate?.toInstant()
    override fun getImageUrl() = content.metadata.imageUrl
}