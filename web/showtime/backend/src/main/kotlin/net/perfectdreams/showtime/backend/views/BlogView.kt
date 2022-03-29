package net.perfectdreams.showtime.backend.views

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.unsafe
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.showtime.backend.ShowtimeBackend
import net.perfectdreams.showtime.backend.content.parsedDate
import net.perfectdreams.showtime.backend.utils.imgSrcSetFromResources
import net.perfectdreams.showtime.backend.utils.innerContent

class BlogView(
    showtimeBackend: ShowtimeBackend,
    websiteTheme: WebsiteTheme,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String
) : NavbarView(
    showtimeBackend,
    websiteTheme,
    locale,
    i18nContext,
    path
) {
    override val hasDummyNavbar = true

    override fun getTitle() = "Blog"

    override fun DIV.generateContent() {
        val languageId = showtimeBackend.languageManager.getIdByI18nContext(i18nContext)

        val posts = showtimeBackend.loadSourceContentsFromFolder("blog")
            .sortedByDescending { it.metadata.parsedDate }
            .filterNot { it.metadata.hidden }

        innerContent {
            posts.forEachIndexed { index, it ->
                div(classes = if (index % 2 == 0) "odd-wrapper" else "even-wrapper") {
                    if (index != 0)
                    classes = classes + "wobbly-bg"

                    div(classes = "media") {
                        div(classes = "media-body") {
                            div {
                                val localizedContent = it.getLocalizedVersion(languageId)

                                a(href = "/${locale.path}${it.path}") {
                                    h1 {
                                        +localizedContent.metadata.title
                                    }
                                }

                                /* val time = it.metadata.parsedDate?.toInstant()?.atZone(ZoneId.of("America/Sao_Paulo"))
                                if (time != null) {
                                    span {
                                        +"${time.dayOfMonth.toString().padStart(2, '0')}/${
                                            time.monthValue.toString().padStart(2, '0')
                                        }/${time.year}"
                                    }
                                } */

                                div {
                                    unsafe {
                                        raw(
                                            BlogPostView.parseContent(
                                                showtimeBackend,
                                                locale,
                                                localizedContent.content
                                                    .substringBefore("{{ read_more }}")
                                            )
                                        )
                                    }

                                    if (localizedContent.content.contains("{{ read_more }}")) {
                                        div(classes = "read-more") {
                                            imgSrcSetFromResources(
                                                "/v3/assets/img/emotes/lori-zap.png",
                                                "1.5em"
                                            ) {
                                                classes = setOf("inline-emoji")
                                            }


                                            a(href = "/${locale.path}${it.path}") {
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
}