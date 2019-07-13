package net.perfectdreams.spicymorenitta.routes

import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.parseList
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.utils.TemmieDiscordGuild
import net.perfectdreams.spicymorenitta.utils.UserIdentification
import net.perfectdreams.spicymorenitta.utils.WebsiteUtils
import kotlin.browser.window

class DashboardRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/dashboard") {
    override val keepLoadingScreen: Boolean
        get() = true

    @UseExperimental(ImplicitReflectionSerializer::class)
    override fun onRender(call: ApplicationCall) {
        m.showLoadingScreen()

        val userIdentification = m.userIdentification ?: return

        SpicyMorenitta.INSTANCE.launch {
            val result = http.get<String> {
                url("${window.location.origin}/api/v1/users/@me/guilds?check-join=true")
            }

            val list = kotlinx.serialization.json.JSON.nonstrict.parseList<TemmieDiscordGuild>(result)

            fixDummyNavbarHeight(call)
            switchContent(call)

            twoColumnLayout(
                    leftSidebar = {
                        leftSidebarForProfileDashboard()
                    },
                    rightSidebar = {
                        rightSidebar(userIdentification, list)
                    }
            )

            SpicyMorenitta.INSTANCE.setUpLinkPreloader()

            m.hideLoadingScreen()
        }
    }

    fun DIV.rightSidebar(userIdentification: UserIdentification, list: List<TemmieDiscordGuild>) {
        div(classes = "user-info") {
            img(src = "https://cdn.discordapp.com/avatars/${userIdentification.id}/${userIdentification.avatar}.png?size=256")

            div(classes = "text") {
                div {
                    + "Bem-vindo de volta..."
                }
                div(classes = "name") {
                    + userIdentification.username
                }
            }
        }

        val canManage = list.filter { WebsiteUtils.canManageGuild(it) }
                .sortedBy { it.name }
        val hasLori = canManage.filter { it.joined }
        val doesntHasLoriButCanAdd = canManage.filter { !it.joined }

        h1 {
            + "Escolha o Servidor!"
        }

        div(classes = "server-list") {
            hasLori.forEach {
                div(classes = "entry") {
                    div(classes = "top-row") {
                        div {
                            img(src = "https://cdn.discordapp.com/icons/${it.id}/${it.icon}.png?size=128", classes = "icon") {}
                        }

                        div(classes = "info") {
                            div(classes = "name") {
                                + it.name
                            }
                            div(classes = "role") {
                                + when (WebsiteUtils.getUserPermissionLevel(it)) {
                                    WebsiteUtils.UserPermissionLevel.OWNER -> locale["website.dashboard.owner"]
                                    WebsiteUtils.UserPermissionLevel.ADMINISTRATOR -> locale["website.dashboard.administrator"]
                                    WebsiteUtils.UserPermissionLevel.MANAGER -> locale["website.dashboard.manager"]
                                    WebsiteUtils.UserPermissionLevel.MEMBER -> locale["website.dashboard.member"]
                                }
                            }
                        }
                    }
                    div(classes = "bottom-row") {
                        a(href = "/${SpicyMorenitta.INSTANCE.websiteLocaleId}/guild/${it.id}/dashboard") {
                            attributes["data-enable-link-preload"] = "true"
                            i(classes = "fas fa-cogs") {}
                            + " ${locale["website.dashboard.configureLori"]}"
                        }
                    }
                }
            }

            doesntHasLoriButCanAdd.forEach {
                div(classes = "entry") {
                    div(classes = "top-row") {
                        div {
                            img(src = "https://cdn.discordapp.com/icons/${it.id}/${it.icon}.png?size=128", classes = "icon") {}
                        }

                        div(classes = "info") {
                            div(classes = "name") {
                                +it.name
                            }
                            div(classes = "role") {
                                + WebsiteUtils.getUserPermissionLevel(it).name
                            }
                        }
                    }
                    div(classes = "bottom-row") {
                        a(href = "/${SpicyMorenitta.INSTANCE.websiteLocaleId}/guild/${it.id}/dashboard") {
                            attributes["data-enable-link-preload"] = "true"
                            i(classes = "fas fa-plus") {}
                            + " ${locale["website.dashboard.addLori"]}"
                        }
                    }
                }
            }
        }
    }
}