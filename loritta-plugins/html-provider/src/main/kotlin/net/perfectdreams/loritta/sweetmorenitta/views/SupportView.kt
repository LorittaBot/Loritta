package net.perfectdreams.loritta.sweetmorenitta.views

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.*
import net.perfectdreams.loritta.sweetmorenitta.utils.imgSrcSet

class SupportView(
    locale: BaseLocale,
    path: String
) : NavbarView(
        locale,
        path
) {
    override fun getTitle() = locale["website.support.title"]

    override fun DIV.generateContent() {
        div(classes = "even-wrapper") {
            div(classes = "media") {
                div(classes = "media-figure") {
                    imgSrcSet(
                            "${versionPrefix}/assets/img/support/",
                            "lori_support.png",
                            "(max-width: 800px) 50vw, 15vw",
                            1168,
                            168,
                            100
                    )
                }

                div(classes = "media-body") {
                    div {
                        style = "text-align: left;"

                        div {
                            style = "text-align: center;"
                            h1 {
                                + locale["website.support.title"]
                            }
                        }

                        for (str in locale.getList("website.support.description")) {
                            p {
                                + str
                            }
                        }
                    }
                }
            }
        }
        div(classes = "odd-wrapper wobbly-bg") {
            div(classes = "media") {
                div(classes = "media-body") {
                    div {
                        style = "text-align: center;"

                        div {
                            style = "display: flex; justify-content: space-evenly; flex-wrap: wrap;"

                            div {
                                style = "min-width: 300px; width: 50%;"

                                h2 {
                                    + locale["website.support.supportServer.title"]
                                }

                                locale.getList("website.support.supportServer.description").forEach {
                                    p {
                                        + it
                                    }
                                }

                                a(href = "https://discord.gg/loritta") {
                                    img(src = "https://discordapp.com/api/guilds/420626099257475072/widget.png?style=banner3") {
                                        style = "border-radius: 7px;"
                                    }
                                }
                            }

                            div {
                                style = "min-width: 300px; width: 50%;"

                                h2 {
                                    + locale["website.support.communityServer.title"]
                                }

                                locale.getList("website.support.communityServer.description").forEach {
                                    p {
                                        + it
                                    }
                                }

                                a(href = "https://discord.gg/lori") {
                                    img(src = "https://discordapp.com/api/guilds/297732013006389252/widget.png?style=banner3") {
                                        style = "border-radius: 7px;"
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