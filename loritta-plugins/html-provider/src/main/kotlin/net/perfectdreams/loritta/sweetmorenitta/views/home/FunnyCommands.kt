package net.perfectdreams.loritta.sweetmorenitta.views.home

import net.perfectdreams.loritta.common.locale.BaseLocale
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
import net.perfectdreams.loritta.sweetmorenitta.utils.NitroPayAdGenerator
import net.perfectdreams.loritta.sweetmorenitta.utils.adWrapper
import net.perfectdreams.loritta.sweetmorenitta.utils.generateNitroPayAdOrSponsor
import net.perfectdreams.loritta.sweetmorenitta.utils.imgSrcSet
import net.perfectdreams.loritta.sweetmorenitta.views.BaseView

fun DIV.funnyCommands(locale: BaseLocale, websiteUrl: String) {
    div(classes = "odd-wrapper wobbly-bg") {
        id = "fun-section"

        adWrapper {
            generateNitroPayAdOrSponsor(2, "home-funny-commands1", NitroPayAdGenerator.ALL_SIZES)
            generateNitroPayAdOrSponsor(3, "home-funny-commands2", NitroPayAdGenerator.ALL_SIZES_EXCEPT_PHONES)

        }

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
                imgSrcSet(
                        "${BaseView.versionPrefix}/assets/img/home/",
                        "lori_commands.png",
                        "(max-width: 800px) 50vw, 15vw",
                        791,
                        191,
                        100
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

        adWrapper {
            generateNitroPayAdOrSponsor(2, "home-funny-commands1", NitroPayAdGenerator.ALL_SIZES)
            generateNitroPayAdOrSponsor(3, "home-funny-commands2", NitroPayAdGenerator.ALL_SIZES_EXCEPT_PHONES)

        }

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
                imgSrcSet(
                        "${BaseView.versionPrefix}/assets/img/home/",
                        "lori_commands.png",
                        "(max-width: 800px) 50vw, 15vw",
                        791,
                        191,
                        100
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