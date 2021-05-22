package net.perfectdreams.spicymorenitta.routes

import io.ktor.client.request.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.ins
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JSON
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.serializable.CommandInfo
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.HTMLDivElement

class CommandsRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/commands") {
    override val keepLoadingScreen: Boolean
        get() = true
    override val requiresUserIdentification = false

    override fun onRender(call: ApplicationCall) {
        super.onRender(call)

        m.launch {
            val result = http.get<String> {
                url("${window.location.origin}/api/v1/loritta/commands/${locale.id}")
            }

            val locale = locale

            val list = JSON.nonstrict.decodeFromString(ListSerializer(CommandInfo.serializer()), result)

            fixDummyNavbarHeight(call)

            val entriesDiv = document.select<HTMLDivElement>("#commands")

            var index = 0

            for (category in CommandCategory.values().filter { it != CommandCategory.MAGIC }) {
                val commands = list.filter { it.category == category }
                if (commands.isNotEmpty()) {
                    entriesDiv.append {
                        var classes = if (index % 2 == 0) "even-wrapper" else "odd-wrapper"
                        if (index != 0)
                            classes += " wobbly-bg"

                        div(classes = classes) {
                            id = "category-${category.name.toLowerCase().replace("_", "-")}"

                            if (index % 2 == 0) {
                                div {
                                    style = "text-align: center;"

                                    ins("adsbygoogle") {
                                        style = "display:block"
                                        attributes["data-ad-client"] = "ca-pub-9989170954243288"
                                        attributes["data-ad-slot"] = "4611100335"
                                        attributes["data-ad-format"] = "auto"
                                    }

                                    script {
                                        +"(adsbygoogle = window.adsbygoogle || []).push({});"
                                    }
                                }
                            }

                            div(classes = "media") {
                                div(classes = "media-figure") {
                                    val imageSource = when (category) {
                                        CommandCategory.SOCIAL -> "https://loritta.website/assets/img/social.png"
                                        CommandCategory.POKEMON -> "https://loritta.website/assets/img/pokemon.png"
                                        CommandCategory.MINECRAFT -> "https://loritta.website/assets/img/loritta_pudim.png"
                                        CommandCategory.FUN -> "https://loritta.website/assets/img/vieirinha.png"
                                        CommandCategory.UTILS -> "https://loritta.website/assets/img/utils.png"
                                        CommandCategory.ANIME -> "https://loritta.website/assets/img/loritta_anime.png"
                                        CommandCategory.ECONOMY -> "https://loritta.website/assets/img/loritta_money_discord.png"
                                        CommandCategory.FORTNITE -> "https://loritta.website/assets/img/loritta_fortnite_icon.png"
                                        else -> "https://loritta.website/assets/img/loritta_gabizinha_v1.png"
                                    }

                                    img(src = imageSource) {
                                        style = "width: 100%; border-radius: 8px;"
                                    }
                                }

                                div(classes = "media-body") {
                                    div {
                                        style = "text-align: center;"
                                        h1 {
                                            + category.getLocalizedName(locale)
                                        }
                                    }
                                    for (entry in category.getLocalizedDescription(locale)) {
                                        p {
                                            +entry
                                        }
                                    }
                                }
                            }

                            div (classes = "media") {
                                div(classes = "media-body") {
                                    style = "overflow: auto;"
                                    
                                    hr {}

                                    table(classes = "fancy-table") {
                                        style = "width: 100%;"

                                        thead {
                                            tr {
                                                th {
                                                    +"Comando"
                                                }
                                                th {
                                                    +"Descrição"
                                                }
                                                th {
                                                    +"Aliases"
                                                }
                                            }
                                        }

                                        for (command in commands.sortedBy(CommandInfo::label)) {
                                            tr {
                                                td {
                                                    + command.label

                                                    val usage = command.usage
                                                    if (usage != null) {
                                                        + " "
                                                        code {
                                                            + usage.build(locale)
                                                        }
                                                    }
                                                }
                                                td {
                                                    val descriptionKey = command.description

                                                    if (descriptionKey != null) {
                                                        + locale.get(descriptionKey)
                                                    } else {
                                                        + "-"
                                                    }
                                                }
                                                td {
                                                    + (command.aliases.joinToString(", "))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    index++
                }
            }

            m.hideLoadingScreen()
        }
    }
}