package net.perfectdreams.loritta.sweetmorenitta.views

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.img
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.style
import kotlinx.html.ul
import net.perfectdreams.loritta.sweetmorenitta.utils.generateSponsorNoWrap

class SponsorsView(locale: BaseLocale, path: String) : NavbarView(locale, path) {
    override fun getTitle() = "Patrocinadores"

    override fun DIV.generateContent() {
        div(classes = "even-wrapper") {
            div(classes = "media") {
                div(classes = "media-body") {
                    div {
                        style = "text-align: center;"

                        h1 {
                            + "Patrocinadores"
                        }

                        com.mrpowergamerbr.loritta.LorittaLauncher.loritta.sponsors.forEach {
                            generateSponsorNoWrap(it)
                        }
                    }
                }
            }
        }

        div(classes = "odd-wrapper wobbly-bg") {
            div(classes = "media") {
                div(classes = "media-figure") {
                    img(src = "https://loritta.website/assets/img/fanarts/Loritta_-_Heathecliff.png") {}
                }
                div(classes = "media-body") {
                    div {
                        style = "text-align: left;"

                        div {
                            style = "text-align: center;"
                            h2 {
                                + "Para que servem os patrocinadores?"
                            }
                        }


                        p {
                            + "Patrocinadores são pessoas incríveis que querem divulgar seus servidores e projetos na Loritta, divulgando para mais de cinco mil pessoas diferentes todos os dias!"
                        }

                        h3 {
                            + "A cada mês, donos de servidores podem colocar os seus servidores..."
                        }

                        ul {
                            li {
                                + "Na \"Quarta Patrocinada\" no Servidor de Suporte da Loritta!"
                            }
                            li {
                                + "Na home page e na página de daily da Loritta"
                            }
                            li {
                                + "No status de \"Jogando\" da Loritta"
                            }
                            li {
                                + "E aqui, na página de patrocinadores da Loritta!"
                            }
                        }

                        h3 {
                            + "Se interessou?"
                        }
                        p {
                            + "Então veja o canal de slots premiums no servidor de suporte da Loritta para saber mais sobre os requisitos, formas de pagamento, como funciona e muito mais!"
                        }
                    }
                }
            }
        }
    }
}