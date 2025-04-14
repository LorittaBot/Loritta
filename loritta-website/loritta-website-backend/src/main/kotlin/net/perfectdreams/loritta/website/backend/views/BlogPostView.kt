package net.perfectdreams.loritta.website.backend.views

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.datetime.toJavaInstant
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.unsafe
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.content.ContentBase
import net.perfectdreams.loritta.website.backend.content.MultilanguageContent
import net.perfectdreams.loritta.website.backend.utils.NitroPayAdGenerator
import net.perfectdreams.loritta.website.backend.utils.NitroPayAdSize
import net.perfectdreams.loritta.website.backend.utils.adWrapper
import net.perfectdreams.loritta.website.backend.utils.generateNitroPayAd
import net.perfectdreams.loritta.website.backend.utils.innerContent
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class BlogPostView(
    LorittaWebsiteBackend: LorittaWebsiteBackend,
    websiteTheme: WebsiteTheme,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String,
    val content: MultilanguageContent,
    val localizedContent: ContentBase,
    val parsedContent: String
) : NavbarView(
    LorittaWebsiteBackend,
    websiteTheme,
    locale,
    i18nContext,
    path
) {
    override val hasDummyNavbar = true

    override fun getTitle() = localizedContent.metadata.title

    override fun DIV.generateContent() {
        innerContent {
            div(classes = "odd-wrapper") {
                div(classes = "post-wrapper") {
                    div(classes = "post-content") {
                        div {
                            div(classes = "post-header") {
                                a(href = localizedContent.path) {
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
                                raw(parsedContent)
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