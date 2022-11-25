package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.*
import kotlinx.html.dom.append
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.ActionType
import net.perfectdreams.spicymorenitta.utils.locale.buildAsHtml
import net.perfectdreams.spicymorenitta.utils.select
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import org.w3c.dom.HTMLDivElement
import utils.Moment

class AuditLogRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/audit-log") {
    override val keepLoadingScreen: Boolean
        get() = true

    override fun onRender(call: ApplicationCall) {
        Moment.locale("pt-br")

        m.showLoadingScreen()

        SpicyMorenitta.INSTANCE.launch {
            val result = http.get {
                url("${window.location.origin}/api/v1/guilds/${call.parameters["guildid"]}/audit-log")
            }.bodyAsText()

            val list = kotlinx.serialization.json.JSON.nonstrict.decodeFromString(ServerConfig.WebAuditLogWrapper.serializer(), result)

            fixDummyNavbarHeight(call)
            m.fixLeftSidebarScroll {
                switchContent(call)
            }

            val entriesDiv = document.select<HTMLDivElement>("#audit-log-entries")

            for (entry in list.entries) {
                val user = list.users.first { it.id == entry.id }

                entriesDiv.append {
                    div {
                        createAuditLogEntry(user, entry)
                    }
                }
            }

            m.hideLoadingScreen()
        }
    }

    fun DIV.createAuditLogEntry(selfMember: ServerConfig.SelfMember, entry: ServerConfig.WebAuditLogEntry) {
        val type = ActionType.valueOf(entry.type)

        this.div(classes = "discord-generic-entry timer-entry") {
            img(classes = "amino-small-image") {
                style = "width: 6%; height: auto; border-radius: 999999px; float: left; position: relative; bottom: 8px;"

                src = selfMember.effectiveAvatarUrl
            }
            div(classes = "pure-g") {
                div(classes = "pure-u-1 pure-u-md-18-24") {
                    div {
                        style = "margin-left: 10px; margin-right: 10px;"

                        val updateString = locale["modules.auditLog.${type.updateType}"]

                        div(classes = "amino-title entry-title") {
                            style = "font-family: Lato,Helvetica Neue,Helvetica,Arial,sans-serif;"

                            locale.buildAsHtml(updateString, { num ->
                                if (num == 0) {
                                    + selfMember.name

                                    span {
                                        style = "font-size: 0.8em; opacity: 0.6;"
                                        + "#${selfMember.discriminator}"
                                    }
                                }

                                if (num == 1) {
                                    span {
                                        val sectionName = when (type) {
                                            ActionType.UPDATE_YOUTUBE -> "YouTube"
                                            ActionType.UPDATE_TWITCH -> "Twitch"
                                            ActionType.UPDATE_MISCELLANEOUS -> locale["commands.category.misc.name"]
                                            else -> locale["modules.sectionNames.${type.sectionName}"]
                                        }

                                        + sectionName
                                    }
                                }
                            }) { str ->
                                span {
                                    style = "opacity: 0.8;"

                                    + str
                                }
                            }
                        }
                        div(classes = "amino-title toggleSubText") {
                            + (Moment.unix(entry.executedAt / 1000).calendar())
                        }
                    }
                }
                /* div(classes = "pure-u-1 pure-u-md-6-24 vertically-centered-right-aligned") {
                    button(classes="button-discord button-discord-edit pure-button edit-timer-button") {
                        onClickFunction = {
                            println("Saving!")
                            SaveUtils.prepareSave("premium", {
                                it["keyId"] = donationKey.id
                            }, onFinish = {
                                val guild = JSON.nonstrict.parse(ServerConfig.Guild.serializer(), it.body)

                                PremiumKeyView.generateStuff(guild)
                            })
                        }
                        + "Ativar"
                    }
                } */
            }
        }
    }
}