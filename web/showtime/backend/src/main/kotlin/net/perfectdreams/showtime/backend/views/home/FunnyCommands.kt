package net.perfectdreams.showtime.backend.views.home

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.style
import kotlinx.html.ul
import net.perfectdreams.showtime.backend.utils.NitroPayAdGenerator
import net.perfectdreams.showtime.backend.utils.SVGIconManager
import net.perfectdreams.showtime.backend.utils.adWrapper
import net.perfectdreams.showtime.backend.utils.generateNitroPayAd
import net.perfectdreams.showtime.backend.utils.imgSrcSetFromResources
import net.perfectdreams.showtime.backend.views.BaseView

fun DIV.funnyCommands(svgIconManager: SVGIconManager, locale: BaseLocale, websiteUrl: String, sectionClassName: String) {
    div(classes = "$sectionClassName wobbly-bg") {
        id = "fun-section"

        // TODO: Sponsor
        adWrapper(svgIconManager) {
            // generateNitroPayAdOrSponsor(2, "home-funny-commands1", "Loritta v2 Funny Commands") { true }
            // generateNitroPayAdOrSponsor(3, "home-funny-commands2", "Loritta v2 Funny Commands") { it != NitroPayAdDisplay.PHONE }
            generateNitroPayAd("home-funny-commands1", NitroPayAdGenerator.ALL_SIZES)
            generateNitroPayAd("home-funny-commands2", NitroPayAdGenerator.ALL_SIZES)
        }

        div(classes = "media") {
            div(classes = "media-figure") {
                imgSrcSetFromResources(
                        "${BaseView.versionPrefix}/assets/img/home/lori_commands.png",
                        "(max-width: 800px) 50vw, 15vw"
                )
            }
            div(classes = "media-body") {
                div {
                    style = "text-align: left;"

                    div {
                        style = "text-align: center;"
                        h1 {
                            + locale["website.home.funnyCommands.title"]
                        }
                    }

                    for (str in locale.getList("website.home.funnyCommands.description")) {
                        p {
                            + str
                        }
                    }
                }
            }
        }
    }
}


fun DIV.funnyCommandsBrasil(locale: BaseLocale, websiteUrl: String) {
    div(classes = "odd-wrapper wobbly-bg") {
        id = "fun-section"

        // generateNitroPayAdOrSponsor(2, "home-funny-commands1-brazil1", "Loritta v2 Funny Commands") { true }
        // generateNitroPayAdOrSponsor(3, "home-funny-commands2-brazil2", "Loritta v2 Funny Commands") { it != NitroPayAdDisplay.PHONE }

        /* div(classes = "funny-commands") {
        div {
            div(classes = "marquee") {
                div(classes = "scroller") {
                    headerCommands()
                }
            }

            div(classes = "marquee marquee2") {
                div(classes = "scroller") {
                    headerCommands()
                }
            }
        }
        } */

        div(classes = "media") {
            div(classes = "media-figure") {
                imgSrcSetFromResources(
                    "${BaseView.versionPrefix}/assets/img/home/",
                    "(max-width: 800px) 50vw, 15vw"
                )
            }
            div(classes = "media-body") {
                div {
                    style = "text-align: left;"


                    div {
                        style = "text-align: center;"
                        h1 {
                            + "Memes Brasileiros em um Bot Brasileiro"
                        }
                    }

                    p {
                        + "Não tem graça usar bots de entreterimento gringos se você não entende nada dos memes que eles fazem, seus membros não entendem nada e você só solta aquela risadinha de \"eu não entendi mas ok\"."
                    }
                    p {
                        + "Por isto eu possui vários comandos diferentes e engraçados para você se divertir e ter gargalhadas com eles! Faça seus próprios memes comigo, sem você precisar do conforto do seu servidor no Discord!"
                    }
                    ul {
                        li {
                            + "Faça montagens com o Bolsonaro com "
                            code {
                                + "+bolsonaro"
                            }
                        }
                        li {
                            + "Destrua seus piores inimigos no cepo de madeira com o "
                            code {
                                + "+cepo"
                            }
                        }
                        li {
                            + "Imagine como você aparecia no Treta News com o "
                            code {
                                + "+tretanews"
                            }
                        }
                        li {
                            + "Tá pegando fogo bicho! Invoque o Faustão no seu servidor com "
                            code {
                                + "+faustão"
                            }
                        }
                        li {
                            + "É pá vê ou pá cume? Piadas de Tiozão no "
                            code {
                                + "+tiodopave"
                            }
                        }
                        li {
                            + "O SAM é brabo? Coloque a marca da água da South America Memes em seus memes de qualidade duvidosa com "
                            code {
                                + "+sam"
                            }
                            + " e pegue memes também de qualidade duvidosa com "
                            code {
                                + "+randomsam"
                            }
                        }
                        li {
                            + "ata com "
                            code {
                                + "+ata"
                            }
                        }
                        li {
                            + "E muito mais! Veja todos na minha "
                            a(href = "$websiteUrl/commands") {
                                + "lista de comandos"
                            }
                            + "."
                        }
                    }


                    div {
                        style = "text-align: center;"
                        img(src = "$websiteUrl${BaseView.versionPrefix}/assets/img/bolsonaro_tv_add_lori.png") {}
                    }

                }
            }
        }
    }
}