package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onInputFunction
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.parse
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.GuildConfig
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSpanElement
import kotlin.browser.document
import kotlin.browser.window

class GeneralDashboardRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildId}/dashboard") {
    override val keepLoadingScreen: Boolean
        get() = true

    fun DIV.createMessage(senderName: String, senderAvatar: String, senderMessage: DIV.() -> (Unit), loriResponse: DIV.() -> (Unit)) {
        div(classes = "discord-chat-box") {
            style = "padding: 12px; border-radius: 7px; border: 1px solid #dcddde;"

            div(classes = "content") {
                div {
                    img(classes = "user-avatar", src = senderAvatar) {}
                }

                div(classes = "right-side") {
                    div(classes = "user-name") {
                        + senderName
                    }

                    div {
                        style = "white-space: normal; max-width: 20em;"
                        senderMessage()
                    }
                }
            }

            hr {}

            div(classes = "content") {
                div {
                    img(classes = "user-avatar", src = "https://cdn.discordapp.com/avatars/297153970613387264/eb14362006ecdd6d5030a463e01935d3.png?size=2048") {}
                }

                div(classes = "right-side") {
                    div(classes = "user-name") {
                        + "Loritta Morenitta 😘 "
                        span(classes = "bot-tag") {
                            + "Bot"
                        }
                    }

                    div {
                        loriResponse()
                    }
                }
            }
        }
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    override fun onRender(call: ApplicationCall) {
        m.showLoadingScreen()

        SpicyMorenitta.INSTANCE.launch {
            val result = http.get<String> {
                url("${window.location.origin}/api/v1/guilds/${call.parameters["guildId"]}/config")
            }

            val guildConfig = kotlinx.serialization.json.JSON.nonstrict.parse<GuildConfig>(result)

            fixDummyNavbarHeight(call)
            switchContent(call)

            twoColumnLayout(
                    leftSidebar = {
                        leftSidebarForGuildDashboard()
                    },
                    rightSidebar = {
                        rightSidebar(call, guildConfig)
                    }
            )

            SpicyMorenitta.INSTANCE.setUpLinkPreloader()

            m.hideLoadingScreen()
        }
    }

    fun DIV.rightSidebar(call: ApplicationCall, guildConfig: GuildConfig) {
        val generalConfig = guildConfig.general!!

        div(classes = "lori-section") {
            div(classes = "left") {
                img(src = "https://cdn.discordapp.com/avatars/297153970613387264/eb14362006ecdd6d5030a463e01935d3.png") {
                    style = "border-radius: 100%; align-self: baseline;"
                }
            }
            div(classes = "right as-column") {
                div(classes = "section-title") {
                    + "Prefixo"
                }
                div {
                    + "Prefixo é o texto que vem antes de um comando. Eu venho como padrão com o caractere +, mas você pode alterá-lo nesta opção."
                }
                input(InputType.text, classes = "command-prefix") {
                    value = generalConfig.commandPrefix

                    onInputFunction = {
                        val cmdPrefixInputElement = document.select<HTMLInputElement>(".lori-section .right .command-prefix")
                        val prefixElement = document.select<HTMLSpanElement>(".lori-section .right .prefix")
                        prefixElement.innerText = cmdPrefixInputElement.value
                    }
                }

                button {
                    + "Reset"

                    onClickFunction = {
                        hideUnsavedAlert()
                    }
                }

                createMessage(
                        SpicyMorenitta.INSTANCE.userIdentification!!.username,
                        "https://cdn.discordapp.com/emojis/523176710439567392.png?v=1",
                        {

                            span(classes = "prefix") {
                                + generalConfig.commandPrefix
                            }
                            + "ping"
                        },
                        {
                            + "Pong!"
                        }
                )
            }
        }

        hr {}

        createToggle(
                "Mencionar quem executou o comando.",
                "Caso o comando não tenha argumentos obrigatórios, você pode usar \uD83E\uDD37 como primeiro argumento para obter a ajuda do comando.",
                "commandStuff",
                true
        )

        hr {}

        createToggle(
                "Explicar comandos ao executar eles sem usar nenhum argumento.",
                "Caso o comando não tenha argumentos obrigatórios, você pode usar \uD83E\uDD37 como primeiro argumento para obter a ajuda do comando.",
                "commandStuff",
                true
        )

        div {
            + generalConfig.commandPrefix
        }

        button {
            id = "button-wow"
            +"Test Update Guild"

            onClickFunction = {
                val testPrefix = kotlin.browser.window.prompt("Prefix", "..")

                val obj = object {}.asDynamic()
                obj.commandPrefix = testPrefix

                net.perfectdreams.spicymorenitta.utils.WebsiteUtils.patchGuildConfigById(
                        call.parameters.getValue("guildId")!!,
                        10000,
                        obj
                )
            }
        }
    }
}