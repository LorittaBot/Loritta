package net.perfectdreams.loritta.website.backend.views

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.toJavaInstant
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.unsafe
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.content.ContentBase
import net.perfectdreams.loritta.website.backend.content.MultilanguageContent
import net.perfectdreams.loritta.website.backend.utils.imgSrcSetFromEtherealGambi
import net.perfectdreams.loritta.website.backend.utils.innerContent
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class BlogView(
    LorittaWebsiteBackend: LorittaWebsiteBackend,
    websiteTheme: WebsiteTheme,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String,
    val posts: List<Post>
) : NavbarView(
    LorittaWebsiteBackend,
    websiteTheme,
    locale,
    i18nContext,
    path
) {
    override val hasDummyNavbar = true

    override fun getTitle() = "Blog"

    override fun DIV.generateContent() {
        innerContent {
            posts.forEachIndexed { index, it ->
                val multilanguageContent = it.multilanguageContent
                val localizedContent = it.localizedContent

                div(classes = if (index % 2 == 0) "odd-wrapper" else "even-wrapper") {
                    if (index != 0)
                        classes = classes + "wobbly-bg"

                    div(classes = "post-wrapper") {
                        div(classes = "post-content") {
                            div {
                                div(classes = "post-header") {
                                    a(href = localizedContent.path) {
                                        h1 {
                                            +localizedContent.metadata.title
                                        }
                                    }

                                    val time = multilanguageContent.metadata.date.toJavaInstant().atZone(ZoneId.of("America/Sao_Paulo"))
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

                                div {
                                    unsafe {
                                        raw(it.parsedContent.substringBefore("{{ read_more }}"))
                                    }

                                    if (localizedContent.content.contains("{{ read_more }}")) {
                                        div(classes = "read-more") {
                                            imgSrcSetFromEtherealGambi(
                                                LorittaWebsiteBackend,
                                                // TODO: Fix this SUPER HACK:tm: - (However do we really care about this? While it does suspend, it should be 99,99% of the times in memory, right)
                                                runBlocking { LorittaWebsiteBackend.getOrRetrieveImageInfo("loritta/emotes/lori-zap")!! },
                                                "png",
                                                "1.5em"
                                            ) {
                                                classes = setOf("inline-emoji")
                                            }

                                            a(href = localizedContent.path) {
                                                +" ${i18nContext.get(I18nKeysData.Website.Blog.KeepReading)} »"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    data class Post(
        val multilanguageContent: MultilanguageContent,
        val localizedContent: ContentBase,
        val parsedContent: String
    )
}