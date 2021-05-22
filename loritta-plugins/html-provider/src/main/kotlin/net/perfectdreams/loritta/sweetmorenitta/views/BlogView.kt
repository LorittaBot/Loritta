package net.perfectdreams.loritta.sweetmorenitta.views

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.p
import kotlinx.html.style
import net.perfectdreams.loritta.website.blog.Post

class BlogView(
    locale: BaseLocale,
    path: String,
    val posts: List<Post>
) : NavbarView(
        locale,
        path
) {
    override fun getTitle() = "Blog"

    override fun DIV.generateContent() {
        div(classes = "odd-wrapper") {
            style = "text-align: center;"

            div(classes = "media single-column") {
                div(classes = "media-body") {
                    for (post in posts) {
                        p {
                            a(href = "/${locale["website.localePath"]}/blog/${post.slug}") {
                                + post.title
                            }
                        }
                    }
                }
            }
        }
    }
}