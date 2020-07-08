package net.perfectdreams.loritta.sweetmorenitta.views

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.html.*
import net.perfectdreams.loritta.sweetmorenitta.AssetHashProvider
import net.perfectdreams.loritta.sweetmorenitta.utils.WebRenderSettings
import net.perfectdreams.loritta.sweetmorenitta.utils.imgSrcSet

class SupportView(settings: WebRenderSettings, locale: BaseLocale) : NavbarView(settings, locale) {
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
                            h2 {
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

                        h1 {
                            + locale["website.support.supportServers"]
                        }

                        div {
                            style = "display: flex; justify-content: space-evenly; flex-wrap: wrap;"

                            div {
                                h2 {
                                    + "English / International"
                                }

                                a(href = "https://discord.gg/ZWt5mKB") {
                                    img(src = "https://discordapp.com/api/guilds/420626099257475072/widget.png?style=banner3") {
                                        style = "border-radius: 7px;"
                                    }
                                }
                            }

                            div {
                                h2 {
                                    + "Português"
                                }

                                a(href = "https://discord.gg/loritta") {
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