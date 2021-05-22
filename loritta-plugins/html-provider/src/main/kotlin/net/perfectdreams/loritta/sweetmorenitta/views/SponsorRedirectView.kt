package net.perfectdreams.loritta.sweetmorenitta.views

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.meta
import kotlinx.html.style
import kotlinx.html.unsafe
import net.perfectdreams.loritta.utils.Sponsor

class SponsorRedirectView(locale: BaseLocale, path: String, val sponsor: Sponsor) : BaseView(locale, path) {
    override fun getTitle() = "Patrocinadores"

    override fun HTML.generateBody() {
        body {
            div {
                id = "content"
                style = "text-align: center;"

                style {
                    unsafe {
                        raw(
                            """
body { background-color: #29a6fe; }
                    
.redirect-center {
  position: absolute;
  left: 50%;
  top: 50%;
  -webkit-transform: translate(-50%, -50%);
  transform: translate(-50%, -50%);
  color: white;
  font-size: 2em;
}"""
                        )
                    }
                }

                div(classes = "redirect-center") {
                    img(src = "https://loritta.website/assets/img/fanarts/Loritta_9_-_Allouette.png") {
                        width = "384"
                        height = "384"
                    }
                    div {
                        +"Redirecionando para o nosso patrocinador, ${sponsor.name}!"
                    }
                }

                meta {
                    attributes["http-equiv"] = "refresh"
                    attributes["content"] = "3; ${sponsor.link}"
                }
            }
        }
    }
}