package net.perfectdreams.loritta.website.views

import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import net.perfectdreams.loritta.utils.locale.BaseLocale
import org.w3c.dom.Document
import org.w3c.dom.Element

abstract class HasNavbarView(document: Document) : BaseView(document) {
    fun generateNavbar(locale: BaseLocale): Element {
        return document.create.div(classes = "navigation-bar") {
            id = "navigation-bar"

            val base = "/${locale.path}"

            div(classes = "left-side-entries") {
                a(classes = "home-page", href = "$base/") {
                    style = "font-family: 'Pacifico', cursive; text-transform: none;"

                    attributes["data-enable-link-preload"] = "true"


                    +"Loritta"
                }

                a(classes = "support", href = "$base/support") {
                    i(classes = "fab fa-discord") {}

                    attributes["data-enable-link-preload"] = "true"

                    +" Suporte"
                }

                a(classes = "fan-arts", href = "$base/fanarts") {
                    i(classes = "fas fa-paint-brush") {}

                    attributes["data-enable-link-preload"] = "true"

                    +" Fan Arts"
                }

                a(classes = "donate", href = "$base/donate") {
                    i(classes = "fas fa-gift") {}

                    attributes["data-enable-link-preload"] = "true"

                    +" Doar"
                }

                a(classes = "blog", href = "$base/blog") {
                    i(classes = "fas fa-newspaper") {}

                    attributes["data-enable-link-preload"] = "true"

                    +" Blog"
                }
            }

            div(classes = "right-side-entries") {
                a {
                    id = "login-button"
                    i(classes = "fas fa-sign-in-alt") {}

                    + " Login"
                }
            }
        }
    }

    override fun generateBody(locale: BaseLocale): Element {
        val body = document.create.body {}

        body.appendChild(generateNavbar(locale))
        body.append.div { id = "dummy-navbar" }

        val content = document.create.div { id = "content" }
        content.appendChild(generateContent())
        body.appendChild(content)

        return body
    }

    abstract fun generateContent(): Element
}